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
package org.openhab.binding.august.internal.handler;

import static org.openhab.binding.august.internal.BindingConstants.CHANNEL_BATTERY;
import static org.openhab.binding.august.internal.BindingConstants.CHANNEL_DOOR_STATE;
import static org.openhab.binding.august.internal.BindingConstants.CHANNEL_LOCK_STATE;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.august.internal.ApiBridge;
import org.openhab.binding.august.internal.AugustException;
import org.openhab.binding.august.internal.config.LockConfiguration;
import org.openhab.binding.august.internal.dto.GetLockRequest;
import org.openhab.binding.august.internal.dto.GetLockResponse;
import org.openhab.binding.august.internal.dto.RemoteOperateLockRequest;
import org.openhab.binding.august.internal.dto.RemoteOperateLockResponse;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
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
 * The {@link AugustLockHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
public class AugustLockHandler extends BaseThingHandler {

    public static final String ERROR_MESSAGE_UNSUPPORTED_COMMAND = "Unsupported command {} for channel {}";

    private final Logger logger = LoggerFactory.getLogger(AugustLockHandler.class);

    private LockConfiguration config;
    @NonNullByDefault({})
    private ApiBridge apiBridge;

    public AugustLockHandler(Thing thing, ApiBridge apiBridge) {
        super(thing);
        this.apiBridge = apiBridge;
    }

    public AugustLockHandler(Thing thing) {
        super(thing);
    }

    private GetLockResponse lock;

    private Optional<ScheduledFuture<?>> statusFuture = Optional.empty();

    private boolean initialized = false;

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);
        config = getConfigAs(LockConfiguration.class);

        logger.info("Initializing lock {}", config.lockId);
        stopScheduledUpdate(); // If any

        // Workaround for testing - need to inject ApiBridge but having difficulties injecting the bridge handler in the
        // tests
        if (getBridge() != null) {
            AugustAccountHandler handler = (AugustAccountHandler) getBridge().getHandler();
            if (handler != null) {
                apiBridge = handler.getApiBridge();
            }
        }

        Objects.requireNonNull(apiBridge,
                "ApiBridge is null - must be set either directly in constructor or fetched via getBridge().getHandler()");
        logger.debug("{} Initializing lock", config.lockId);
        statusFuture = Optional.of(scheduler.scheduleWithFixedDelay(this::doPoll, config.refreshIntervalSeconds,
                config.refreshIntervalSeconds, TimeUnit.SECONDS));
        logger.info("{} Lock init successful", config.lockId);
        initialized = true;
    }

    @Override
    public void dispose() {
        initialized = false;
        stopScheduledUpdate();
        super.dispose();
    }

    public void doPoll() {
        if (getBridge().getStatus() != ThingStatus.ONLINE) {
            logger.warn("{} Not polling lock since bridge isn't online yet. Bridge reported status {}", config.lockId,
                    getBridge().getStatus());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);

            return;
        }

        logger.info("{} Polling for updated lock status", config.lockId);
        try {
            final GetLockRequest getLockRequest = new GetLockRequest(config.lockId);

            lock = apiBridge.sendRequest(getLockRequest, new TypeToken<GetLockResponse>() {
            }.getType());

            Map<String, String> properties = createProperties(lock);
            updateThing(editThing().withProperties(properties).build());
            updateStatus(ThingStatus.ONLINE);
            thing.getChannels().forEach(e -> handleCommandInternal(e.getUID(), null));
        } catch (AugustException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error retrieving data from server: " + ex.getMessage());
            // Undef all channels if error
            thing.getChannels().forEach(e -> updateState(e.getUID(), UnDefType.UNDEF));
        }
    }

    private Map<String, String> createProperties(GetLockResponse lockResponse) {
        Map<String, String> properties = new HashMap<>();
        properties.put("macAddress", lockResponse.macAddress);
        properties.put("firmwareVersion", lockResponse.currentFirmwareVersion);
        properties.put("skuNumber", lockResponse.skuNumber);
        properties.put("lockName", lockResponse.lockName);
        properties.put("houseName", lockResponse.houseName);

        return properties;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final @Nullable Command command) {
        handleCommandInternal(channelUID, command);
    }

    private void handleCommandInternal(final ChannelUID channelUID, final Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_BATTERY:
                handleBatteryCommand(channelUID, command);
                break;
            case CHANNEL_LOCK_STATE:
                handleLockStateCommand(channelUID, command);
                break;
            case CHANNEL_DOOR_STATE:
                handleDoorStateCommand(channelUID, command);
                break;
            default:
                logger.debug("{} Received command on unknown channel {}, ignoring", config.lockId, channelUID.getId());
        }
    }

    private void handleDoorStateCommand(ChannelUID channelUID, Command command) {
        if (command == null || command instanceof RefreshType) {
            logger.info("{} Updating door state channel with cloud state", config.lockId);
            updateState(channelUID,
                    "closed".equals(lock.lockStatus.doorStatus) ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private void handleLockStateCommand(ChannelUID channelUID, Command command) {
        if (command == null) {
            logger.info("{} Updating lock state channel with cloud state", config.lockId);
            updateState(channelUID, "locked".equals(lock.lockStatus.lockStatus) ? OnOffType.ON : OnOffType.OFF);
        } else if (command instanceof OnOffType || command instanceof RefreshType) {
            try {
                logger.info("{} Querying lock/performing operation for lock state", config.lockId);
                final RemoteOperateLockRequest operateLockRequest = new RemoteOperateLockRequest(config.lockId,
                        getOperationFromCommand(command));
                RemoteOperateLockResponse rsp = apiBridge.sendRequest(operateLockRequest,
                        new TypeToken<RemoteOperateLockResponse>() {
                        }.getType());

                updateState(channelUID, rsp.lockStatus.equals("kAugLockState_Unlocked") ? OnOffType.OFF : OnOffType.ON);
            } catch (AugustException e) {
                logger.warn("{} Error contacting lock", config.lockId, e);
                updateState(channelUID, UnDefType.UNDEF);
            }

        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private void handleBatteryCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType || command == null) {
            updateState(channelUID, new QuantityType<>(lock.batteryPercentage * 100, Units.PERCENT));
        } else {
            logger.debug(ERROR_MESSAGE_UNSUPPORTED_COMMAND, command, channelUID);
        }
    }

    private RemoteOperateLockRequest.Operation getOperationFromCommand(Command command) {
        if (command instanceof RefreshType) {
            return RemoteOperateLockRequest.Operation.STATUS;
        } else if (command instanceof OnOffType) {
            return command == OnOffType.ON ? RemoteOperateLockRequest.Operation.LOCK
                    : RemoteOperateLockRequest.Operation.UNLOCK;
        } else {
            return null;
        }
    }

    /**
     * Stops this thing's polling future
     */
    private void stopScheduledUpdate() {
        statusFuture.ifPresent(future -> {
            if (!future.isCancelled()) {
                future.cancel(true);
            }
            statusFuture = Optional.empty();
        });
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Only used for testing, need to make public for mocking purposes
     * 
     * @return
     */
    @Override
    public @Nullable Bridge getBridge() {
        return super.getBridge();
    }
}
