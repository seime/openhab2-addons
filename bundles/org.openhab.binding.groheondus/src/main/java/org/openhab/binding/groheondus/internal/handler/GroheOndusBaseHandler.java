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
package org.openhab.binding.groheondus.internal.handler;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.groheondus.internal.GroheOndusApplianceConfiguration;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.floriansw.ondus.api.OndusService;
import io.github.floriansw.ondus.api.model.BaseAppliance;
import io.github.floriansw.ondus.api.model.Location;
import io.github.floriansw.ondus.api.model.Room;

/**
 * @author Florian Schmidt - Initial contribution
 */
@NonNullByDefault
public abstract class GroheOndusBaseHandler<T extends BaseAppliance, M> extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(GroheOndusBaseHandler.class);

    protected @Nullable GroheOndusApplianceConfiguration config;

    private @Nullable ScheduledFuture poller;

    private final int applianceType;

    // Used to space scheduled updates apart by 1 second to avoid rate limiting from service
    private int thingCounter = 0;

    protected PersistenceServiceRegistry persistenceServiceRegistry;
    protected ItemChannelLinkRegistry itemChannelLinkRegistry;

    public GroheOndusBaseHandler(Thing thing, int applianceType, int thingCounter,
            PersistenceServiceRegistry persistenceServiceRegistry, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing);
        this.applianceType = applianceType;
        this.thingCounter = thingCounter;
        this.persistenceServiceRegistry = persistenceServiceRegistry;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            schedulePolling();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            cancelTimer();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
        }
    }

    protected void schedulePolling() {
        cancelTimer(); // Cancel any pending timers to avoid duplicates
        OndusService ondusService = getOndusService();
        if (ondusService == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
            return;
        }

        @Nullable
        T appliance = getAppliance(ondusService);
        if (appliance == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/error.empty.response");
            return;
        }
        int pollingInterval = getPollingInterval(appliance);
        poller = scheduler.scheduleWithFixedDelay(this::updateChannels, thingCounter * 5, pollingInterval,
                TimeUnit.SECONDS);
        logger.debug("Scheduled polling every {}s for appliance {}", pollingInterval, thing.getUID());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing scheduled updater for thing {}", thing.getUID());
        cancelTimer();
        super.dispose();
    }

    private void cancelTimer() {
        if (poller != null) {
            poller.cancel(true);
            poller = null;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(GroheOndusApplianceConfiguration.class);
        schedulePolling();
    }

    public void updateChannels() {
        @Nullable
        OndusService ondusService = getOndusService();
        if (getBridge().getStatus() != ThingStatus.ONLINE || ondusService == null) {
            logger.debug("Bridge status {}, ondusService {},  stopping", getBridge().getStatus(), ondusService);
            cancelTimer();
            getThing().getChannels().forEach(e -> updateState(e.getUID(), UnDefType.UNDEF));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
            return;
        }

        @Nullable
        T appliance = getAppliance(ondusService);
        if (appliance == null) {
            logger.debug("Updating channels failed since appliance is null, thing {}", thing.getUID());
            cancelTimer();
            getThing().getChannels().forEach(e -> updateState(e.getUID(), UnDefType.UNDEF));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
            return;
        }

        M measurement = getLastDataPoint(appliance);
        if (measurement != null) {
            getThing().getChannels().forEach(channel -> updateChannel(channel.getUID(), appliance, measurement));
            updateStatus(ThingStatus.ONLINE);
        } else {
            cancelTimer();
            getThing().getChannels().forEach(e -> updateState(e.getUID(), UnDefType.UNDEF));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/error.noservice");
        }
    }

    protected abstract M getLastDataPoint(T appliance);

    protected abstract void updateChannel(ChannelUID channelUID, T appliance, M measurement);

    public @Nullable OndusService getOndusService() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        BridgeHandler handler = bridge.getHandler();
        if (!(handler instanceof GroheOndusAccountHandler)) {
            return null;
        }
        try {
            return ((GroheOndusAccountHandler) handler).getService();
        } catch (IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return null;
        }
    }

    protected Room getRoom() {
        return new Room(config.roomId, getLocation());
    }

    protected Location getLocation() {
        return new Location(config.locationId);
    }

    protected @Nullable T getAppliance(OndusService ondusService) {
        try {
            BaseAppliance appliance = ondusService.getAppliance(getRoom(), config.applianceId).orElse(null);
            if (appliance != null) {
                if (appliance.getType() != getType()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.wrongtype");
                    return null;
                }
                return (T) appliance;
            } else {
                logger.debug("getAppliance for thing {} returned null", thing.getUID());
                cancelTimer();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/error.failedtoloaddata");
                getThing().getChannels().forEach(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
            }

        } catch (IOException e) {
            cancelTimer();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            getThing().getChannels().forEach(channel -> updateState(channel.getUID(), UnDefType.UNDEF));
            logger.debug("Could not load appliance", e);
        }
        return null;
    }

    protected abstract int getPollingInterval(T appliance);

    private int getType() {
        return this.applianceType;
    }
}
