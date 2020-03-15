/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.openhab.binding.sensibo.internal.SensiboTemperatureUnitParser;

/**
 *
 * @author Arne Seime - Initial contribution
 */
public class AcState {
    private boolean on;
    private String fanLevel;
    private Unit<Temperature> temperatureUnit;
    private Integer targetTemperature;
    private String mode;
    private String swing;

    public AcState(final org.openhab.binding.sensibo.internal.dto.poddetails.AcState dto) {
        this.on = dto.on;
        this.fanLevel = dto.fanLevel;
        this.targetTemperature = dto.targetTemperature;
        this.mode = dto.mode;
        this.swing = dto.swing;
        this.temperatureUnit = SensiboTemperatureUnitParser.parse(dto.temperatureUnit);
    }

    public AcState(final AcState original) {
        this.on = original.on;
        this.fanLevel = original.fanLevel;
        this.targetTemperature = original.targetTemperature;
        this.mode = original.mode;
        this.swing = original.swing;
        this.temperatureUnit = original.temperatureUnit;
    }

    @Override
    public AcState clone() {
        return new AcState(this);
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(final boolean on) {
        this.on = on;
    }

    public String getFanLevel() {
        return fanLevel;
    }

    public void setFanLevel(final String fanLevel) {
        this.fanLevel = fanLevel;
    }

    public Unit<Temperature> getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(final Unit<Temperature> temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public Integer getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(final Integer targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(final String mode) {
        this.mode = mode;
    }

    public String getSwing() {
        return swing;
    }

    public void setSwing(final String swing) {
        this.swing = swing;
    }
}
