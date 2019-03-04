package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

public class SelectDeviceByRoomRequest extends AbstractRequest {

    public SelectDeviceByRoomRequest(Long roomId, String timeZone) {
        super();
        this.roomId = roomId;
        this.timeZone = timeZone;
    }

    public Long roomId;

    @SerializedName("timeZoneNum")
    public String timeZone;

    @Override
    public String getRequestUrl() {
        return "selectDevicebyRoom";
    }

}
