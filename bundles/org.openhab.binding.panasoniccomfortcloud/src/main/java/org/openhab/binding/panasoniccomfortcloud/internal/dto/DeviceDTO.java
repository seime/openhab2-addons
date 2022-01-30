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
package org.openhab.binding.panasoniccomfortcloud.internal.dto;

/**
 * All classes in the .dto are data transfer classes used by the GSON mapper. This class reflects a
 * part of a request/response data structure.
 *
 * @author Arne Seime - Initial contribution.
 */

public class DeviceDTO {
    public String deviceGuid;
    public String deviceType;
    public String deviceName;
    public Integer permission;
    public String deviceModuleNumber;
    public String deviceHashGuid;
    public Integer summerHouse;
    public Boolean iAutoX;
    public Boolean nanoe;
    public Boolean nanoeStandAlone;
    public Boolean autoMode;
    public Boolean heatMode;
    public Boolean fanMode;
    public Boolean dryMode;
    public Boolean coolMode;
    public Boolean ecoNavi;
    public Boolean powerfulMode;
    public Boolean quietMode;
    public Boolean airSwingLR;
    public Boolean autoSwingUD;
    public Integer ecoFunction;
    public Integer temperatureUnit;
    /**
     * TODO modeAvlList - figure out what it is
     * * "modeAvlList": {
     * "autoMode": 1,
     * "fanMode": 1
     * },
     */
    public Boolean coordinableFlg;
    public Boolean pairedFlg;

    public Integer dryTempMin;
    public Integer dryTempMax;
    public Integer heatTempMin;
    public Integer heatTempMax;
    public Integer coolTempMin;
    public Integer coolTempMax;
    public Integer autoTempMin;
    public Integer autoTempMax;
    public Long timestamp;

    public ParametersDTO parameters;
}
