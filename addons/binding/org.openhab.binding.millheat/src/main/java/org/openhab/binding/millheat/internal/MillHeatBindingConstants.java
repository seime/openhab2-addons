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
package org.openhab.binding.millheat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MillHeatBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillHeatBindingConstants {

    private static final String BINDING_ID = "millheat";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_ROOM = new ThingTypeUID(BINDING_ID, "room");
    public static final ThingTypeUID THING_TYPE_HEATER = new ThingTypeUID(BINDING_ID, "heater");

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_TEMPERATURE = "currentTemperature";
    public static final String CHANNEL_COMFORT_TEMPERATURE = "comfortTemperature";
    public static final String CHANNEL_SLEEP_TEMPERATURE = "sleepTemperature";
    public static final String CHANNEL_AWAY_TEMPERATURE = "awayTemperature";
    public static final String CHANNEL_HEATING_ACTIVE = "heatingActive";
    public static final String CHANNEL_TARGET_TEMPERATURE = "targetTemperature";
    public static final String CHANNEL_CURRENT_POWER = "currentPower";
}
