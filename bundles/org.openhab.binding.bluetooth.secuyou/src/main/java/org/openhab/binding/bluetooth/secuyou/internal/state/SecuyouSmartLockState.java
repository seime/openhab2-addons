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
package org.openhab.binding.bluetooth.secuyou.internal.state;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SecuyouSmartLockState} is responsible for parsing lock state
 *
 * @author Arne Seime - Initial contribution
 */
public class SecuyouSmartLockState {

    private final Logger logger = LoggerFactory.getLogger(SecuyouSmartLockState.class);

    private boolean homeLockEnabled;
    private boolean pinCodeCorrect;
    private boolean rescueState;
    private BatteryStatus batteryStatus = BatteryStatus.UNKNOWN;
    private HandleState handleState = HandleState.UNKNOWN;
    private LockingMechanismPosition previousLockPosition = LockingMechanismPosition.UNKNOWN;
    private LockingMechanismPosition lockPosition = LockingMechanismPosition.UNKNOWN;
    private DeviceState deviceState = DeviceState.KEY_GENERATION;
    private boolean treatLockingInProgressAsLocked;

    public SecuyouSmartLockState(boolean treatLockingInProgressAsLocked) {

        this.treatLockingInProgressAsLocked = treatLockingInProgressAsLocked;
    }

    public AuthenticationState getAuthenticationState() {
        return authenticationState;
    }

    public void setAuthenticationState(AuthenticationState authenticationState) {
        this.authenticationState = authenticationState;
    }

    private AuthenticationState authenticationState = AuthenticationState.UNAUTHENTICATED;
    private byte[] challenge;

    public void setChallenge(byte[] challenge) {
        this.challenge = challenge;
    }

    public void setLockState(byte[] lockState) {
        deviceState = DeviceState.fromValue(lockState[0]);
    }

    public void setLockStatus(byte[] lockStatus) {
        previousLockPosition = lockPosition;
        lockPosition = LockingMechanismPosition.fromValue(lockStatus[0]);
        if (treatLockingInProgressAsLocked && lockPosition == LockingMechanismPosition.LOCKING_OPERATION_IN_PROGRESS) {
            logger.warn(
                    "Lock reported {} but will assume state LOCKED since lock reported LOCKING_OPERATION_IN_PROGRESS",
                    lockPosition);
            lockPosition = LockingMechanismPosition.LOCKED;
        }

        if (lockStatus[1] == 16) {
            pinCodeCorrect = true;
        } else {
            pinCodeCorrect = false;
        }

        batteryStatus = BatteryStatus.fromValue(lockStatus[2]);

        if (lockStatus[3] != 0 && lockStatus[3] != 1) {
            handleState = HandleState.OPEN;
        } else {
            handleState = HandleState.CLOSED;
        }

        byte lockPositionStatus = lockStatus[4];
        if (lockPositionStatus == 0) {
            homeLockEnabled = false;
            rescueState = false;
        } else if (lockPositionStatus == 1) {
            homeLockEnabled = true;
            rescueState = false;
        } else if (lockPositionStatus == 2) {
            homeLockEnabled = false;
            rescueState = true;
        } else if (lockPositionStatus == 3) {
            homeLockEnabled = true;
            rescueState = true;
        }
    }

    public byte[] generateChallengeResponse(String pinCode, String encryptionKeyHexString) {
        byte[] challengeResponse = Arrays.copyOf(challenge, 16);
        byte[] pinAsBytes = pinCode.getBytes(StandardCharsets.ISO_8859_1);
        for (int i = 0; i < 5; i++) {
            challengeResponse[i] = (byte) (challenge[i] + (pinAsBytes[i] - 0x30)); // Convert '0' (0x30) to 0x00
        }

        return encrypt(challengeResponse, encryptionKeyHexString);
    }

    private byte[] encrypt(byte[] data, String encryptionKeyHexString) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(DatatypeConverter.parseHexBinary(encryptionKeyHexString), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new SecurityException(String.format("Error doing pin encryption: %s", e.getMessage()), e);
        }
    }

    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public LockingMechanismPosition getLockPosition() {
        return lockPosition;
    }

    public boolean isHomeLockEnabled() {
        return homeLockEnabled;
    }

    public BatteryStatus getBatteryStatus() {
        return batteryStatus;
    }

    public HandleState getHandleState() {
        return handleState;
    }

    @Override
    public String toString() {
        return "SecuyouSmartLockState{" + "authenticationState=" + authenticationState + ", batteryStatus="
                + batteryStatus + ", deviceState=" + deviceState + ", handleState=" + handleState + ", homeLockEnabled="
                + homeLockEnabled + ", lockPosition=" + lockPosition + '}';
    }
}
