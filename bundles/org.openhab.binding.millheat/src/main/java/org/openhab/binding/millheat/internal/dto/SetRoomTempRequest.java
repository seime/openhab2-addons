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

import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.Room;

/**
 * @author Arne Seime - Initial contribution
 */
public class SetRoomTempRequest extends AbstractRequest {
    public long roomId;
    public int comfortTemp;
    public int sleepTemp;
    public int awayTemp;
    public int homeType;

    public SetRoomTempRequest(Home home, Room room) {
        roomId = Long.parseLong(room.getId());
        homeType = home.getType();
        comfortTemp = room.getComfortTemp();
        sleepTemp = room.getSleepTemp();
        awayTemp = room.getAwayTemp();
    }

    @Override
    public String getRequestUrl() {
        return "changeRoomModeTempInfo";
    }
}
