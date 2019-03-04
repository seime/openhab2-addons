package org.openhab.binding.millheat.internal.model;

import org.openhab.binding.millheat.internal.dto.DeviceDTO;

public class Heater {
    public Heater(DeviceDTO dto) {
        id = String.valueOf(dto.deviceId);
        name = dto.deviceName;
        macAddress = dto.macAddress;
        heatingActive = dto.heaterFlag;
        canChangeTemp = dto.canChangeTemp;
        subDomain = dto.subDomainId;
        currentTemp = (int) dto.currentTemp;
    }

    public String id;
    public String name;
    public String macAddress;
    public boolean heatingActive;
    public boolean canChangeTemp;
    public int subDomain;
    public int currentTemp;
}
