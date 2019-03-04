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
package org.openhab.binding.millheat.internal.dto;

/**
 * @author Arne Seime - Initial contribution
 */
public class HeaterDTO {

    public String name;
    public long deviceId;
    public String currentTemp;
    public double setTemp;
    public String fanStatus;
    public String powerStatus;
    public boolean independentDevice;
    public String room;
    public boolean openWindow;
    public boolean isHeating;
    public String tibberControl;
    public String subDomain;
    public boolean available;
    public boolean isHoliday;
    public boolean canChangeTemp;

}
