package org.openhab.binding.sensibo.internal.dto.poddetails;

import java.time.ZonedDateTime;

import com.google.gson.annotations.SerializedName;

public class Measurement {
    private Double batteryVoltage;
    private Double temperature;
    private Double humidity;
    @SerializedName("rssi")
    private Integer wifiSignalStrength;
    @SerializedName("time")
    private TimeWrapper measurementTimestamp;

    public Double getBatteryVoltage() {
        return batteryVoltage;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public ZonedDateTime getMeasurementTimestamp() {
        if (measurementTimestamp != null) {
            return measurementTimestamp.getTime();
        }
        return null;
    }

    public Integer getWifiSignalStrength() {
        return wifiSignalStrength;
    }

}
