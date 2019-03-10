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
package org.openhab.binding.millheat.internal.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.millheat.internal.MillheatBindingConstants;
import org.openhab.binding.millheat.internal.config.MillheatHeaterConfiguration;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MillheatHeaterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillheatHeaterHandler extends MillheatBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MillheatHeaterHandler.class);

    @Nullable
    private MillheatHeaterConfiguration config;

    public MillheatHeaterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Bridge bridge = getBridge();
        if (bridge != null) {
            MillheatBridgeHandler handler = (MillheatBridgeHandler) bridge.getHandler();

            if (handler != null) {

                MillheatModel model = handler.getModel();

                handleCommand(channelUID, command, model);

            } else {
                logger.error("BridgeHandler is null, cannot update data");
            }

        } else

        {
            logger.error("Bridge is null");
        }

    }

    @Override
    @SuppressWarnings("null")
    protected void handleCommand(ChannelUID channelUID, @NonNull Command command, @NonNull MillheatModel model) {

        Heater heater = model.findHeaterByMac(config.macAddress);
        if (heater != null) {
            if (MillheatBindingConstants.CHANNEL_CURRENT_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new DecimalType(heater.currentTemp));
                }
            } else if (MillheatBindingConstants.CHANNEL_HEATING_ACTIVE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, heater.heatingActive ? OnOffType.ON : OnOffType.OFF);
                }
            } else if (MillheatBindingConstants.CHANNEL_CURRENT_POWER.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    if (config.power != null) {
                        if (heater.heatingActive) {
                            updateState(channelUID, new DecimalType(config.power));
                        } else {
                            updateState(channelUID, new DecimalType(0));
                        }

                    } else {
                        logger.warn(
                                "Cannot update power for heater {} as the nominal power on the heater has not been configured",
                                config.macAddress);
                    }
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Start initializing heater");
        config = getConfigAs(MillheatHeaterConfiguration.class);

        MillheatBridgeHandler handler = (MillheatBridgeHandler) getBridge().getHandler();

        boolean handled = false;

        if (handler != null) {
            MillheatModel model = handler.getModel();
            Heater heater = model.findHeaterByMac(config.macAddress);
            if (heater != null) {
                updateStatus(ThingStatus.ONLINE);
                handled = true;
            }

        } else {
            logger.debug("Bridge handler not yet ready");
        }

        if (!handled) {
            logger.debug("Heater not yet ready, setting to OFFLINE");
            updateStatus(ThingStatus.OFFLINE);
        }

    }

}
