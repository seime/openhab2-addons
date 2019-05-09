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
package org.openhab.binding.sensibo.internal.model;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;

/**
 * @author Arne Seime - Initial contribution
 */
public class AcState {
    private boolean on;
    private String fanLevel;
    private @NonNull Unit<@NonNull Temperature> temperatureUnit;
    private int targetTemperature;
    private String mode;
    private String swing;

    public AcState(final org.openhab.binding.sensibo.internal.dto.poddetails.AcState dto) {
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

    public AcState(final AcState original) {
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

    public void setOn(final boolean on) {
        this.on = on;
    }

    public void setFanLevel(final String fanLevel) {
        this.fanLevel = fanLevel;
    }

    public void setTemperatureUnit(final Unit<Temperature> temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public void setTargetTemperature(final int targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public void setMode(final String mode) {
        this.mode = mode;
    }

    public void setSwing(final String swing) {
        this.swing = swing;
    }

}
