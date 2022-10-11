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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;
import org.openhab.binding.panasoniccomfortcloud.internal.BindingConstants;
import org.openhab.binding.panasoniccomfortcloud.internal.model.Group;
import org.openhab.binding.panasoniccomfortcloud.internal.model.airconditioner.AirconditionDevice;

import com.google.gson.reflect.TypeToken;

/**
 * @author Arne Seime - Initial contribution
 */
public class GetDeviceResponseTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testDeserialize() throws IOException {
        final Type type = new TypeToken<DeviceDTO>() {
        }.getType();

        final DeviceDTO rsp = wireHelper.deSerializeFromClasspathResource("/get_device_response_on.json", type);
        AirconditionDevice airconditionDevice = new AirconditionDevice(new Group());
        airconditionDevice.mergeFromDeviceDetails(rsp);

        assertEquals(20, airconditionDevice.getCurrentParameters().getInsideTemperature());
    }

    @Test
    public void testDeserializeWifiDongleInvalidInsideTemperature() throws IOException {
        final Type type = new TypeToken<DeviceDTO>() {
        }.getType();

        final DeviceDTO rsp = wireHelper.deSerializeFromClasspathResource("/get_device_response_wifi_dongle_off.json",
                type);
        AirconditionDevice airconditionDevice = new AirconditionDevice(new Group());
        airconditionDevice.setType(BindingConstants.DEVICE_TYPE_WIFI_DONGLE);
        airconditionDevice.mergeFromDeviceDetails(rsp);

        assertEquals(null, airconditionDevice.getCurrentParameters().getInsideTemperature());
    }
}
