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

/**
 * This DTO class wraps the metrics details of a device
 *
 * @author Arne Seime - Initial contribution
 */
public class MetricsDTO {
    public String deviceId;

    public double temperature;
    public double temperatureAmbient;

    public double humidity;

    public double currentPower;

    public int currentOperationMode;

    public int currentTemperatureTypeInWeeklyProgram;

    public int openWindowStatus;

    public int powerStatus;

    public int heaterFlag;
}
