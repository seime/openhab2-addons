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

import static org.openhab.binding.millheat.internal.MillHeatBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.millheat.internal.MillHeatBindingConstants;
import org.openhab.binding.millheat.internal.MillHeatRoomConfiguration;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.ModeType;
import org.openhab.binding.millheat.internal.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MillHeatRoomHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillHeatRoomHandler extends MillheatBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MillHeatRoomHandler.class);

    @Nullable
    private MillHeatRoomConfiguration config;

    public MillHeatRoomHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        MillHeatBridgeHandler handler = (MillHeatBridgeHandler) getBridge().getHandler();

        if (handler != null) {
            MillheatModel model = handler.getModel();

            handleCommand(channelUID, command, model);
        } else {
            logger.error("BridgeHandler is null, cannot update data");
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
            } else if (CHANNEL_COMFORT_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new DecimalType(room.comfortTemp));
                } else {
                    MillHeatBridgeHandler handler = (MillHeatBridgeHandler) getBridge().getHandler();
                    handler.updateRoomTemperature(config.roomId, command, ModeType.Comfort);
                }
            } else if (CHANNEL_SLEEP_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new DecimalType(room.sleepTemp));
                } else {
                    MillHeatBridgeHandler handler = (MillHeatBridgeHandler) getBridge().getHandler();
                    handler.updateRoomTemperature(config.roomId, command, ModeType.Sleep);
                }
            } else if (CHANNEL_AWAY_TEMPERATURE.equals(channelUID.getId())) {
                if (command instanceof RefreshType) {
                    updateState(channelUID, new DecimalType(room.awayTemp));
                } else {
                    MillHeatBridgeHandler handler = (MillHeatBridgeHandler) getBridge().getHandler();
                    handler.updateRoomTemperature(config.roomId, command, ModeType.Away);
                }
            } else if (MillHeatBindingConstants.CHANNEL_HEATING_ACTIVE.equals(channelUID.getId())) {
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
        config = getConfigAs(MillHeatRoomConfiguration.class);

        MillHeatBridgeHandler handler = (MillHeatBridgeHandler) getBridge().getHandler();

        boolean handled = false;

        if (handler != null) {
            MillheatModel model = handler.getModel();
            Room room = model.findRoomById(config.roomId);
            if (room != null) {
                updateStatus(ThingStatus.ONLINE);
                handled = true;
            }

        }

        if (!handled) {
            updateStatus(ThingStatus.OFFLINE);
        }

    }
}
