package org.openhab.binding.millheat.internal.model;

import org.openhab.binding.millheat.internal.dto.DeviceDTO;

public class Heater {

    @Override
    public String toString() {
        return "Heater [room=" + room + ", id=" + id + ", name=" + name + ", macAddress=" + macAddress
                + ", heatingActive=" + heatingActive + ", canChangeTemp=" + canChangeTemp + ", subDomain=" + subDomain
                + ", currentTemp=" + currentTemp + ", targetTemp=" + targetTemp + ", fanActive=" + fanActive
                + ", powerStatus=" + powerStatus + ", windowOpen=" + windowOpen + "]";
    }

    public Room room;
    public String id;

    public String name;
    public String macAddress;
    public boolean heatingActive;
    public boolean canChangeTemp = true;
    public int subDomain;
    public int currentTemp;
    public Integer targetTemp;
    public boolean fanActive;
    public boolean powerStatus;
    public boolean windowOpen;

    public Heater(DeviceDTO dto) {
        id = String.valueOf(dto.deviceId);
        name = dto.deviceName;
        macAddress = dto.macAddress;
        heatingActive = dto.heaterFlag;
        canChangeTemp = dto.holiday;
        subDomain = dto.subDomainId;
        currentTemp = (int) dto.currentTemp;
        targetTemp = dto.holidayTemp;
        fanActive = dto.fanStatus;
        powerStatus = dto.powerStatus;
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

        switch (room.mode) {
            case Comfort:
                targetTemp = room.comfortTemp;
                break;
            case Sleep:
                targetTemp = room.sleepTemp;
                break;
            case Away:
                targetTemp = room.awayTemp;
                break;
            case Off:
                targetTemp = null;
            default:
                // TODO
        }
        fanActive = dto.fanStatus;
        powerStatus = dto.powerStatus;
        windowOpen = dto.openWindow;

    }
}
