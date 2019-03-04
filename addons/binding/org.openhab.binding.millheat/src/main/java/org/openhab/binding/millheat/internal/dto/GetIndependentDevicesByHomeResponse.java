package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

public class GetIndependentDevicesByHomeResponse extends AbstractResponse {
    @SerializedName("deviceInfo")
    public DeviceDTO devices[] = new DeviceDTO[0];
}
