package org.openhab.binding.millheat.internal.dto;

import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.Room;

public class SetRoomTempRequest extends AbstractRequest {

    public long roomId;
    public int comfortTemp;
    public int sleepTemp;
    public int awayTemp;
    public int homeType;

    public SetRoomTempRequest(Home home, Room room) {
        roomId = Long.parseLong(room.id);
        homeType = home.type;
        comfortTemp = room.comfortTemp;
        sleepTemp = room.sleepTemp;
        awayTemp = room.awayTemp;
    }

    @Override
    public String getRequestUrl() {
        return "changeRoomModeTempInfo";
    }

}
