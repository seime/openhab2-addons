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
package org.openhab.binding.millheat.internal.model;

import org.openhab.binding.millheat.internal.dto.DeviceDTO;

/**
 * The {@link Heater} represents a heater, either connected to a room or independent
 *
 * @author Arne Seime - Initial contribution
 */
public class Heater {
    private Room room;
    private final String id;
    private final String name;
    private final String macAddress;
    private final boolean heatingActive;
    private boolean canChangeTemp = true;
    private final double currentTemp;
    private Double targetTemp;
    private boolean fanActive;
    private boolean powerStatus;
    private final boolean windowOpen;

    private boolean connected;

    private boolean enabled;

    public Heater(final Room room, final DeviceDTO dto) {
        this.room = room;
        id = dto.deviceId;
        name = dto.customName;
        macAddress = dto.macAddress;
        heatingActive = dto.lastMetrics.heaterFlag == 1;
        // canChangeTemp = dto.canChangeTemp;
        currentTemp = dto.lastMetrics.temperatureAmbient;
        setTargetTemp(dto.deviceSettings.temperatureLastSet);
        // setFanActive(dto.fanStatus);
        setPowerStatus(dto.lastMetrics.powerStatus == 1);
        windowOpen = dto.lastMetrics.openWindowStatus == 1;
        connected = dto.isConnected;
        enabled = dto.isEnabled;
    }

    @Override
    public String toString() {
        return "Heater [room=" + room + ", id=" + id + ", name=" + name + ", macAddress=" + macAddress
                + ", heatingActive=" + heatingActive + ", canChangeTemp=" + canChangeTemp + ", currentTemp="
                + currentTemp + ", targetTemp=" + getTargetTemp() + ", fanActive=" + fanActive() + ", powerStatus="
                + powerStatus() + ", windowOpen=" + windowOpen + "]";
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Room getRoom() {
        return room;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public boolean isHeatingActive() {
        return heatingActive;
    }

    public boolean canChangeTemp() {
        return canChangeTemp;
    }

    public double getCurrentTemp() {
        return currentTemp;
    }

    public Double getTargetTemp() {
        return targetTemp;
    }

    public boolean fanActive() {
        return fanActive;
    }

    public boolean powerStatus() {
        return powerStatus;
    }

    public boolean windowOpen() {
        return windowOpen;
    }

    public void setTargetTemp(final Double targetTemp) {
        this.targetTemp = targetTemp;
    }

    public void setFanActive(final boolean fanActive) {
        this.fanActive = fanActive;
    }

    public void setPowerStatus(final boolean powerStatus) {
        this.powerStatus = powerStatus;
    }
}
