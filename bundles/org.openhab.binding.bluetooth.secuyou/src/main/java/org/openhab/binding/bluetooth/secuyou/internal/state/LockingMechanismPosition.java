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
 * Enum defining lock bolt positions
 * 
 * @author Arne Seime - Initial contribution
 */
public enum LockingMechanismPosition {
    UNLOCKED(0),
    LOCKED(1),
    LOCKING_OPERATION_IN_PROGRESS(2),
    UNKNOWN(-1);

    private int value;

    LockingMechanismPosition(int value) {
        this.value = value;
    }

    static LockingMechanismPosition fromValue(byte value) {
        for (LockingMechanismPosition state : LockingMechanismPosition.values()) {
            if (state.value == (int) value) {
                return state;
            }
        }
        return LockingMechanismPosition.UNKNOWN; // Default
    }
}
