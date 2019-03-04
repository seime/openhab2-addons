package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

public class SelectRoomByHomeResponse extends AbstractResponse {

    @SerializedName("roomInfo")
    public RoomDTO[] rooms = new RoomDTO[0];

}
