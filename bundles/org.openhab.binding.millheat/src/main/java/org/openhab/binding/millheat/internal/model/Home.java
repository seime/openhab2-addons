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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.millheat.internal.dto.HomeDTO;

/**
 * The {@link Home} represents a home
 *
 * @author Arne Seime - Initial contribution
 */
public class Home {
    private String id;
    private String name;
    private int type;
    private String timezone;
    private int holidayTemp;
    private Mode mode;
    private String program = null;
    private List<Room> rooms = new ArrayList<>();
    private List<Heater> independentHeaters = new ArrayList<>();

    public Home(HomeDTO dto) {
        id = String.valueOf(dto.homeId);
        name = dto.name;
        type = dto.homeType;
        timezone = dto.timeZone;
        holidayTemp = dto.holidayTemp;
        if (dto.holiday) {
            LocalDateTime modeStart = LocalDateTime.ofEpochSecond(dto.holidayStartTime, 0, ZoneOffset.of(timezone));
            LocalDateTime modeEnd = LocalDateTime.ofEpochSecond(dto.holidayEndTime, 0, ZoneOffset.of(timezone));
            mode = new Mode(ModeType.AdvancedAway, modeStart, modeEnd);
        } else if (dto.alwaysHome) {
            mode = new Mode(ModeType.AlwaysHome, null, null);
        } else {
            LocalDateTime modeStart = LocalDateTime.ofEpochSecond(dto.modeStartTime, 0, ZoneOffset.of(timezone));
            LocalDateTime modeEnd = modeStart.withHour(dto.modeHour).withMinute(dto.modeMinute);
            mode = new Mode(ModeType.valueOf(dto.currentMode), modeStart, modeEnd);
        }
    }

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public void addHeater(Heater heater) {
        independentHeaters.add(heater);
    }

    @Override
    public String toString() {
        return "Home [id=" + id + ", name=" + name + ", type=" + type + ", timezone=" + timezone + ", holidayTemp="
                + holidayTemp + ", mode=" + mode + ", rooms=" + rooms + ", independentHeaters=" + independentHeaters
                + ", program=" + program + "]";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getTimezone() {
        return timezone;
    }

    public int getHolidayTemp() {
        return holidayTemp;
    }

    public Mode getMode() {
        return mode;
    }

    public String getProgram() {
        return program;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Heater> getIndependentHeaters() {
        return independentHeaters;
    }
}
