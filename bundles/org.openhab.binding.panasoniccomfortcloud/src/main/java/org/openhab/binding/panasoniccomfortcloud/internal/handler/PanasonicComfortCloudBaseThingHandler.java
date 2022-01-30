/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

package org.openhab.binding.panasoniccomfortcloud.internal.handler;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.panasoniccomfortcloud.internal.PanasonicComfortCloudException;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.DeviceDTO;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.GetDeviceRequest;
import org.openhab.binding.panasoniccomfortcloud.internal.model.Device;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

/**
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public abstract class PanasonicComfortCloudBaseThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(PanasonicComfortCloudBaseThingHandler.class);
    @NonNullByDefault({})
    protected String deviceId;
    @NonNullByDefault({})
    protected PanasonicComfortCloudAccountHandler accountHandler;

    public PanasonicComfortCloudBaseThingHandler(final Thing thing) {
        super(thing);
    }

    protected void initialize(String deviceId) {
        accountHandler = (PanasonicComfortCloudAccountHandler) getBridge().getHandler();
        this.deviceId = deviceId;
    }

    public void loadFromServer() {
        Optional<Device> device = accountHandler.getModel().findDeviceByDeviceId(deviceId);
        if (device.isPresent()) {
            try {
                final GetDeviceRequest getDeviceDetailsRequest = new GetDeviceRequest(deviceId);

                final DeviceDTO updatedDeviceDetails = accountHandler.getApiBridge()
                        .sendRequest(getDeviceDetailsRequest, new TypeToken<DeviceDTO>() {
                        }.getType());

                device.get().mergeFromDeviceDetails(updatedDeviceDetails);
                Map<String, String> properties = device.get().getThingProperties();
                updateThing(editThing().withProperties(properties).build());
                updateStatus(ThingStatus.ONLINE);
                thing.getChannels().forEach(e -> handleCommand(e.getUID(), RefreshType.REFRESH));
            } catch (PanasonicComfortCloudException ex) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error retrieving data from server: " + ex.getMessage());
                // Undef all channels if error
                thing.getChannels().forEach(e -> updateState(e.getUID(), UnDefType.UNDEF));
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Could not find device in internal model, check deviceId configuration");
            thing.getChannels().forEach(e -> updateState(e.getUID(), UnDefType.UNDEF));
        }
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        Optional<Device> device = accountHandler.getModel().findDeviceByDeviceId(deviceId);
        if (device.isPresent()) {
            handleCommand(channelUID, command, device.get());
        } else {
            logger.debug("Ignoring command {} for channel {}, device {} is unknown", command, channelUID, deviceId);
        }
    }

    protected abstract void handleCommand(ChannelUID channelUID, Command command, Device device);
}
