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
package org.openhab.binding.millheat.internal.discovery;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.millheat.internal.MillheatBindingConstants;
import org.openhab.binding.millheat.internal.handler.MillheatBridgeHandler;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arne Seime - Initial contribution
 */

@NonNullByDefault
public class MillheatDiscoveryService extends AbstractDiscoveryService {

    private static final long REFRESH_INTERVAL_MINUTES = 60;

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Stream
            .of(MillheatBindingConstants.THING_TYPE_HEATER, MillheatBindingConstants.THING_TYPE_ROOM)
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(MillheatDiscoveryService.class);

    private @NonNullByDefault({}) ScheduledFuture<?> discoveryJob;

    private MillheatBridgeHandler bridgeHandler;

    public MillheatDiscoveryService(MillheatBridgeHandler bridgeHandler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, 10);
        this.bridgeHandler = bridgeHandler;

    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for Millheat devices.");

        synchronized (this) {
            try {
                ThingUID bridgeUID = bridgeHandler.getThing().getUID();
                bridgeHandler.updateModelFromServerAndUpdateThingStatus();
                MillheatModel model = bridgeHandler.getModel();
                for (Home home : model.homes) {
                    for (Room room : home.rooms) {
                        ThingUID roomUID = new ThingUID(MillheatBindingConstants.THING_TYPE_ROOM, bridgeUID,
                                String.valueOf(room.id));
                        DiscoveryResult discoveryResultRoom = DiscoveryResultBuilder.create(roomUID)
                                .withBridge(bridgeUID).withLabel(room.name).withProperty("roomId", room.id)
                                .withRepresentationProperty("roomId").build();
                        thingDiscovered(discoveryResultRoom);

                        for (Heater heater : room.heaters) {
                            ThingUID heaterUID = new ThingUID(MillheatBindingConstants.THING_TYPE_HEATER, bridgeUID,
                                    String.valueOf(heater.id));
                            DiscoveryResult discoveryResultHeater = DiscoveryResultBuilder.create(heaterUID)
                                    .withBridge(bridgeUID).withLabel(heater.name).withProperty("heaterId", heater.id)
                                    .withRepresentationProperty("macAddress")
                                    .withProperty("macAddress", heater.macAddress).build();
                            thingDiscovered(discoveryResultHeater);

                        }
                    }

                    for (Heater heater : home.independentHeaters) {
                        ThingUID heaterUID = new ThingUID(MillheatBindingConstants.THING_TYPE_HEATER, bridgeUID,
                                String.valueOf(heater.id));
                        DiscoveryResult discoveryResultHeater = DiscoveryResultBuilder.create(heaterUID)
                                .withBridge(bridgeUID).withLabel(heater.name).withRepresentationProperty("heaterId")
                                .withProperty("heaterId", heater.id).build();
                        thingDiscovered(discoveryResultHeater);
                    }
                }
            } catch (Exception e) {
                logger.debug("Error during discovery: {}", e.getMessage());
            } finally {
                removeOlderResults(getTimestampOfLastScan());
            }
        }
    }

    public void startService() {
        super.activate(null);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop scan for Millheat devices.");

        super.stopScan();
    }

    public void stopService() {
        super.abortScan();
        super.stopScan();
    }

}
