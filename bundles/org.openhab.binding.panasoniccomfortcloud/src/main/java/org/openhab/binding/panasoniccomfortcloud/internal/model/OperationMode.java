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
package org.openhab.binding.panasoniccomfortcloud.internal.model;

/**
 * @author Arne Seime - Initial contribution
 */
public enum OperationMode {
    AUTO(0),
    DRY(1),
    COOL(2),
    HEAT(3),
    FAN(4);

    public final int value;

    OperationMode(int value) {
        this.value = value;
    }

    public static OperationMode parseValue(int value) {
        for (OperationMode e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}
