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

import static org.openhab.binding.millheat.internal.MillheatBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.millheat.internal.MillheatBindingConstants;
import org.openhab.binding.millheat.internal.config.MillheatRoomConfiguration;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.ModeType;
import org.openhab.binding.millheat.internal.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MillheatRoomHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillheatRoomHandler extends MillheatBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MillheatRoomHandler.class);

    private MillheatRoomConfiguration config;

    public MillheatRoomHandler(Thing thing) {
        super(thing);
        config = getConfigAs(MillheatRoomConfiguration.class);
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
        } else {
            logger.error("Bridge is null, cannot update data");
        }

    }

    private void updateRoomTemperature(String roomId, Command command, ModeType modeType) {
        Bridge bridge = getBridge();
        if (bridge != null) {
            MillheatBridgeHandler handler = (MillheatBridgeHandler) bridge.getHandler();

            if (handler != null) {
                handler.updateRoomTemperature(config.roomId, command, modeType);
            } else {
                logger.error("BridgeHandler is null, cannot update data");
            }
        } else {
            logger.error("Bridge is null, cannot update data");
        }
    }

    @Override
    protected void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command,
            @NonNull MillheatModel model) {
        Room room = model.findRoomById(config.roomId);
        if (room != null) {
            if (CHANNEL_CURRENT_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new DecimalType(room.currentTemp));
                }
            } else if (CHANNEL_CURRENT_MODE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new StringType(room.mode.toString()));
                }
            } else if (CHANNEL_PROGRAM.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new StringType(room.roomProgram.name));
                }
            } else if (CHANNEL_COMFORT_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new DecimalType(room.comfortTemp));
                } else {
                    updateRoomTemperature(config.roomId, command, ModeType.Comfort);
                }
            } else if (CHANNEL_SLEEP_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new DecimalType(room.sleepTemp));
                } else {
                    updateRoomTemperature(config.roomId, command, ModeType.Sleep);
                }
            } else if (CHANNEL_AWAY_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new DecimalType(room.awayTemp));
                } else {
                    updateRoomTemperature(config.roomId, command, ModeType.Away);
                }
            } else if (MillheatBindingConstants.CHANNEL_HEATING_ACTIVE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, room.heatingActive ? OnOffType.ON : OnOffType.OFF);
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Start initializing room");
        config = getConfigAs(MillheatRoomConfiguration.class);

        MillheatBridgeHandler handler = (MillheatBridgeHandler) getBridge().getHandler();

        boolean handled = false;

        if (handler != null) {
            MillheatModel model = handler.getModel();
            Room room = model.findRoomById(config.roomId);
            if (room != null) {
                updateStatus(ThingStatus.ONLINE);
                handled = true;
            }

        } else {
            logger.error("BridgeHandler is null, initialize room");
        }

        if (!handled) {
            updateStatus(ThingStatus.OFFLINE);
        }

    }
}
