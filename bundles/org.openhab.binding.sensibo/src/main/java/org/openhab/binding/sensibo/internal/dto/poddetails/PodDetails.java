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
