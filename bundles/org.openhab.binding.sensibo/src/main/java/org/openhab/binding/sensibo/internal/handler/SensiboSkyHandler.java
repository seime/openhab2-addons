/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.sensibo.internal.SensiboBindingConstants.CHANNEL_CURRENT_HUMIDITY;
import static org.openhab.binding.sensibo.internal.SensiboBindingConstants.CHANNEL_CURRENT_TEMPERATURE;
import static org.openhab.binding.sensibo.internal.SensiboBindingConstants.CHANNEL_FAN_LEVEL;
import static org.openhab.binding.sensibo.internal.SensiboBindingConstants.CHANNEL_MASTER_SWITCH;
import static org.openhab.binding.sensibo.internal.SensiboBindingConstants.CHANNEL_MODE;
import static org.openhab.binding.sensibo.internal.SensiboBindingConstants.CHANNEL_SWING_MODE;
import static org.openhab.binding.sensibo.internal.SensiboBindingConstants.CHANNEL_TARGET_TEMPERATURE;
import static org.openhab.binding.sensibo.internal.SensiboBindingConstants.CHANNEL_TIMER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.IncommensurableException;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Temperature;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.thing.type.StateChannelTypeBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.sensibo.internal.CallbackChannelsTypeProvider;
import org.openhab.binding.sensibo.internal.SensiboBindingConstants;
import org.openhab.binding.sensibo.internal.config.SensiboSkyConfiguration;
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
    public static final String SWING_PROPERTY = "swing";
    public static final String MASTER_SWITCH_PROPERTY = "on";
    public static final String FAN_LEVEL_PROPERTY = "fanLevel";
    public static final String MODE_PROPERTY = "mode";
    public static final String TARGET_TEMPERATURE_PROPERTY = "targetTemperature";
    private final Logger logger = LoggerFactory.getLogger(SensiboSkyHandler.class);
    private final Map<ChannelTypeUID, ChannelType> generatedChannelTypes = new HashMap<>();
    private Optional<SensiboSkyConfiguration> config = Optional.empty();

    public SensiboSkyHandler(final Thing thing) {
        super(thing);
    }

    private static String beautify(final String camelCaseWording) {
        final StringBuilder b = new StringBuilder();
        for (final String s : StringUtils.splitByCharacterTypeCamelCase(camelCaseWording)) {
            b.append(" ");
            b.append(s);
        }
        final StringBuilder bs = new StringBuilder();
        for (final String t : StringUtils.splitByWholeSeparator(b.toString(), " _")) {
            bs.append(" ");
            bs.append(t);
        }

        return WordUtils.capitalizeFully(bs.toString()).trim();
    }

    private String getMacAddress() {
        if (config.isPresent()) {
            return config.get().macAddress;
        }
        throw new IllegalArgumentException("No configuration present");
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        handleCommand(channelUID, command, getSensiboModel());
    }

    public void updateAcState(SensiboSky sensiboSky, String property, Object value) {
        StateChange stateChange = checkStateChangeValid(sensiboSky, property, value);
        if (stateChange.valid) {
            getAccountHandler().ifPresent(
                    handler -> handler.updateSensiboSkyAcState(getMacAddress(), property, stateChange.value, this));
        } else {
            logger.info("Update command not sent; invalid state change for SensiboSky AC state: {}",
                    stateChange.validationMessage);
        }
    }

    private void updateTimer(SensiboSky sensiboSky, @Nullable Integer secondsFromNowUntilSwitchOff) {
        getAccountHandler()
                .ifPresent(handler -> handler.updateSensiboSkyTimer(getMacAddress(), secondsFromNowUntilSwitchOff));
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command, final SensiboModel model) {
        model.findSensiboSkyByMacAddress(getMacAddress()).ifPresent(sensiboSky -> {
            if (sensiboSky.isAlive()) {

                updateStatus(ThingStatus.ONLINE); // In case it has been offline
                if (CHANNEL_CURRENT_HUMIDITY.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        updateState(channelUID, new QuantityType<>(sensiboSky.getHumidity(), Units.PERCENT));
                    }
                } else if (CHANNEL_CURRENT_TEMPERATURE.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        updateState(channelUID,
                                new QuantityType<>(sensiboSky.getTemperature(), sensiboSky.getTemperatureUnit()));
                    }
                } else if (CHANNEL_MASTER_SWITCH.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        updateState(channelUID, OnOffType.from(sensiboSky.getAcState().get().isOn()));
                    } else if (command instanceof OnOffType) {
                        updateAcState(sensiboSky, MASTER_SWITCH_PROPERTY, (OnOffType) command == OnOffType.ON);
                    }
                } else if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        if (sensiboSky.getAcState().isPresent()
                                && sensiboSky.getAcState().get().getTargetTemperature() != null) {
                            updateState(channelUID,
                                    new QuantityType<>(sensiboSky.getAcState().get().getTargetTemperature(),
                                            sensiboSky.getTemperatureUnit()));
                        } else {
                            updateState(channelUID, UnDefType.UNDEF);
                        }
                    } else if (command instanceof QuantityType<?>) {
                        QuantityType<?> newValue = (QuantityType<?>) command;
                        if (!Objects.equals(sensiboSky.getTemperatureUnit(), newValue.getUnit())) {
                            // If quantity is given in celsius when fahrenheit is used or opposite
                            try {
                                UnitConverter temperatureConverter = newValue.getUnit()
                                        .getConverterToAny(sensiboSky.getTemperatureUnit());
                                // No decimals supported
                                long convertedValue = (long) temperatureConverter.convert(newValue.longValue());
                                updateAcState(sensiboSky, TARGET_TEMPERATURE_PROPERTY, new DecimalType(convertedValue));
                            } catch (UnconvertibleException | IncommensurableException e) {
                                logger.info("Could not convert {} to {}: {}", newValue, sensiboSky.getTemperatureUnit(),
                                        e.getMessage());
                            }
                        } else {
                            updateAcState(sensiboSky, TARGET_TEMPERATURE_PROPERTY,
                                    new DecimalType(newValue.intValue()));
                        }

                    } else if (command instanceof DecimalType) {
                        updateAcState(sensiboSky, TARGET_TEMPERATURE_PROPERTY, command);
                    }
                } else if (CHANNEL_MODE.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        if (sensiboSky.getAcState().isPresent()) {
                            updateState(channelUID, new StringType(sensiboSky.getAcState().get().getMode()));
                        } else {
                            updateState(channelUID, UnDefType.UNDEF);
                        }
                    } else if (command instanceof StringType) {
                        final StringType newValue = (StringType) command;
                        updateAcState(sensiboSky, MODE_PROPERTY, newValue.toString());
                        addDynamicChannelsAndProperties(sensiboSky);
                    }
                } else if (CHANNEL_SWING_MODE.equals(channelUID.getId())) {
                    if (command instanceof RefreshType && sensiboSky.getAcState().isPresent()) {
                        if (sensiboSky.getAcState().isPresent() && sensiboSky.getAcState().get().getSwing() != null) {
                            updateState(channelUID, new StringType(sensiboSky.getAcState().get().getSwing()));
                        } else {
                            updateState(channelUID, UnDefType.UNDEF);
                        }
                    } else if (command instanceof StringType) {
                        final StringType newValue = (StringType) command;
                        updateAcState(sensiboSky, SWING_PROPERTY, newValue.toString());
                    }
                } else if (CHANNEL_FAN_LEVEL.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        if (sensiboSky.getAcState().isPresent()
                                && sensiboSky.getAcState().get().getFanLevel() != null) {
                            updateState(channelUID, new StringType(sensiboSky.getAcState().get().getFanLevel()));
                        } else {
                            updateState(channelUID, UnDefType.UNDEF);
                        }
                    } else if (command instanceof StringType) {
                        final StringType newValue = (StringType) command;
                        updateAcState(sensiboSky, FAN_LEVEL_PROPERTY, newValue.toString());
                    }
                } else if (CHANNEL_TIMER.equals(channelUID.getId())) {
                    if (command instanceof RefreshType) {
                        if (sensiboSky.getTimer().isPresent() && sensiboSky.getTimer().get().secondsRemaining > 0) {
                            updateState(channelUID, new DecimalType(sensiboSky.getTimer().get().secondsRemaining));
                        } else {
                            updateState(channelUID, UnDefType.UNDEF);
                        }
                    } else if (command instanceof DecimalType) {
                        final DecimalType newValue = (DecimalType) command;
                        updateTimer(sensiboSky, newValue.intValue());
                    } else {
                        updateTimer(sensiboSky, null);
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unreachable by Sensibo servers");
            }
        });
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.unmodifiableList(Stream.of(CallbackChannelsTypeProvider.class).collect(Collectors.toList()));
    }

    @Override
    public void initialize() {
        config = Optional.ofNullable(getConfigAs(SensiboSkyConfiguration.class));
        logger.debug("Initializing SensiboSky using config {}", config);
        getSensiboModel().findSensiboSkyByMacAddress(getMacAddress()).ifPresent(pod -> {
            addDynamicChannelsAndProperties(pod);

            if (pod.isAlive()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unreachable by Sensibo servers");
            }
        });
    }

    private boolean isDynamicChannel(final ChannelTypeUID uid) {
        return SensiboBindingConstants.DYNAMIC_CHANNEL_TYPES.stream().anyMatch(e -> uid.getId().startsWith(e));
    }

    private void addDynamicChannelsAndProperties(final SensiboSky sensiboSky) {
        final List<Channel> newChannels = new ArrayList<>();
        for (final Channel channel : getThing().getChannels()) {
            final ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
            if (channelTypeUID != null && !isDynamicChannel(channelTypeUID)) {
                newChannels.add(channel);
            }
        }

        generatedChannelTypes.clear();

        newChannels.addAll(createDynamicChannels(sensiboSky));
        Map<String, String> properties = sensiboSky.getThingProperties();
        updateThing(editThing().withChannels(newChannels).withProperties(properties).build());
    }

    public List<Channel> createDynamicChannels(final SensiboSky sensiboSky) {
        final List<Channel> newChannels = new ArrayList<>();

        sensiboSky.getCurrentModeCapabilities().ifPresent(capabilities -> {
            // Not all modes have swing and fan level
            final ChannelTypeUID swingModeChannelType = addChannelType(SensiboBindingConstants.CHANNEL_TYPE_SWING_MODE,
                    "Swing Mode", "String", capabilities.swingModes, null, null);
            newChannels.add(ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_SWING_MODE), "String")
                    .withType(swingModeChannelType).build());

            final ChannelTypeUID fanLevelChannelType = addChannelType(SensiboBindingConstants.CHANNEL_TYPE_FAN_LEVEL,
                    "Fan Level", "String", capabilities.fanLevels, null, null);
            newChannels.add(ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_FAN_LEVEL), "String")
                    .withType(fanLevelChannelType).build());
        });

        final ChannelTypeUID modeChannelType = addChannelType(SensiboBindingConstants.CHANNEL_TYPE_MODE, "Mode",
                "String", sensiboSky.getRemoteCapabilities().keySet(), null, null);
        newChannels.add(ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_MODE), "String")
                .withType(modeChannelType).build());

        final ChannelTypeUID targetTemperatureChannelType = addChannelType(
                SensiboBindingConstants.CHANNEL_TYPE_TARGET_TEMPERATURE, "Target Temperature", "Number:Temperature",
                sensiboSky.getTargetTemperatures(), "%d %unit%", "TargetTemperature");
        newChannels.add(ChannelBuilder
                .create(new ChannelUID(getThing().getUID(), SensiboBindingConstants.CHANNEL_TARGET_TEMPERATURE),
                        "Number:Temperature")
                .withType(targetTemperatureChannelType).build());

        return newChannels;
    }

    private ChannelTypeUID addChannelType(final String channelTypePrefix, final String label, final String itemType,
            final Collection<?> options, @Nullable final String pattern, @Nullable final String tag) {
        final ChannelTypeUID channelTypeUID = new ChannelTypeUID(SensiboBindingConstants.BINDING_ID,
                channelTypePrefix + getThing().getUID().getId());
        final List<StateOption> stateOptions = options.stream()
                .map(e -> new StateOption(e.toString(), e instanceof String ? beautify((String) e) : e.toString()))
                .collect(Collectors.toList());

        StateDescriptionFragmentBuilder stateDescription = StateDescriptionFragmentBuilder.create().withReadOnly(false)
                .withOptions(stateOptions);
        if (pattern != null) {
            stateDescription = stateDescription.withPattern(pattern);
        }
        final StateChannelTypeBuilder builder = ChannelTypeBuilder.state(channelTypeUID, label, itemType)
                .withStateDescription(stateDescription.build().toStateDescription());
        if (tag != null) {
            builder.withTag(tag);
        }
        final ChannelType channelType = builder.build();

        generatedChannelTypes.put(channelTypeUID, channelType);

        return channelTypeUID;
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable final Locale locale) {
        return generatedChannelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(final ChannelTypeUID channelTypeUID, @Nullable final Locale locale) {
        return generatedChannelTypes.get(channelTypeUID);
    }

    public StateChange checkStateChangeValid(SensiboSky sensiboSky, String property, Object value) {

        StateChange stateChange = new StateChange(value);

        sensiboSky.getCurrentModeCapabilities().ifPresent(currentModeCapabilities -> {
            switch (property) {
                case TARGET_TEMPERATURE_PROPERTY:
                    Unit<Temperature> temperatureUnit = sensiboSky.getTemperatureUnit();
                    org.openhab.binding.sensibo.internal.dto.poddetails.Temperature validTemperatures = currentModeCapabilities.temperatures
                            .get(temperatureUnit == SIUnits.CELSIUS ? "C" : "F");
                    DecimalType rawValue = (DecimalType) value;
                    stateChange.updateValue(rawValue.intValue());
                    if (!validTemperatures.validValues.contains(rawValue.intValue())) {
                        stateChange.addError(String.format(
                                "Cannot change targetTemperature to '%d', valid targetTemperatures are one of %s",
                                rawValue.intValue(), ToStringBuilder.reflectionToString(
                                        validTemperatures.validValues.toArray(), ToStringStyle.SIMPLE_STYLE)));
                    }

                    break;
                case MODE_PROPERTY:
                    if (!sensiboSky.getRemoteCapabilities().keySet().contains(value)) {
                        stateChange.addError(String.format("Cannot change mode to %s, valid modes are %s", value,
                                ToStringBuilder.reflectionToString(
                                        sensiboSky.getRemoteCapabilities().keySet().toArray(),
                                        ToStringStyle.SIMPLE_STYLE)));
                    }
                    break;
                case FAN_LEVEL_PROPERTY:
                    if (!currentModeCapabilities.fanLevels.contains(value)) {
                        stateChange.addError(String.format("Cannot change fanLevel to %s, valid fanLevels are %s",
                                value, ToStringBuilder.reflectionToString(currentModeCapabilities.fanLevels.toArray(),
                                        ToStringStyle.SIMPLE_STYLE)));
                    }
                    break;
                case MASTER_SWITCH_PROPERTY:
                    // Always allowed
                    break;
                case SWING_PROPERTY:
                    if (!currentModeCapabilities.swingModes.contains(value)) {
                        stateChange.addError(String.format("Cannot change swing to %s, valid swings are %s", value,
                                ToStringBuilder.reflectionToString(currentModeCapabilities.swingModes.toArray(),
                                        ToStringStyle.SIMPLE_STYLE)));
                    }
                    break;
                default:
                    stateChange.addError(String.format("No such ac state property %s", property));
            }

            logger.debug("State change request {}", stateChange);

        });
        return stateChange;
    }

    public class StateChange {
        Object value;

        boolean valid = true;
        @Nullable
        String validationMessage;

        public StateChange(Object value) {
            this.value = value;
        }

        public void updateValue(Object updatedValue) {
            this.value = updatedValue;
        }

        public void addError(String validationMessage) {
            valid = false;
            this.validationMessage = validationMessage;
        }

        @Override
        public String toString() {
            return "StateChange [valid=" + valid + ", validationMessage=" + validationMessage + ", value=" + value
                    + ", value Class=" + value.getClass() + "]";
        }
    }
}
