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
public class TemperatureRange {
    private static final int DEFAULT_MIN = 16;
    private static final int DEFAULT_MAX = 30;

    private int min;
    private int max;

    public TemperatureRange(int min, int max) {
        this.min = set(min, DEFAULT_MIN);
        this.max = set(max, DEFAULT_MAX);
    }

    private int set(int value, int defaultValue) {
        if (value == -1) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public boolean isValid(double targetValue) {
        return targetValue >= min && targetValue <= max;
    }

    @Override
    public String toString() {
        return "TemperatureRange{" + "min=" + min + ", max=" + max + '}';
    }
}
