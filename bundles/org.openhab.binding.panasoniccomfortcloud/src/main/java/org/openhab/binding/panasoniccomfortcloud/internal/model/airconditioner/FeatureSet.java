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
package org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.openhab.binding.panasoniccomfortcloud.internal.dto.DeviceDTO;

/**
 * @author Arne Seime - Initial contribution
 */
public class FeatureSet {
    private boolean iAutoX;
    private boolean nanoe;
    private boolean nanoeStandAlone;
    private Set<OperationMode> supportedOperationModes;
    private Set<EcoMode> supportedEcoModes;
    private Set<AirSwingSideways> supportedSwingSidewayModes;
    private Set<AirSwingUpDown> supportedAirSwingUpDownModes;

    private boolean ecoNavi;
    private Integer ecoFunction;

    public FeatureSet(DeviceDTO dto) {
        supportedOperationModes = new TreeSet<>();
        // Main operation mode

        if (Boolean.TRUE.equals(dto.fanMode)) {
            supportedOperationModes.add(OperationMode.FAN);
        }
        if (Boolean.TRUE.equals(dto.dryMode)) {
            supportedOperationModes.add(OperationMode.DRY);
        }
        if (Boolean.TRUE.equals(dto.autoMode)) {
            supportedOperationModes.add(OperationMode.AUTO);
        }
        if (Boolean.TRUE.equals(dto.coolMode)) {
            supportedOperationModes.add(OperationMode.COOL);
        }
        if (Boolean.TRUE.equals(dto.heatMode)) {
            supportedOperationModes.add(OperationMode.HEAT);
        }

        // Eco modes
        supportedEcoModes = new TreeSet<>();
        supportedEcoModes.add(EcoMode.AUTO);
        if (Boolean.TRUE.equals(dto.powerfulMode)) {
            supportedEcoModes.add(EcoMode.POWERFUL);
        }
        if (Boolean.TRUE.equals(dto.quietMode)) {
            supportedEcoModes.add(EcoMode.QUIET);
        }

        // Sideways swing modes
        supportedSwingSidewayModes = new TreeSet<>();
        if (Boolean.TRUE.equals(dto.autoSwingUD)) { // Note: Seems to be switched with airSwingLR
            supportedSwingSidewayModes.addAll(Arrays.asList(AirSwingSideways.values()));
        }

        // Up/down swing modes
        supportedAirSwingUpDownModes = new TreeSet<>();
        if (Boolean.TRUE.equals(dto.airSwingLR)) { // Note: Seems to be switched with autoSwingUD
            supportedAirSwingUpDownModes.addAll(Arrays.asList(AirSwingUpDown.values()));
        }

        iAutoX = dto.iAutoX != null && dto.iAutoX;
        nanoe = dto.nanoe != null && dto.nanoe;
        nanoeStandAlone = dto.nanoeStandAlone != null && dto.nanoeStandAlone;
        iAutoX = dto.iAutoX != null && dto.iAutoX;
        ecoNavi = dto.ecoNavi != null && dto.ecoNavi;
        ecoFunction = (dto.ecoFunction == null) ? 0 : dto.ecoFunction;
    }

    public boolean isiAutoX() {
        return iAutoX;
    }

    public boolean isNanoe() {
        return nanoe;
    }

    public boolean isNanoeStandAlone() {
        return nanoeStandAlone;
    }

    public Set<OperationMode> getSupportedOperationModes() {
        return supportedOperationModes;
    }

    public Set<EcoMode> getSupportedEcoModes() {
        return supportedEcoModes;
    }

    public Set<AirSwingSideways> getSupportedSwingSidewayModes() {
        return supportedSwingSidewayModes;
    }

    public Set<AirSwingUpDown> getSupportedSwingUpDownModes() {
        return supportedAirSwingUpDownModes;
    }

    public boolean isEcoNavi() {
        return ecoNavi;
    }

    public Integer getEcoFunction() {
        return ecoFunction;
    }
}
