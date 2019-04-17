/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link ModeType} represents a type of mode the user can set in the app.
 *
 * @author Arne Seime - Initial contribution
 */
public enum ModeType {
    AlwaysHome(-1),
    Comfort(1),
    Sleep(2),
    Away(3),
    AdvancedAway(4),
    Off(5);

    private static Map<Integer, ModeType> map = new HashMap<>();

    static {
        for (ModeType Mode : ModeType.values()) {
            map.put(Mode.value, Mode);
        }
    }

    public static ModeType valueOf(int mode) {
        return map.get(mode);
    }

    private int value;

    private ModeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
