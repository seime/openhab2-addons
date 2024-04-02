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
package org.openhab.binding.millheat.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;
import org.openhab.binding.millheat.internal.dto.*;

import com.google.gson.reflect.TypeToken;

/**
 * Testing class for serialization and deserialization of http json playloads
 * 
 * @author Arne Seime - Initial contribution
 */
class SerializationDeserializationTest {

    protected WireHelper wireHelper = new WireHelper();

    @Test
    void loginResponse() throws IOException {
        final Type type = new TypeToken<LoginResponse>() {
        }.getType();

        final LoginResponse message = wireHelper.deSerializeFromClasspathResource("/v2/login_response_ok.json", type);

        assertEquals("IDTOKEN", message.idToken);
        assertEquals("REFRESHTOKEN", message.refreshToken);
    }

    @Test
    void getHouses() throws IOException {
        final Type type = new TypeToken<GetHousesResponse>() {
        }.getType();

        final GetHousesResponse message = wireHelper.deSerializeFromClasspathResource("/v2/get_houses_ok.json", type);

        assertEquals(2, message.houses.length);
        HouseDTO house = message.houses[0];

        assertEquals("HOUSEID", house.id);
        assertEquals("Hjemme", house.name);
        assertEquals("Europe/Berlin", house.timezone);
        assertEquals("weekly_program", house.mode);
        assertEquals(1601570862L, house.holidayStartTime);
        assertEquals(1604252862L, house.holidayEndTime);
        assertEquals(5, house.holidayTemp);
        assertEquals("use_vacation_temperature", house.vacationModeType);
    }

    @Test
    void getRooms() throws IOException {
        final Type type = new TypeToken<GetRoomsResponse>() {
        }.getType();

        final GetRoomsResponse message = wireHelper.deSerializeFromClasspathResource("/v2/get_rooms_ok.json", type);

        assertEquals(3, message.rooms.length);
        RoomDTO room = message.rooms[1];

        assertEquals("OFFICE", room.id);
        assertEquals("Kontor oppe", room.name);
        assertEquals(10, room.roomAwayTemperature);
        assertEquals(19, room.roomComfortTemperature);
        assertEquals(15, room.roomSleepTemperature);
        assertFalse(room.online);
    }

    @Test
    void getDevices() throws IOException {
        final Type type = new TypeToken<GetDevicesResponse[]>() {
        }.getType();

        final GetDevicesResponse[] message = wireHelper.deSerializeFromClasspathResource("/v2/get_devices_ok.json",
                type);

        assertEquals(2, message.length);

        GetDevicesResponse bedroomUpstairs = message[0];
        DeviceDTO[] devices = bedroomUpstairs.devices;
        assertEquals(1, devices.length);

        DeviceDTO device = devices[0];

        assertEquals("DEVICEID_BEDROOM_UPSTAIRS", device.deviceId);
        assertEquals("Chuck", device.customName);
        assertEquals("MAC_BEDROOM_UPSTAIRS", device.macAddress);
        assertTrue(device.isConnected);
        assertTrue(device.isEnabled);

        MetricsDTO lastMetrics = device.lastMetrics;
        assertNotNull(lastMetrics);
        assertEquals(20, lastMetrics.temperatureAmbient);
        assertEquals(0, lastMetrics.openWindowStatus);
        assertEquals(1, lastMetrics.powerStatus);
        assertEquals(0, lastMetrics.heaterFlag);
    }
}
