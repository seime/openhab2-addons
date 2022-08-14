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
public enum AirSwingUpDown {
    AUTO(-1),
    TOP(0),
    TOP_MIDDLE(3),
    MIDDLE(2),
    MIDDLE_BOTTOM(4),
    BOTTOM(1),
    ALL(5);

    public final int value;

    AirSwingUpDown(int value) {
        this.value = value;
    }

    public static AirSwingUpDown parseValue(int value) {
        for (AirSwingUpDown e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}
