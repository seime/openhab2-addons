package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

public class DeviceDTO {

    public boolean heaterFlag;
    public int subDomainId;
    public int controlType;
    public double currentTemp;
    public boolean canChangeTemp;
    public int deviceId;
    public String deviceName;
    @SerializedName("mac")
    public String macAddress;
    public int deviceStatus;
    public int holidayTemp = 0;
    public boolean fanStatus;
    @SerializedName("open")
    public boolean openWindow;
    public boolean powerStatus;
    @SerializedName("isHoliday")
    public boolean holiday;
}
