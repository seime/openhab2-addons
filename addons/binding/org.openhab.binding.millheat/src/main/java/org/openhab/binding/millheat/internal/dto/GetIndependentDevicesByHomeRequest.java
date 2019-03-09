package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

public class GetIndependentDevicesByHomeRequest extends AbstractRequest {

    public Long homeId;

    @SerializedName("timeZoneNum")
    public String timeZone;

    public GetIndependentDevicesByHomeRequest(Long homeId, String timeZone) {
        super();
        this.homeId = homeId;
        this.timeZone = timeZone;
    }

    @Override
    public String getRequestUrl() {
        return "getIndependentDevices";
    }

}
