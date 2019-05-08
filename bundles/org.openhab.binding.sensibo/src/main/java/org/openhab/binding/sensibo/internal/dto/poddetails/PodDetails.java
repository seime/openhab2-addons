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

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class PodDetails {
    private String id;
    private String macAddress;
    private String firmwareVersion;
    private String firmwareType;
    @SerializedName("serial")
    private String serialNumber;
    private String temperatureUnit;
    private String productModel;
    private Boolean smartMode;
    private AcState acState;
    @SerializedName("measurements")
    private Measurement lastMeasurement;
    private ModeCapabilityWrapper remoteCapabilities;
    private ConnectionStatus connectionStatus;

    public String getMacAddress() {
        return macAddress;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getFirmwareType() {
        return firmwareType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    public String getProductModel() {
        return productModel;
    }

    public Boolean getSmartMode() {
        return smartMode;
    }

    public AcState getAcState() {
        return acState;
    }

    public Measurement getLastMeasurement() {
        return lastMeasurement;
    }

    public Map<String, ModeCapability> getRemoteCapabilities() {
        return remoteCapabilities.getModes();
    }

    public boolean isAlive() {
        return connectionStatus.isAlive();
    }

    public String getId() {
        return id;
    }
}
