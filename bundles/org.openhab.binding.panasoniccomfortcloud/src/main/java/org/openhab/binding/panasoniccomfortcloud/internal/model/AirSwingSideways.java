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
public enum AirSwingSideways {
    AUTO(-1),
    LEFT(0),
    LEFT_CENTER(4),
    CENTER(2),
    RIGHT_CENTER(3),
    RIGHT(1);

    public final int value;

    AirSwingSideways(int value) {
        this.value = value;
    }

    public static AirSwingSideways parseValue(int value) {
        for (AirSwingSideways e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}
