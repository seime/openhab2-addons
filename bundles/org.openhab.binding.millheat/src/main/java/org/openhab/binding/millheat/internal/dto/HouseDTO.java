/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the home dto json structure
 *
 * @author Arne Seime - Initial contribution
 */
public class HouseDTO {
    public String id;

    @SerializedName("name")
    public String name;
    public String timezone;

    public String mode;

    @SerializedName("vacationStartDate")
    public Long holidayStartTime;
    @SerializedName("vacationEndDate")
    public Long holidayEndTime;

    @SerializedName("vacationTemperature")
    public Double holidayTemp;

    public String vacationModeType;

    @SerializedName("isVacationModeActive")
    public boolean holiday;

    public Object overrideModeType;

    public Long overrideEndDate;
}
