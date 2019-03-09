package org.openhab.binding.millheat.internal.model;

import org.openhab.binding.millheat.internal.dto.RoomDTO;

public class Room {
    public String id;

    public String name;
    public int currentTemp;

    public int comfortTemp;

    public int sleepTemp;
    public int awayTemp;
    public boolean heatingActive;

    public ModeType mode;

    // TODO
    public Program roomProgram;

    public Heater[] heaters = new Heater[0];

    public Room(RoomDTO dto) {
        id = String.valueOf(dto.roomId);
        name = dto.name;
        currentTemp = (int) dto.currentTemp;

        comfortTemp = dto.comfortTemp;
        sleepTemp = dto.sleepTemp;
        awayTemp = dto.awayTemp;

        heatingActive = dto.heatStatus;
        mode = ModeType.valueOf(dto.currentMode);

    }
}
