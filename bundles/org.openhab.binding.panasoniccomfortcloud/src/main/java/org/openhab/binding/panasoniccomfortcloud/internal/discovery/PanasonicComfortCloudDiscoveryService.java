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
package org.openhab.binding.panasoniccomfortcloud.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants;
import org.openhab.binding.panasoniccomfortcloud.internal.handler.PanasonicComfortCloudAccountHandler;
import org.openhab.binding.panasoniccomfortcloud.internal.model.Device;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class PanasonicComfortCloudDiscoveryService extends AbstractDiscoveryService {
    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .singleton(BindingConstants.THING_TYPE_AIRCONDITION);
    private static final long REFRESH_INTERVAL_MINUTES = 60;
    private final Logger logger = LoggerFactory.getLogger(PanasonicComfortCloudDiscoveryService.class);
    private final PanasonicComfortCloudAccountHandler accountHandler;
    private Optional<ScheduledFuture<?>> discoveryJob = Optional.empty();

    public PanasonicComfortCloudDiscoveryService(final PanasonicComfortCloudAccountHandler accountHandler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, 10);
        this.accountHandler = accountHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryJob = Optional
                .of(scheduler.scheduleWithFixedDelay(this::startScan, 0, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES));
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for Panasonic Comfort Cloud devices.");
        synchronized (this) {
            removeOlderResults(getTimestampOfLastScan(), null, accountHandler.getThing().getUID());
            final ThingUID accountUID = accountHandler.getThing().getUID();
            accountHandler.doPoll(false);

            List<Device> devices = accountHandler.getModel().getGroups().stream().flatMap(e -> e.getDevices().stream())
                    .collect(Collectors.toList());

            for (final Device device : devices) {
                ThingTypeUID thingType = getThingType(device);
                if (thingType != null) {

                    final ThingUID deviceUID = new ThingUID(thingType, accountUID,
                            device.getDeviceId().replace('+', '-'));
                    Map<String, String> properties = device.getThingProperties();

                    // DiscoveryResult result uses Map<String,Object> as properties while ThingBuilder uses
                    // Map<String,String>
                    Map<String, Object> stringObjectProperties = new HashMap<>();
                    stringObjectProperties.putAll(properties);

                    final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(deviceUID)
                            .withBridge(accountUID).withLabel(device.getName()).withRepresentationProperty("deviceId")
                            .withProperties(stringObjectProperties).build();
                    thingDiscovered(discoveryResult);
                } else {
                    logger.debug(
                            "Found device of type {} which is currently not supported (not known by the developer) - please report back",
                            device.getType());
                }
            }
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
        discoveryJob.ifPresent(job -> {
            if (!job.isCancelled()) {
                job.cancel(true);
            }
            discoveryJob = Optional.empty();
        });
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop scan for devices.");
        super.stopScan();
    }

    @Nullable
    private ThingTypeUID getThingType(Device device) {
        switch (device.getType()) {
            case "3":
                return BindingConstants.THING_TYPE_AIRCONDITION;

            default:
                return null;
        }
    }
}
