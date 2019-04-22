/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.millheat.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.millheat.internal.dto.RoomDTO;

/**
 * The {@link Room} represents a room in a home as designed by the end user in the Millheat app.
 *
 * @author Arne Seime - Initial contribution
 */
public class Room {
    private Home home;
    private String id;
    private String name;
    private int currentTemp;
    private int comfortTemp;
    private int sleepTemp;
    private int awayTemp;
    private boolean heatingActive;
    private ModeType mode;
    private String roomProgramName;
    private List<Heater> heaters = new ArrayList<>();

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
        roomProgramName = dto.roomProgram;
    }

    public void addHeater(Heater h) {
        heaters.add(h);
    }

    public List<Heater> getHeaters() {
        return heaters;
    }

    public Integer getTargetTemperature() {
        switch (mode) {
            case AdvancedAway:
                return home.getHolidayTemp();
            case Sleep:
                return sleepTemp;
            case Comfort:
                return comfortTemp;
            case Away:
                return awayTemp;
            case Off:
            case AlwaysHome:
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "Room [home=" + home.getId() + ", id=" + id + ", name=" + name + ", currentTemp=" + currentTemp
                + ", comfortTemp=" + comfortTemp + ", sleepTemp=" + sleepTemp + ", awayTemp=" + awayTemp
                + ", heatingActive=" + heatingActive + ", mode=" + mode + ", roomProgramName=" + roomProgramName
                + ", heaters=" + heaters + "]";
    }

    public Home getHome() {
        return home;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getComfortTemp() {
        return comfortTemp;
    }

    public int getSleepTemp() {
        return sleepTemp;
    }

    public int getAwayTemp() {
        return awayTemp;
    }

    public boolean isHeatingActive() {
        return heatingActive;
    }

    public ModeType getMode() {
        return mode;
    }

    public String getRoomProgramName() {
        return roomProgramName;
    }
}
