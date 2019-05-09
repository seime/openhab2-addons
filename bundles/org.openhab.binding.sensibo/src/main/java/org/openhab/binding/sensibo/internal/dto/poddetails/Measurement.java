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
package org.openhab.binding.sensibo.internal.dto.poddetails;

import java.time.ZonedDateTime;

import com.google.gson.annotations.SerializedName;

/**
 * @author Arne Seime - Initial contribution
 */
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
