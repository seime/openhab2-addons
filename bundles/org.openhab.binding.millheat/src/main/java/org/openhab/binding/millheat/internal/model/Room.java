/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Locale;

import org.openhab.binding.millheat.internal.dto.RoomDTO;

/**
 * The {@link Room} represents a room in a home as designed by the end user in the Millheat app.
 *
 * @author Arne Seime - Initial contribution
 */
public class Room {
    private final Home home;
    private final String id;
    private final String name;
    private final Double currentTemp;
    private final double comfortTemp;
    private final double sleepTemp;
    private final double awayTemp;
    private final boolean heatingActive;
    private final ModeType mode;
    private final String roomProgramName;
    private final List<Heater> heaters = new ArrayList<>();

    private boolean online;

    public Room(final RoomDTO dto, final Home home) {
        this.home = home;
        id = dto.id;
        name = dto.name;
        currentTemp = dto.currentTemp;
        comfortTemp = dto.roomComfortTemperature;
        sleepTemp = dto.roomSleepTemperature;
        awayTemp = dto.roomAwayTemperature;
        heatingActive = dto.heatStatus;
        mode = ModeType.valueOf(dto.activeModeFromWeeklyProgram.toUpperCase(Locale.ROOT));
        roomProgramName = dto.roomProgram;
        online = dto.online;
    }

    public void addHeater(final Heater h) {
        heaters.add(h);
    }

    public List<Heater> getHeaters() {
        return heaters;
    }

    public Double getTargetTemperature() {
        switch (mode) {
            case VACATION:
                return home.getHolidayTemp();
            case SLEEP:
                return sleepTemp;
            case COMFORT:
                return comfortTemp;
            case AWAY:
                return awayTemp;
            case OFF:
            case ALWAYSHOME:
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

    public Double getCurrentTemp() {
        return currentTemp;
    }

    public double getComfortTemp() {
        return comfortTemp;
    }

    public double getSleepTemp() {
        return sleepTemp;
    }

    public double getAwayTemp() {
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

    public boolean isOnline() {
        return online;
    }
}
