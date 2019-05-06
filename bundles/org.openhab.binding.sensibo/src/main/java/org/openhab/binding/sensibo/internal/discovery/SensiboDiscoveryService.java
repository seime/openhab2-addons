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
package org.openhab.binding.sensibo.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.sensibo.internal.SensiboBindingConstants;
import org.openhab.binding.sensibo.internal.handler.SensiboAccountHandler;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
import org.openhab.binding.sensibo.internal.model.SensiboSky;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arne Seime - Initial contribution
 */
public class SensiboDiscoveryService extends AbstractDiscoveryService {
    private static final long REFRESH_INTERVAL_MINUTES = 60;
    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(SensiboBindingConstants.THING_TYPE_SENSIBOSKY).collect(Collectors.toSet()));
    private final Logger logger = LoggerFactory.getLogger(SensiboDiscoveryService.class);
    private ScheduledFuture<?> discoveryJob;
    private SensiboAccountHandler accountHandler;

    public SensiboDiscoveryService(SensiboAccountHandler accountHandler) {
        super(DISCOVERABLE_THING_TYPES_UIDS, 10);
        this.accountHandler = accountHandler;
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for Sensibo devices.");
        synchronized (this) {
            try {
                ThingUID accountUID = accountHandler.getThing().getUID();
                accountHandler.updateModelFromServerAndUpdateThingStatus();
                SensiboModel model = accountHandler.getModel();
                for (SensiboSky pod : model.getPods()) {

                    ThingUID podUID = new ThingUID(SensiboBindingConstants.THING_TYPE_SENSIBOSKY, accountUID,
                            String.valueOf(pod.getMacAddress()));
                    Map<String, Object> properties = new HashMap<>();
                    properties.put("podId", pod.getId());
                    properties.put("firmwareType", pod.getFirmwareType());
                    properties.put("firmwareVersion", pod.getFirmwareVersion());
                    properties.put("productModel", pod.getProductModel());
                    properties.put("macAddress", pod.getMacAddress());

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(podUID).withBridge(accountUID)
                            .withLabel(pod.getProductName()).withRepresentationProperty("macAddress")
                            .withProperties(properties).build();
                    thingDiscovered(discoveryResult);

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
        logger.debug("Stop scan for Sensibo devices.");
        super.stopScan();
    }

    public void stopService() {
        super.abortScan();
        super.stopScan();
    }
}
