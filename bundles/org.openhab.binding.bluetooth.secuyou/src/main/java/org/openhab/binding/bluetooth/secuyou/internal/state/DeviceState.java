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

/**
 * Enum defining device authentication states
 * 
 * @author Arne Seime - Initial contribution
 */
public enum DeviceState {
    KEY_BLOCKING(3),
    KEY_CHECKING(2),
    KEY_CONFIRMATION(1),
    KEY_GENERATION(0);

    private int value;

    DeviceState(int value) {
        this.value = value;
    }

    static DeviceState fromValue(byte value) {
        for (DeviceState state : DeviceState.values()) {
            if (state.value == (int) value) {
                return state;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }
}
