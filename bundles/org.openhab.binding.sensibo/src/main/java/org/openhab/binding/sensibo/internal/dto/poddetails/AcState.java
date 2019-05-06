package org.openhab.binding.sensibo.internal.dto.poddetails;

public class AcState {
    private boolean on;
    private String fanLevel;
    private String temperatureUnit;
    private int targetTemperature;
    private String mode;
    private String swing;

    public boolean isOn() {
        return on;
    }

    public String getFanLevel() {
        return fanLevel;
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    public int getTargetTemperature() {
        return targetTemperature;
    }

    public String getMode() {
        return mode;
    }

    public String getSwing() {
        return swing;
    }

}
