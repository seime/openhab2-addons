/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.panasoniccomfortcloud.internal.model;

import org.openhab.binding.panasoniccomfortcloud.internal.dto.ParametersDTO;

/**
 * @author Arne Seime - Initial contribution
 */
public class Parameters {

    private AirSwingUpDown swingUpDown;
    private AirSwingSideways airSwingSideways;
    private OperationMode mode;
    private EcoMode ecoMode;
    private AirSwingAutoMode airSwingAutoMode;
    private FanSpeed fanSpeed;
    private NanoeMode nanoeMode;
    private NanoeMode actualNanoeMode;
    private Double targetTemperature;

    private Integer ecoFunctionData;
    private Integer lastSettingMode;
    private Integer ecoNavi;
    private Integer iAuto;
    private Integer airQuality;
    private Integer insideTemperature;
    private Integer outsideTemperature;
    private boolean masterSwitch;
    private Integer airDirection;

    /**
     * Parse from wire format
     */
    public Parameters(ParametersDTO dto) {
        swingUpDown = AirSwingUpDown.parseValue(dto.airSwingUD);
        airSwingSideways = AirSwingSideways.parseValue(dto.airSwingLR);
        mode = OperationMode.parseValue(dto.operationMode);
        ecoMode = EcoMode.parseValue(dto.ecoMode);
        airSwingAutoMode = AirSwingAutoMode.parseValue(dto.fanAutoMode);
        fanSpeed = FanSpeed.parseValue(dto.fanSpeed);
        nanoeMode = NanoeMode.parseValue(dto.nanoe);
        actualNanoeMode = NanoeMode.parseValue(dto.actualNanoe);
        masterSwitch = dto.operate == null || dto.operate == 0 ? false : true;
        targetTemperature = dto.temperatureSet;
        insideTemperature = dto.insideTemperature;
        outsideTemperature = dto.outTemperature;

        this.airQuality = dto.airQuality;
        this.ecoNavi = dto.ecoNavi;
        this.iAuto = dto.iAuto;
        this.airDirection = dto.airDirection;
        this.lastSettingMode = dto.lastSettingMode;
        this.ecoFunctionData = dto.ecoFunctionData;
    }

    /**
     * Convert to wire format
     */
    public ParametersDTO toParametersDTO(Device device) {
        ParametersDTO dto = new ParametersDTO();
        if (!device.getFeatureSet().getSupportedSwingUpDownModes().isEmpty()) {
            dto.airSwingUD = swingUpDown.value;
        }

        if (!device.getFeatureSet().getSupportedSwingSidewayModes().isEmpty()) {
            dto.airSwingLR = airSwingSideways.value;
        }

        dto.operationMode = mode.value;
        dto.ecoMode = ecoMode.value;
        dto.fanAutoMode = airSwingAutoMode.value;
        dto.fanSpeed = fanSpeed.value;

        if (device.getFeatureSet().isNanoe()) {
            dto.nanoe = nanoeMode.value;
            dto.actualNanoe = actualNanoeMode.value;
        }
        dto.operate = masterSwitch ? 1 : 0;
        dto.temperatureSet = targetTemperature;

        if (device.getFeatureSet().isEcoNavi()) {
            dto.ecoNavi = ecoNavi;
        }

        if (device.getFeatureSet().isiAutoX()) {
            dto.iAuto = iAuto;
        }

        dto.airDirection = airDirection;

        return dto;
    }

    public AirSwingUpDown getSwingUpDown() {
        return swingUpDown;
    }

    public AirSwingSideways getSwingSideways() {
        return airSwingSideways;
    }

    public OperationMode getMode() {
        return mode;
    }

    public EcoMode getEcoMode() {
        return ecoMode;
    }

    public AirSwingAutoMode getFanAutoMode() {
        return airSwingAutoMode;
    }

    public FanSpeed getFanSpeed() {
        return fanSpeed;
    }

    public NanoeMode getNanoeMode() {
        return nanoeMode;
    }

    public NanoeMode getActualNanoeMode() {
        return actualNanoeMode;
    }

    public Double getTargetTemperature() {
        return targetTemperature;
    }

    public Integer getEcoFunctionData() {
        return ecoFunctionData;
    }

    public Integer getLastSettingMode() {
        return lastSettingMode;
    }

    public Integer getEcoNavi() {
        return ecoNavi;
    }

    public Integer getiAuto() {
        return iAuto;
    }

    public Integer getAirQuality() {
        return airQuality;
    }

    public Integer getInsideTemperature() {
        return insideTemperature;
    }

    public Integer getOutsideTemperature() {
        return outsideTemperature;
    }

    public boolean isMasterSwitch() {
        return masterSwitch;
    }

    public Integer getAirDirection() {
        return airDirection;
    }

    public void setSwingUpDown(AirSwingUpDown airSwingUpDown) {
        this.swingUpDown = airSwingUpDown;
    }

    public void setSwingSideways(AirSwingSideways airSwingSideways) {
        this.airSwingSideways = airSwingSideways;
    }

    public void setMode(OperationMode mode) {
        this.mode = mode;
    }

    public void setEcoMode(EcoMode ecoMode) {
        this.ecoMode = ecoMode;
    }

    public void setFanAutoMode(AirSwingAutoMode airSwingAutoMode) {
        this.airSwingAutoMode = airSwingAutoMode;
    }

    public void setFanSpeed(FanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public void setNanoeMode(NanoeMode nanoeMode) {
        this.nanoeMode = nanoeMode;
    }

    public void setActualNanoeMode(NanoeMode actualNanoeMode) {
        this.actualNanoeMode = actualNanoeMode;
    }

    public void setTargetTemperature(Double targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public void setMasterSwitch(boolean masterSwitch) {
        this.masterSwitch = masterSwitch;
    }

    public void setAirDirection(Integer airDirection) {
        this.airDirection = airDirection;
    }
}
