package org.openhab.binding.millheat.internal.model;

import java.util.Arrays;

import org.openhab.binding.millheat.internal.dto.RoomDTO;

public class Room {
    public Home home;
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

    public Room(RoomDTO dto, Home home) {
        this.home = home;
        id = String.valueOf(dto.roomId);
        name = dto.name;
        currentTemp = (int) dto.currentTemp;

        comfortTemp = dto.comfortTemp;
        sleepTemp = dto.sleepTemp;
        awayTemp = dto.awayTemp;

        heatingActive = dto.heatStatus;
        mode = ModeType.valueOf(dto.currentMode);

        roomProgram = new Program(dto.roomProgram);

    }

    public int getTargetTemperature() {
        switch (mode) {
            case AdvancedAway:
                return home.holidayTemp;
            case Sleep:
                return sleepTemp;
            case Comfort:
                return comfortTemp;
            case Away:
                return awayTemp;
            case AlwaysHome:
            case Off:
            default:
                return 0; // TODO

        }
    }

    @Override
    public String toString() {
        return "Room [home=" + home.id + ", id=" + id + ", name=" + name + ", currentTemp=" + currentTemp
                + ", comfortTemp=" + comfortTemp + ", sleepTemp=" + sleepTemp + ", awayTemp=" + awayTemp
                + ", heatingActive=" + heatingActive + ", mode=" + mode + ", roomProgram=" + roomProgram + ", heaters="
                + Arrays.toString(heaters) + "]";
    }
}
