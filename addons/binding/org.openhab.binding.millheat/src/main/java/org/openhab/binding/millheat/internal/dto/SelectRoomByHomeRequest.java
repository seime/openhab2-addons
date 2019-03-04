package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

public class SelectRoomByHomeRequest extends AbstractRequest {

    public Long homeId;

    @SerializedName("timeZoneNum")
    public String timeZone;

    public SelectRoomByHomeRequest(Long homeId, String timeZone) {
        super();
        this.homeId = homeId;
        this.timeZone = timeZone;
    }

    @Override
    public String getRequestUrl() {
        return "selectRoombyHome";
    }

}
