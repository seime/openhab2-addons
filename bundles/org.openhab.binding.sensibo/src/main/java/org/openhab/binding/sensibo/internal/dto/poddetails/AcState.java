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
    private String temperatureUnit;
    private int targetTemperature;
    private String mode;
    private String swing;

    public AcState(boolean on, String fanLevel, String temperatureUnit, int targetTemperature, String mode,
            String swing) {
        this.on = on;
        this.fanLevel = fanLevel;
        this.temperatureUnit = temperatureUnit;
        this.targetTemperature = targetTemperature;
        this.mode = mode;
        this.swing = swing;
    }

    public AcState() {
    }

    public AcState(org.openhab.binding.sensibo.internal.model.AcState acState) {
        this.on = acState.isOn();
        this.fanLevel = acState.getFanLevel();
        this.targetTemperature = acState.getTargetTemperature();
        this.mode = acState.getMode();
        this.swing = acState.getSwing();

        Unit<@NonNull Temperature> unit = acState.getTemperatureUnit();

        if (unit.equals(SIUnits.CELSIUS)) {
            this.temperatureUnit = "C";
        } else if (unit.equals(ImperialUnits.FAHRENHEIT)) {
            this.temperatureUnit = "F";
        } else {
            throw new IllegalArgumentException("Unexpected temperature unit " + unit);
        }

    }

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
