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

import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.millheat.internal.model.Room;

/**
 * This DTO class wraps the set room temp request
 *
 * @see SetDeviceTempRequest
 * @author Arne Seime - Initial contribution
 */
public class SetRoomTempRequest implements AbstractRequest {

    public final String roomId;
    public double roomComfortTemperature;
    public double roomSleepTemperature;
    public double roomAwayTemperature;

    public SetRoomTempRequest(final Room room) {
        roomId = room.getId();
        roomComfortTemperature = room.getComfortTemp();
        roomSleepTemperature = room.getSleepTemp();
        roomAwayTemperature = room.getAwayTemp();
    }

    @Override
    public String getRequestUrl() {
        return "/rooms/" + roomId + "/temperature";
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.POST;
    }
}
