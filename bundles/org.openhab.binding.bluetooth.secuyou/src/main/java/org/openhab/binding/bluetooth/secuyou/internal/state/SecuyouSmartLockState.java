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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * The {@link SecuyouSmartLockState} is responsible for parsing lock state
 *
 * @author Arne Seime - Initial contribution
 */
public class SecuyouSmartLockState {

    private boolean homeLockEnabled;
    private boolean pinCodeCorrect;
    private boolean rescueState;
    private BatteryStatus batteryStatus = BatteryStatus.UNKNOWN;
    private HandleState handleState = HandleState.UNKNOWN;
    private LockingMechanismPosition previousLockPosition = LockingMechanismPosition.UNKNOWN;
    private LockingMechanismPosition lockPosition = LockingMechanismPosition.UNKNOWN;
    private DeviceState deviceState = DeviceState.KEY_GENERATION;

    public AuthenticationState getAuthenticationState() {
        return authenticationState;
    }

    public void setAuthenticationState(AuthenticationState authenticationState) {
        this.authenticationState = authenticationState;
    }

    private AuthenticationState authenticationState = AuthenticationState.UNAUTHENTICATED;
    private byte[] challenge;

    private static byte toByte(char digit) {
        switch (digit) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            default:
                return 0;
        }
    }

    public byte[] generateChallengeResponse(String pinCode, String encryptionKeyHexString) {
        byte[] challengeResponse = new byte[16];
        Arrays.fill(challengeResponse, (byte) 0);

        int digitCounter = 0;

        while (true) {
            int position = 0;
            if (digitCounter >= 5) {
                while (position < 16) {
                    challengeResponse[position] = ((byte) (challengeResponse[position] + challenge[position]));
                    ++position;
                }

                return encrypt(challengeResponse, encryptionKeyHexString);
            }

            challengeResponse[digitCounter] = toByte(pinCode.charAt(digitCounter));
            ++digitCounter;
        }
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

    public void setChallenge(byte[] challenge) {
        this.challenge = challenge;
    }

    public void setLockState(byte[] lockStatus) {
        deviceState = DeviceState.fromValue(lockStatus[0]);
    }

    public void setLockStatus(byte[] statusData) {
        if (previousLockPosition == LockingMechanismPosition.UNKNOWN) {
            previousLockPosition = lockPosition;
            lockPosition = LockingMechanismPosition.fromValue(statusData[0]);
        }

        if (statusData[1] == 16) {
            pinCodeCorrect = true;
        } else {
            pinCodeCorrect = false;
        }

        batteryStatus = BatteryStatus.fromValue(statusData[2]);

        if (statusData[3] != 0 && statusData[3] != 1) {
            handleState = HandleState.OPEN;
        } else {
            handleState = HandleState.CLOSED;
        }

        byte lockPositionStatus = statusData[4];
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
