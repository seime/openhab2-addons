/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the device settings of a device
 *
 * @author Arne Seime - Initial contribution
 */
public class DeviceSettingsDTO {

    @SerializedName("temperature_away")
    public Double temperatureAway;
    @SerializedName("temperature_normal")
    public Double temperatureNormal;
    @SerializedName("temperature_comfort")
    public Double temperatureComfort;
    @SerializedName("temperature_sleep")
    public Double temperatureSleep;
    @SerializedName("temperature_vacactioj")
    public Double temperatureVacation;
    @SerializedName("temperature_last_set")
    public Double temperatureLastSet;
}
