/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sensibo.internal.handler;

import static org.openhab.binding.sensibo.internal.SensiboBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.StateChannelTypeBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.sensibo.internal.CallbackChannelsTypeProvider;
import org.openhab.binding.sensibo.internal.SensiboBindingConstants;
import org.openhab.binding.sensibo.internal.config.SensiboSkyConfiguration;
import org.openhab.binding.sensibo.internal.dto.poddetails.ModeCapability;
import org.openhab.binding.sensibo.internal.model.AcState;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
import org.openhab.binding.sensibo.internal.model.SensiboSky;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.unit.Units;

/**
 * The {@link SensiboSkyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SensiboSkyHandler extends SensiboBaseThingHandler implements ChannelTypeProvider {
    private final Logger logger = LoggerFactory.getLogger(SensiboSkyHandler.class);
    private @NonNullByDefault({}) SensiboSkyConfiguration config;
    private Map<ChannelTypeUID, ChannelType> generatedChannelTypes = new HashMap<>();

    public SensiboSkyHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        handleCommand(channelUID, command, getSensiboModel());
    }

    private void updateAcState(AcState newState) {
        SensiboAccountHandler accountHandler = getAccountHandler();
        if (accountHandler != null) {
            accountHandler.updateSensiboSkyAcState(config.macAddress, newState);
        }
    }

    @Override
    protected void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command,
            @NonNull SensiboModel model) {
        Optional<SensiboSky> optionalSensiboSky = model.findSensiboSkyByMacAddress(config.macAddress);
        if (optionalSensiboSky.isPresent()) {
            SensiboSky unit = optionalSensiboSky.get();
            if (unit.isAlive()) {
                if (CHANNEL_CURRENT_HUMIDITY.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new QuantityType<>(unit.getHumidity(), Units.PERCENT));
                    }
                } else if (CHANNEL_CURRENT_TEMPERATURE.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new QuantityType<>(unit.getTemperature(), unit.getTemperatureUnit()));
                    }
                } else if (CHANNEL_MASTER_SWITCH.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        updateState(channelUID, unit.getAcState().isOn() ? OnOffType.ON : OnOffType.OFF);
                    }
                } else if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new QuantityType<>(unit.getAcState().getTargetTemperature(),
                                unit.getTemperatureUnit()));
                    } else {
                        QuantityType<?> newValue = (QuantityType<?>) command;
                        AcState newAcState = unit.getAcState().clone();
                        newAcState.setTargetTemperature(newValue.intValue());
                        newAcState.setTemperatureUnit(unit.getTemperatureUnit());
                        updateAcState(newAcState);
                    }
                } else if (CHANNEL_MODE.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new StringType(unit.getAcState().getMode()));
                    }
                } else if (CHANNEL_SWING_MODE.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new StringType(unit.getAcState().getSwing()));
                    }
                } else if (CHANNEL_FAN_LEVEL.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new StringType(unit.getAcState().getFanLevel()));
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.unmodifiableList(Stream.of(CallbackChannelsTypeProvider.class).collect(Collectors.toList()));
    }

    @Override
    public void initialize() {
        config = getConfigAs(SensiboSkyConfiguration.class);
        logger.debug("Initializing SensiboSky using config {}", config);
        Optional<SensiboSky> sensiboSky = getSensiboModel().findSensiboSkyByMacAddress(config.macAddress);
        if (sensiboSky.isPresent()) {
            SensiboSky pod = sensiboSky.get();
            addDynamicChannelsAndProperties(sensiboSky.get());

            if (pod.isAlive()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private boolean isDynamicChannel(ChannelTypeUID uid) {
        return SensiboBindingConstants.DYNAMIC_CHANNEL_TYPES.stream().filter(e -> uid.getId().startsWith(e)).findFirst()
                .isPresent();
    }

    private void addDynamicChannelsAndProperties(SensiboSky sensiboSky) {

        // Dynamic channels
        List<Channel> newChannels = new ArrayList<>();
        for (Channel channel : getThing().getChannels()) {
            ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null && !isDynamicChannel(channelTypeUID)) {
                newChannels.add(channel);
            }
        }

        generatedChannelTypes.clear();

        ModeCapability capabilities = sensiboSky.getModeCapabilities();
        List<Integer> targetTemperatures = sensiboSky.getTargetTemperatures();

        ChannelTypeUID modeChannelType = addChannelType(SensiboBindingConstants.CHANNEL_TYPE_MODE, "Mode", "String",
                sensiboSky.getRemoteCapabilities().keySet(), null, null);
        newChannels.add(ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_MODE), "String")
                .withType(modeChannelType).build());

        ChannelTypeUID swingModeChannelType = addChannelType(SensiboBindingConstants.CHANNEL_TYPE_SWING_MODE,
                "Swing Mode", "String", capabilities.getSwingModes(), null, null);
        newChannels.add(ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_SWING_MODE), "String")
                .withType(swingModeChannelType).build());

        ChannelTypeUID fanLevelChannelType = addChannelType(SensiboBindingConstants.CHANNEL_TYPE_FAN_LEVEL, "Fan Level",
                "String", capabilities.getFanLevels(), null, null);
        newChannels.add(ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_FAN_LEVEL), "String")
                .withType(fanLevelChannelType).build());

        ChannelTypeUID targetTemperatureChannelType = addChannelType(
                SensiboBindingConstants.CHANNEL_TYPE_TARGET_TEMPERATURE, "Target Temperature", "Number:Temperature",
                targetTemperatures, "%d %unit%", "TargetTemperature");
        newChannels.add(ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_TARGET_TEMPERATURE),
                        "Number:Temperature")
                .withType(targetTemperatureChannelType).build());

        // Add properties
        Map<String, String> properties = new HashMap<>();
        properties.put("podId", sensiboSky.getId());
        properties.put("firmwareType", sensiboSky.getFirmwareType());
        properties.put("firmwareVersion", sensiboSky.getFirmwareVersion());
        properties.put("productModel", sensiboSky.getProductModel());
        properties.put("macAddress", sensiboSky.getMacAddress());

        updateThing(editThing().withChannels(newChannels).withProperties(properties).build());
    }

    private ChannelTypeUID addChannelType(String channelTypePrefix, String label, String itemType,
            Collection<?> options, @Nullable String pattern, @Nullable String tag) {
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(SensiboBindingConstants.BINDING_ID,
                channelTypePrefix + getThing().getUID().getId());
        List<StateOption> stateOptions = options.stream()
                .map(e -> new StateOption(e.toString(), e instanceof String ? beautify((String) e) : e.toString()))
                .collect(Collectors.toList());
        StateDescription stateDescription = new StateDescription(null, null, null, pattern, false, stateOptions);
        StateChannelTypeBuilder builder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                .withStateDescription(stateDescription);
        if (tag != null) {
            builder.withTag(tag);
        }
        ChannelType channelType = builder.build();

        generatedChannelTypes.put(channelTypeUID, channelType);

        return channelTypeUID;

    }

    private static String beautify(String camelCaseWording) {
        StringBuilder b = new StringBuilder();
        for (String s : StringUtils.splitByCharacterTypeCamelCase(camelCaseWording)) {
            b.append(" ");
            b.append(s);
        }
        StringBuilder bs = new StringBuilder();
        for (String t : StringUtils.splitByWholeSeparator(b.toString(), " _")) {
            bs.append(" ");
            bs.append(t);
        }

        return WordUtils.capitalizeFully(bs.toString()).trim();
    }

    @Override
    public @Nullable Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return generatedChannelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return generatedChannelTypes.get(channelTypeUID);
    }

}
