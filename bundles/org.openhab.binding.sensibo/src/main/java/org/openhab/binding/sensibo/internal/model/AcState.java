package org.openhab.binding.sensibo.internal.model;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;

public class AcState {
    private boolean on;
    private String fanLevel;
    private @NonNull Unit<@NonNull Temperature> temperatureUnit;
    private int targetTemperature;
    private String mode;
    private String swing;

    public AcState(org.openhab.binding.sensibo.internal.dto.poddetails.AcState dto) {

        this.on = dto.isOn();
        this.fanLevel = dto.getFanLevel();
        this.targetTemperature = dto.getTargetTemperature();
        this.mode = dto.getMode();
        this.swing = dto.getSwing();

        switch (dto.getTemperatureUnit()) {
            case "C":
                this.temperatureUnit = SIUnits.CELSIUS;
                break;
            case "F":
                this.temperatureUnit = ImperialUnits.FAHRENHEIT;
                break;
            default:
                throw new IllegalArgumentException("Do not understand temperature unit " + temperatureUnit);
        }
    }

    public AcState(AcState original) {
        this.on = original.isOn();
        this.fanLevel = original.getFanLevel();
        this.targetTemperature = original.getTargetTemperature();
        this.mode = original.getMode();
        this.swing = original.getSwing();
        this.temperatureUnit = original.temperatureUnit;

    }

    @Override
    public AcState clone() {
        return new AcState(this);
    }

    public boolean isOn() {
        return on;
    }

    public String getFanLevel() {
        return fanLevel;
    }

    public @NonNull Unit<@NonNull Temperature> getTemperatureUnit() {
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

    public void setOn(boolean on) {
        this.on = on;
    }

    public void setFanLevel(String fanLevel) {
        this.fanLevel = fanLevel;
    }

    public void setTemperatureUnit(Unit<Temperature> temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public void setTargetTemperature(int targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setSwing(String swing) {
        this.swing = swing;
    }

}
