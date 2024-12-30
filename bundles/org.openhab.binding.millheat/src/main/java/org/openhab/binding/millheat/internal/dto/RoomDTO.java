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
 * This DTO class wraps the room json structure
 * 
 * @author Arne Seime - Initial contribution
 */
public class RoomDTO {
    public String id;

    public String name;
    public double roomAwayTemperature;
    public double roomComfortTemperature;
    public double roomSleepTemperature;
    @SerializedName("averageTemperature")
    public Double currentTemp;
    public String roomProgram;

    public String activeModeFromWeeklyProgram;
    public boolean heatStatus = false;
    @SerializedName("roomOnlineDevicesNumber")
    public int onlineDeviceCount = 0;
    @SerializedName("roomOfflineDevicesNumber")
    public int offLineDeviceCount = 0;
    @SerializedName("roomTotalDevicesNumber")
    public int totalCount = 0;
    @SerializedName("isRoomOnline")
    public boolean online;
}
