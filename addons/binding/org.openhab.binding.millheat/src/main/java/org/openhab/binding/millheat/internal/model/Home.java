package org.openhab.binding.millheat.internal.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.openhab.binding.millheat.internal.dto.HomeDTO;

public class Home {
    public String id;

    public String name;
    public int type;
    public String timezone;

    public Mode mode;

    public Room[] rooms = new Room[0];

    public Heater[] independentHeaters = new Heater[0];
    // TODO
    public Program program = null;

    public Home(HomeDTO dto) {
        id = String.valueOf(dto.homeId);
        name = dto.name;
        type = dto.homeType;
        timezone = dto.timeZone;

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
}
