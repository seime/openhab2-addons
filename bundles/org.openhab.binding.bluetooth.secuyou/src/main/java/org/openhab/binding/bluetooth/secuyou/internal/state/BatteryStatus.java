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
 * Enum defining possible battery status categories
 * 
 * @author Arne Seime - Initial contribution
 */
public enum BatteryStatus {
    GOOD(0),
    LOW(1),
    CRITICAL(2),
    EMPTY(3),
    UNKNOWN(4);

    private int value;

    BatteryStatus(int value) {
        this.value = value;
    }

    static BatteryStatus fromValue(byte value) {
        for (BatteryStatus state : BatteryStatus.values()) {
            if (state.value == (int) value) {
                return state;
            }
        }
        return BatteryStatus.UNKNOWN; // Default value
    }
}
