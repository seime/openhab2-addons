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
package org.openhab.binding.bluetooth.secuyou.internal;

import java.util.Set;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SecuyouBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Arne Seime - Initial contribution
 * 
 */
@NonNullByDefault
public class SecuyouBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SMART_LOCK = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID,
            "secuyou_smart_lock");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_SMART_LOCK);

    // Channel IDs
    public static final String CHANNEL_ID_HANDLE_POSITION = "handle_position";
    public static final String CHANNEL_ID_LOCK = "lock";
    public static final String CHANNEL_ID_BATTERY = "battery";
    public static final String CHANNEL_ID_HOMELOCK = "home_lock";

    public static final byte[] CMD_TOGGLE_LOCK = { 1 };
    public static final byte[] CMD_TOGGLE_HOME_LOCK = { -1 };
    // public static final byte[] CMD_TOGGLE_HANDLE_AUTO_LOCK = { 16 };
    public static final byte[] CMD_GENERATE_CHALLENGE = { 1 };
    public static final byte[] CMD_CHALLENGE_RECEIVED = { 0 };

    public static final UUID FIRMWARE_REVISION_CHARACTERISTIC = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB");
    public static final UUID MODEL_NUMBER_CHARACTERISTIC = UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB");
    public static final UUID HARDWARE_REVISION_CHARACTERISTIC = UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB");
    public static final UUID NA_CHARACTERISTIC = UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB");
    public static final UUID SERIAL_CHARACTERISTIC = UUID.fromString("00002A25-0000-1000-8000-00805F9B34FB");

    public static final UUID KEY_SERVICE = UUID.fromString("B1DE1528-85EF-37CC-00C8-A3CF3412A548");
    public static final UUID LOCK_STATUS_CHARACTERISTIC = UUID.fromString("B1DE1529-85EF-37CC-00C8-A3CF3412A548");
    public static final UUID CONFIRM_CHARACTERISTIC = UUID.fromString("B1DE1530-85EF-37CC-00C8-A3CF3412A548");
    public static final UUID LOCK_STATE_CHARACTERISTIC = UUID.fromString("B1DE1531-85EF-37CC-00C8-A3CF3412A548");
    public static final UUID NAME_CHARACTERISTIC = UUID.fromString("B1DE1532-85EF-37CC-00C8-A3CF3412A548");
}
