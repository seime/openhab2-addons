package org.openhab.binding.sensibo.internal.model;

import java.time.LocalTime;
import java.time.ZonedDateTime;

public class Schedule {
    public LocalTime targetLocalTime;
    public ZonedDateTime nextTime;
    public String[] recurringDays;
    public AcState acState;
    public boolean enabled;

    public Schedule(org.openhab.binding.sensibo.internal.dto.poddetails.Schedule dto) {
        this.targetLocalTime = LocalTime.parse(dto.targetLocalTime);
        this.nextTime = ZonedDateTime.parse(nextTime + "Z"); // API field seems to be in Zulu
        this.recurringDays = dto.recurringDays;
        this.acState = new AcState(dto.acState);
        this.enabled = dto.enabled;
    }
}
