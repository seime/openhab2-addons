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

import org.openhab.binding.millheat.internal.dto.DeviceDTO;

/**
 * The {@link Heater} represents a heater, either connected to a room or independent
 *
 * @author Arne Seime - Initial contribution
 */
public class Heater {
    private Room room;
    private String id;
    private String name;
    private String macAddress;
    private boolean heatingActive;
    private boolean canChangeTemp = true;
    private int subDomain;
    private int currentTemp;
    private Integer targetTemp;
    private boolean fanActive;
    private boolean powerStatus;
    private boolean windowOpen;

    public Heater(DeviceDTO dto) {
        id = String.valueOf(dto.deviceId);
        name = dto.deviceName;
        macAddress = dto.macAddress;
        heatingActive = dto.heaterFlag;
        canChangeTemp = dto.holiday;
        subDomain = dto.subDomainId;
        currentTemp = (int) dto.currentTemp;
        setTargetTemp(dto.holidayTemp);
        setFanActive(dto.fanStatus);
        setPowerStatus(dto.powerStatus);
        windowOpen = dto.openWindow;
    }

    public Heater(DeviceDTO dto, Room room) {
        this.room = room;
        id = String.valueOf(dto.deviceId);
        name = dto.deviceName;
        macAddress = dto.macAddress;
        heatingActive = dto.heaterFlag;
        canChangeTemp = dto.canChangeTemp;
        subDomain = dto.subDomainId;
        currentTemp = (int) dto.currentTemp;
        if (room != null && room.getMode() != null) {
            switch (room.getMode()) {
                case Comfort:
                    setTargetTemp(room.getComfortTemp());
                    break;
                case Sleep:
                    setTargetTemp(room.getSleepTemp());
                    break;
                case Away:
                    setTargetTemp(room.getAwayTemp());
                    break;
                case Off:
                    setTargetTemp(null);
                default:
                    // TODO
            }
        }
        setFanActive(dto.fanStatus);
        setPowerStatus(dto.powerStatus);
        windowOpen = dto.openWindow;
    }

    @Override
    public String toString() {
        return "Heater [room=" + room + ", id=" + id + ", name=" + name + ", macAddress=" + macAddress
                + ", heatingActive=" + heatingActive + ", canChangeTemp=" + canChangeTemp + ", subDomain=" + subDomain
                + ", currentTemp=" + currentTemp + ", targetTemp=" + getTargetTemp() + ", fanActive=" + isFanActive()
                + ", powerStatus=" + isPowerStatus() + ", windowOpen=" + windowOpen + "]";
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

    public boolean isCanChangeTemp() {
        return canChangeTemp;
    }

    public int getSubDomain() {
        return subDomain;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public Integer getTargetTemp() {
        return targetTemp;
    }

    public boolean isFanActive() {
        return fanActive;
    }

    public boolean isPowerStatus() {
        return powerStatus;
    }

    public boolean isWindowOpen() {
        return windowOpen;
    }

    public void setTargetTemp(Integer targetTemp) {
        this.targetTemp = targetTemp;
    }

    public void setFanActive(boolean fanActive) {
        this.fanActive = fanActive;
    }

    public void setPowerStatus(boolean powerStatus) {
        this.powerStatus = powerStatus;
    }

}
