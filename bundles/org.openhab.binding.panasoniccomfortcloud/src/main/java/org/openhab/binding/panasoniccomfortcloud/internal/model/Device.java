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

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.openhab.binding.panasoniccomfortcloud.internal.dto.DeviceDTO;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;

/**
 * @author Arne Seime - Initial contribution
 */
public class Device {

    private Group group;
    private String deviceId;
    private String type; // Figure out what this is
    private String name;
    private Integer permission; // Figure out what this is
    private Integer summerhouse; // Figure out what this is
    private String deviceModel;

    private FeatureSet featureSet;

    private Unit temperatureUnit;
    /**
     * TODO modeAvlList - figure out what it is
     * * "modeAvlList": {
     * "autoMode": 1,
     * "fanMode": 1
     * },
     */
    private Boolean coordinableFlg;
    private Boolean pairedFlg;

    private TemperatureRange dryRange;
    private TemperatureRange heatRange;
    private TemperatureRange coolRange;
    private TemperatureRange autoRange;

    private Instant lastUpdated;

    private Parameters currentParameters;

    public Device(Group group) {
        this.group = group;
    }

    public void mergeFromGroupList(DeviceDTO dto) {
        this.deviceId = dto.deviceGuid;
        this.type = dto.deviceType;
        this.deviceModel = dto.deviceModuleNumber;
        this.name = dto.deviceName;
    }

    public void mergeFromDeviceDetails(DeviceDTO dto) {
        this.featureSet = new FeatureSet(dto);

        this.permission = dto.permission;
        this.summerhouse = dto.summerHouse;
        this.temperatureUnit = dto.temperatureUnit == 0 ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT;

        currentParameters = new Parameters(dto.parameters);

        this.dryRange = new TemperatureRange(dto.dryTempMin, dto.dryTempMax);
        this.heatRange = new TemperatureRange(dto.heatTempMin, dto.heatTempMax);
        this.autoRange = new TemperatureRange(dto.autoTempMin, dto.autoTempMax);
        this.coolRange = new TemperatureRange(dto.coolTempMin, dto.coolTempMax);

        this.lastUpdated = Instant.ofEpochMilli(dto.timestamp);
    }

    public boolean isAlive() {
        return true; // TODO Check if device actually is reachable by servers
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Integer getPermission() {
        return permission;
    }

    public Integer getSummerhouse() {
        return summerhouse;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public FeatureSet getFeatureSet() {
        return featureSet;
    }

    public Unit getTemperatureUnit() {
        return temperatureUnit;
    }

    public Boolean getCoordinableFlg() {
        return coordinableFlg;
    }

    public Boolean getPairedFlg() {
        return pairedFlg;
    }

    public TemperatureRange getDryRange() {
        return dryRange;
    }

    public TemperatureRange getHeatRange() {
        return heatRange;
    }

    public TemperatureRange getCoolRange() {
        return coolRange;
    }

    public TemperatureRange getAutoRange() {
        return autoRange;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public Parameters getCurrentParameters() {
        return currentParameters;
    }

    public Map<String, String> getThingProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("deviceId", deviceId);
        properties.put("name", name);
        properties.put("group", group.getName());
        properties.put("model", deviceModel);
        if (featureSet != null) {
            properties.put("nanoe", String.valueOf(featureSet.isNanoe()));
            properties.put("iAutoX", String.valueOf(featureSet.isiAutoX()));
            properties.put("nanoeStandalone", String.valueOf(featureSet.isNanoeStandAlone()));
            properties.put("ecoNavi", String.valueOf(featureSet.isEcoNavi()));
            properties.put("supportedOperationModes", String.valueOf(featureSet.getSupportedOperationModes()));
            properties.put("supportedEcoModes", String.valueOf(featureSet.getSupportedEcoModes()));
            properties.put("supportedAirSwingHorizontal", String.valueOf(featureSet.getSupportedSwingSidewayModes()));
            properties.put("supportedAirSwingVertical", String.valueOf(featureSet.getSupportedSwingUpDownModes()));
        }

        properties.put("supportedFanSpeeds", String.valueOf(Arrays.asList(FanSpeed.values())));
        properties.put("supportedAirSwingAutoModes", String.valueOf(Arrays.asList(AirSwingAutoMode.values())));

        if (summerhouse != null)
            properties.put("summerHouse", String.valueOf(summerhouse));
        if (dryRange != null)
            properties.put("allowedTemperatureRangeDryMode", dryRange.toString());
        if (heatRange != null)
            properties.put("allowedTemperatureRangeHeatMode", heatRange.toString());
        if (autoRange != null)
            properties.put("allowedTemperatureRangeAutoMode", autoRange.toString());
        if (coolRange != null)
            properties.put("allowedTemperatureRangeCoolMode", coolRange.toString());

        return properties;
    }
}
