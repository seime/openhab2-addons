package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

public class HomeDTO {

    public Long homeId;

    @SerializedName("homeAlways")
    public boolean alwaysHome;

    @SerializedName("homeName")
    public String name;

    @SerializedName("isHoliday")
    public boolean holiday;

    public Long holidayStartTime;

    public String timeZone;

    public Integer modeMinute;

    public Long modeStartTime;

    public Integer holidayTemp;

    public Integer modeHour;

    public Integer currentMode;

    public Long holidayEndTime;

    public Integer homeType;

    public String programId;

}
