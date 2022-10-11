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
package org.openhab.binding.panasoniccomfortcloud.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class BindingConstants {
    public static final String BINDING_ID = "panasoniccomfortcloud";
    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_AIRCONDITION = new ThingTypeUID(BINDING_ID, "aircondition");
    public static final ThingTypeUID THING_TYPE_WATERHEATPUMP = new ThingTypeUID(BINDING_ID, "waterheatpump");
    // Fixed channels
    public static final String CHANNEL_CURRENT_INDOOR_TEMPERATURE = "currentIndoorTemperature";
    public static final String CHANNEL_CURRENT_OUTDOOR_TEMPERATURE = "currentOutdoorTemperature";
    public static final String CHANNEL_MASTER_SWITCH = "masterSwitch";
    public static final String CHANNEL_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_AIR_SWING_AUTO_MODE = "airSwingAutoMode";
    public static final String CHANNEL_OPERATION_MODE = "operationMode";
    public static final String CHANNEL_ECO_MODE = "ecoMode";
    public static final String CHANNEL_AIR_SWING_VERTICAL = "airSwingVertical";
    public static final String CHANNEL_AIR_SWING_HORIZONTAL = "airSwingHorizontal";
    public static final String CHANNEL_TARGET_TEMPERATURE = "targetTemperature";
    public static final String CHANNEL_NANOE = "nanoe";
    public static final String CHANNEL_ACTUAL_NANOE = "actualNanoe";
    public static final String DEVICE_TYPE_WIFI_DONGLE = "1";

    public static final String DEVICE_TYPE_WATERHEATPUMP = "2";
    public static final String DEVICE_TYPE_WIFI_BUILTIN = "3";
}
