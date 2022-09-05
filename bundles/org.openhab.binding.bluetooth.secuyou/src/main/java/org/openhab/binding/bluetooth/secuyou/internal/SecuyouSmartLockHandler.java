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
package org.openhab.binding.bluetooth.secuyou.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.ConnectedBluetoothHandler;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.secuyou.internal.state.AuthenticationState;
import org.openhab.binding.bluetooth.secuyou.internal.state.BatteryStatus;
import org.openhab.binding.bluetooth.secuyou.internal.state.DeviceState;
import org.openhab.binding.bluetooth.secuyou.internal.state.LockingMechanismPosition;
import org.openhab.binding.bluetooth.secuyou.internal.state.SecuyouSmartLockState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SecuyouSmartLockHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
public class SecuyouSmartLockHandler extends ConnectedBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(SecuyouSmartLockHandler.class);

    private Optional<SecuyouConfiguration> configuration = Optional.empty();

    private Map<String, String> deviceProps = new HashMap<>();

    private SecuyouSmartLockState lock;
    private ScheduledFuture<?> keepAliveJob;
    private ScheduledFuture<?> delayedDisconnectJob;

    public SecuyouSmartLockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize {}", this);
        super.initialize();
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "Init started");

        configuration = Optional.of(getConfigAs(SecuyouConfiguration.class));
        logger.debug("Using configuration: {}", configuration.get());
        lock = new SecuyouSmartLockState(configuration.get().treatLockingInProgressAsLocked);

        if (device.getConnectionState() != BluetoothDevice.ConnectionState.CONNECTED) {
            device.connect();
        } else if (!device.isServicesDiscovered()) {
            device.discoverServices();
        } else {
            initializeLock();
        }
    }

    @Override
    public void dispose() {
        cancelKeepAlive();
        super.dispose();
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        super.onConnectionStateChange(connectionNotification);
        if (connectionNotification.getConnectionState() == BluetoothDevice.ConnectionState.DISCONNECTED) {
            cancelKeepAlive();

            delayedDisconnectJob = scheduler.schedule(() -> {
                // Do not set device to OFFLINE just yet, a reconnect might come in a very short time
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Bluetooth connection to device lost");
                // Set all channels to UNDEF
                updateState(SecuyouBindingConstants.CHANNEL_ID_HOMELOCK, UnDefType.UNDEF);
                updateState(SecuyouBindingConstants.CHANNEL_ID_LOCK, UnDefType.UNDEF);
                updateState(SecuyouBindingConstants.CHANNEL_ID_HANDLE_POSITION, UnDefType.UNDEF);
                updateState(SecuyouBindingConstants.CHANNEL_ID_BATTERY, UnDefType.UNDEF);
            }, 3, TimeUnit.SECONDS);

        } else if (connectionNotification.getConnectionState() == BluetoothDevice.ConnectionState.CONNECTED) {
            cancelDelayedDisconnect();

            // Reset state when reconnected
            lock = new SecuyouSmartLockState(configuration.get().treatLockingInProgressAsLocked);
        }
    }

    private void cancelKeepAlive() {
        if (keepAliveJob != null && !keepAliveJob.isCancelled()) {
            keepAliveJob.cancel(true);
            keepAliveJob = null;
        }
    }

    private void cancelDelayedDisconnect() {
        if (delayedDisconnectJob != null && !delayedDisconnectJob.isCancelled()) {
            delayedDisconnectJob.cancel(true);
            delayedDisconnectJob = null;
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] value) {
        super.onCharacteristicUpdate(characteristic, value);

        if (SecuyouBindingConstants.LOCK_STATUS_CHARACTERISTIC.equals(characteristic.getUuid())) {
            switch (lock.getDeviceState()) {
                case KEY_GENERATION:
                    if (lock.getAuthenticationState() == AuthenticationState.AUTHENTICATION_IN_PROGRESS) {
                        logger.info("Received challenge from lock {}", DatatypeConverter.printHexBinary(value));
                        lock.setChallenge(value);
                        BluetoothCharacteristic confirmCharacteristic = device
                                .getCharacteristic(SecuyouBindingConstants.CONFIRM_CHARACTERISTIC);
                        if (confirmCharacteristic != null) {
                            logger.info("Confirming challenge received to {}", confirmCharacteristic.getUuid());
                            device.writeCharacteristic(confirmCharacteristic,
                                    SecuyouBindingConstants.CMD_CHALLENGE_RECEIVED);
                        }
                    } else {
                        handleLockStatusUpdated(value);
                    }
                    break;
                case KEY_CHECKING:
                case KEY_CONFIRMATION:
                    handleLockStatusUpdated(value);
                    break;
                default:
                    logger.info("Received status update {} in state {}, ", DatatypeConverter.printHexBinary(value),
                            lock.getDeviceState());
            }
        } else if (SecuyouBindingConstants.LOCK_STATE_CHARACTERISTIC.equals(characteristic.getUuid())) {
            lock.setLockState(value);
            if (lock.getAuthenticationState() == AuthenticationState.AUTHENTICATION_IN_PROGRESS) {
                if (lock.getDeviceState() == DeviceState.KEY_CHECKING) {
                    // Ready for pin
                    logger.info("Lock is ready for pin to be written");
                    try {
                        byte[] challengeResponse = lock.generateChallengeResponse(configuration.get().pinCode,
                                configuration.get().encryptionKey);
                        BluetoothCharacteristic confirmCharacteristic = device
                                .getCharacteristic(SecuyouBindingConstants.LOCK_STATUS_CHARACTERISTIC);
                        if (confirmCharacteristic != null) {
                            device.writeCharacteristic(confirmCharacteristic, challengeResponse)
                                    .whenComplete((respnse, ex) -> {
                                        logger.info("Pin sent");
                                    });
                        }
                    } catch (Exception e) {
                        logger.error("Error doing pin encryption, check pin and encryptionKey thing parameters {}",
                                e.getMessage());
                    }
                } else if (lock.getDeviceState() == DeviceState.KEY_CONFIRMATION) {
                    logger.info("Authentication complete, refreshing status");
                    // Should now be authenticated
                    lock.setAuthenticationState(AuthenticationState.AUTHENTICATED);
                    refreshStatus();

                    if (configuration.get().keepAliveSeconds > -1) {
                        logger.debug("Scheduling polling every {}s", configuration.get().keepAliveSeconds);
                        keepAliveJob = scheduler.scheduleWithFixedDelay(this::refreshStatus,
                                configuration.get().keepAliveSeconds, configuration.get().keepAliveSeconds,
                                TimeUnit.SECONDS);
                    }

                    updateStatus(ThingStatus.ONLINE);

                }
            }

        }
    }

    private void handleLockStatusUpdated(byte[] lockStatus) {
        lock.setLockStatus(lockStatus);
        logger.debug("Updated state: {}", lock);

        updateState(SecuyouBindingConstants.CHANNEL_ID_BATTERY,
                lock.getBatteryStatus() == BatteryStatus.UNKNOWN ? UnDefType.UNDEF
                        : new StringType(lock.getBatteryStatus().toString()));

        updateState(SecuyouBindingConstants.CHANNEL_ID_HOMELOCK, OnOffType.from(lock.isHomeLockEnabled()));

        switch (lock.getHandleState()) {
            case CLOSED:
                updateState(SecuyouBindingConstants.CHANNEL_ID_HANDLE_POSITION, OpenClosedType.CLOSED);
                break;
            case OPEN:
                updateState(SecuyouBindingConstants.CHANNEL_ID_HANDLE_POSITION, OpenClosedType.OPEN);
                break;
            default:
                logger.info("Unsupported handle status {}", lock.getHandleState());
        }

        switch (lock.getLockPosition()) {
            case LOCKED:
                updateState(SecuyouBindingConstants.CHANNEL_ID_LOCK, OnOffType.ON);
                break;
            case UNLOCKED:
                updateState(SecuyouBindingConstants.CHANNEL_ID_LOCK, OnOffType.OFF);
                break;
            case LOCKING_OPERATION_IN_PROGRESS:
            case UNKNOWN:
                // Try unlock and locking again if status cannot be determined
                if (!unknownLockStatusRescueOperationInProgress
                        && lastRescueOperation.plus(3, ChronoUnit.MINUTES).isBefore(Instant.now())
                        && configuration.get().attemptLockRescue) {
                    lastRescueOperation = Instant.now();
                    tryDoubleLockToogleToResetUnknownLockPosition();
                } else {
                    updateState(SecuyouBindingConstants.CHANNEL_ID_LOCK, UnDefType.UNDEF);
                }
                break;
            default:
                logger.info("Unsupported lock state {}", lock.getLockPosition());
        }

        // Update channels
    }

    private Instant lastRescueOperation = Instant.EPOCH;

    private boolean unknownLockStatusRescueOperationInProgress = false;

    private synchronized void tryDoubleLockToogleToResetUnknownLockPosition() {
        unknownLockStatusRescueOperationInProgress = true;
        logger.info("Starting rescue operation");
        CountDownLatch resetCountdown = new CountDownLatch(1);
        if (device.getConnectionState() == BluetoothDevice.ConnectionState.CONNECTED) {
            BluetoothCharacteristic confirmCharacteristic = device
                    .getCharacteristic(SecuyouBindingConstants.CONFIRM_CHARACTERISTIC);
            if (confirmCharacteristic != null) {
                device.writeCharacteristic(confirmCharacteristic, SecuyouBindingConstants.CMD_TOGGLE_LOCK)
                        .whenComplete((toggle1, ex) -> {
                            logger.info("Toggle #1 sent");
                            sleep(2000);
                            device.writeCharacteristic(confirmCharacteristic, SecuyouBindingConstants.CMD_TOGGLE_LOCK)
                                    .whenComplete((toggle2, ex2) -> {
                                        logger.info("Toggle #2 sent");
                                        sleep(2000);

                                        BluetoothCharacteristic lockStatusCharacteristic = device
                                                .getCharacteristic(SecuyouBindingConstants.LOCK_STATUS_CHARACTERISTIC);

                                        if (lockStatusCharacteristic != null) {
                                            device.readCharacteristic(lockStatusCharacteristic)
                                                    .whenComplete((lockStatus, ex3) -> {
                                                        logger.info("Status update after rescue received");
                                                        handleLockStatusUpdated(lockStatus);
                                                        if (lock.getLockPosition() == LockingMechanismPosition.UNLOCKED
                                                                || lock.getLockPosition() == LockingMechanismPosition.LOCKED) {
                                                            logger.info("Rescue operation successful");
                                                        } else {
                                                            logger.warn("Rescue operation unsuccessful");
                                                        }
                                                        resetCountdown.countDown();
                                                    });
                                        } else {
                                            logger.warn(
                                                    "Could not request lock status during recovery - characteristic not found");
                                        }
                                    });

                        });
            } else {
                logger.warn("Could not send command to lock - characteristic not found");
            }
        } else {
            logger.warn("Could not send command to lock - device not connected");
        }

        try {
            resetCountdown.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            unknownLockStatusRescueOperationInProgress = false;
        }
    }

    private static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();
        initializeLock();
    }

    private synchronized void initializeLock() {
        logger.info("Starting lock handshake procedure");
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Connected, initializing");
        @Nullable
        BluetoothService keyService = device.getServices(SecuyouBindingConstants.KEY_SERVICE);
        if (keyService == null) {
            logger.debug("ERROR: Expected key service {}, cannot communicate with device", address);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unsupported device or firmware (gatt service not found)");
            disconnect();
        } else {
            setupNotifications();
            readThingProperties();

            BluetoothCharacteristic lockStateCharacteristic = keyService
                    .getCharacteristic(SecuyouBindingConstants.LOCK_STATE_CHARACTERISTIC);
            BluetoothCharacteristic lockStatusCharacteristic = keyService
                    .getCharacteristic(SecuyouBindingConstants.LOCK_STATUS_CHARACTERISTIC);
            BluetoothCharacteristic confirmCharacteristic = keyService
                    .getCharacteristic(SecuyouBindingConstants.CONFIRM_CHARACTERISTIC);

            if (lockStateCharacteristic == null || lockStatusCharacteristic == null || confirmCharacteristic == null) {
                logger.debug("ERROR: Expected GATT characteristics missing for {}, cannot communicate with device",
                        address);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unsupported device or firmware (characteristics missing)");
                disconnect();
            }

            if (lockStateCharacteristic != null) {
                device.readCharacteristic(lockStateCharacteristic).whenComplete((initialLockState, ex) -> {
                    logger.debug("Initial lock state characteristic {} from device {}: {}",
                            lockStateCharacteristic.getUuid(), address,
                            DatatypeConverter.printHexBinary(initialLockState));

                    lock.setLockState(initialLockState);
                    if (lock.getDeviceState() == DeviceState.KEY_GENERATION) {
                        // Ready to generate key
                        if (isPinPresentAndOfCorrectFormat()) {
                            // Write 01 to CONFIRM_CHARACTERISTIC
                            // Receive random data at LOCK_STATUS_CHARACTERISTIC
                            // WRITE 00 to CONFIRM_CHARACTERISTIC
                            // WAIT FOR 02 on LOCK_STATE_CHARACTERISTIC
                            // WRITE ENCODED PIN to LOCK_STATUS_CHARACTERISTIC
                            logger.info("Starting authentication");

                            if (confirmCharacteristic != null) {
                                lock.setAuthenticationState(AuthenticationState.AUTHENTICATION_IN_PROGRESS);
                                device.writeCharacteristic(confirmCharacteristic,
                                        SecuyouBindingConstants.CMD_GENERATE_CHALLENGE).whenComplete(
                                                (data, ex4) -> device.readCharacteristic(lockStateCharacteristic));
                            }
                        } else {
                            logger.warn(
                                    "Pin code is empty or of incorrect format - will not try to authenticate but quietly listen for updates");
                            if (lockStatusCharacteristic != null) {
                                lock.setDeviceState(DeviceState.KEY_CONFIRMATION); // Override state

                                device.readCharacteristic(lockStatusCharacteristic).whenComplete((keyData, keyEx) -> {
                                    logger.debug("Key Characteristic {} from device {}: {}",
                                            lockStateCharacteristic.getUuid(), address,
                                            DatatypeConverter.printHexBinary(keyData));

                                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Read only mode");

                                    handleLockStatusUpdated(keyData);
                                });
                            }
                        }
                    } else {
                        logger.warn("Lock is in unexpected state {}", lock.getDeviceState());
                    }
                });
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unsupported device or firmware (state characteristic missing)");

            }
        }
    }

    private boolean isPinPresentAndOfCorrectFormat() {
        boolean presentAndCorrect = false;
        if (configuration.isPresent()) {
            SecuyouConfiguration e = configuration.get();
            try {
                if (e.pinCode.length() == 5 && Integer.parseInt(e.pinCode) <= 99999 && e.encryptionKey.length() == 32) {
                    presentAndCorrect = true;
                }
            } catch (NumberFormatException ex) {
                logger.warn("Pin code is either not present or not numeric <= 99999 - cannot authenticate");
            }

        }
        return presentAndCorrect;
    }

    private void refreshStatus() {
        if (device.getConnectionState() == BluetoothDevice.ConnectionState.CONNECTED) {
            // Ensure we still get 'em
            setupNotifications();

            BluetoothCharacteristic lockStatusCharacteristic = device
                    .getCharacteristic(SecuyouBindingConstants.LOCK_STATUS_CHARACTERISTIC);
            if (lockStatusCharacteristic != null) {
                device.readCharacteristic(lockStatusCharacteristic);
            }
        }
    }

    private void setupNotifications() {
        BluetoothCharacteristic stateCharacteristic = device
                .getCharacteristic(SecuyouBindingConstants.LOCK_STATE_CHARACTERISTIC);
        if (stateCharacteristic != null) {
            device.enableNotifications(stateCharacteristic);
        }
        BluetoothCharacteristic keyCharacteristic = device
                .getCharacteristic(SecuyouBindingConstants.LOCK_STATUS_CHARACTERISTIC);
        if (keyCharacteristic != null) {
            device.enableNotifications(keyCharacteristic);
        }
    }

    private void readThingProperties() {
        // Update thing properties in one go
        CountDownLatch latch = new CountDownLatch(6);

        readDeviceProp(latch, SecuyouBindingConstants.MODEL_NUMBER_CHARACTERISTIC, Thing.PROPERTY_MODEL_ID,
                data -> String.valueOf(Float.parseFloat(new String(data)) / 100F));
        readDeviceProp(latch, SecuyouBindingConstants.HARDWARE_REVISION_CHARACTERISTIC, Thing.PROPERTY_HARDWARE_VERSION,
                data -> String.valueOf(Float.parseFloat(new String(data)) / 10F));
        readDeviceProp(latch, SecuyouBindingConstants.NA_CHARACTERISTIC, Thing.PROPERTY_VENDOR, String::new);
        readDeviceProp(latch, SecuyouBindingConstants.SERIAL_CHARACTERISTIC, Thing.PROPERTY_SERIAL_NUMBER, String::new);
        readDeviceProp(latch, SecuyouBindingConstants.NAME_CHARACTERISTIC, "name", String::new);
        readDeviceProp(latch, SecuyouBindingConstants.FIRMWARE_REVISION_CHARACTERISTIC, Thing.PROPERTY_FIRMWARE_VERSION,
                String::new);
        // Update thing when all requests have completed
        try {
            latch.await(10, TimeUnit.SECONDS);
            Map<String, String> existingProps = editProperties();
            deviceProps.putAll(existingProps);
            updateProperties(deviceProps);
            logger.debug("Updated thing properties");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void readDeviceProp(CountDownLatch latch, UUID characteristicUUID, String propKey,
            Function<byte[], String> resultConverter) {
        BluetoothCharacteristic characteristic = device.getCharacteristic(characteristicUUID);
        if (characteristic != null) {
            device.readCharacteristic(characteristic).whenComplete((data, ex) -> {
                deviceProps.put(propKey, resultConverter.apply(data));
                latch.countDown();
            });
        } else {
            latch.countDown();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshStatus();
        } else {
            if (lock.getAuthenticationState() == AuthenticationState.AUTHENTICATED) {
                switch (channelUID.getId()) {
                    case SecuyouBindingConstants.CHANNEL_ID_LOCK: {
                        if (lock.getLockPosition() == LockingMechanismPosition.LOCKING_OPERATION_IN_PROGRESS) {
                            logger.warn("Lock is {}, ignoring command as result will be unpredictable",
                                    lock.getLockPosition());
                        } else if (lock.getLockPosition() == LockingMechanismPosition.LOCKED
                                && command == OnOffType.ON) {
                            logger.warn("Not toggling lock as it reports as it is already locked");
                        } else if (lock.getLockPosition() == LockingMechanismPosition.UNLOCKED
                                && command == OnOffType.OFF) {
                            logger.warn("Not toggling lock as it reports as it is already unlocked");
                        } else {
                            logger.debug("Toggling lock, current state is {}", lock.getLockPosition());
                            sendCommandToLock(SecuyouBindingConstants.CMD_TOGGLE_LOCK);
                        }
                        break;
                    }
                    case SecuyouBindingConstants.CHANNEL_ID_HOMELOCK: {
                        if (lock.isHomeLockEnabled() && command == OnOffType.ON) {
                            logger.warn("Not toggling home lock as it reports {}", lock.isHomeLockEnabled());
                        } else {
                            logger.debug("Toggling homelock setting, current state is {}", lock.isHomeLockEnabled());
                            sendCommandToLock(SecuyouBindingConstants.CMD_TOGGLE_HOME_LOCK);
                        }
                        break;
                    }
                    default:
                        logger.warn("Ignored command {} for channel {}", command, channelUID.getId());
                }
            } else {
                logger.warn("Ignoring command as authentication state is not AUTHENTICATED but {}",
                        lock.getAuthenticationState());
            }
        }
        super.handleCommand(channelUID, command);
    }

    private void sendCommandToLock(byte[] data) {
        if (device.getConnectionState() == BluetoothDevice.ConnectionState.CONNECTED) {
            BluetoothCharacteristic confirmCharacteristic = device
                    .getCharacteristic(SecuyouBindingConstants.CONFIRM_CHARACTERISTIC);
            if (confirmCharacteristic != null) {
                device.writeCharacteristic(confirmCharacteristic, data);
            } else {
                logger.warn("Could not send command to lock - characteristic not found");
            }
        } else {
            logger.warn("Could not send command to lock - device not connected");
        }
    }
}
