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
package org.openhab.binding.sensibo.internal;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;

import org.junit.Test;
import org.openhab.binding.sensibo.internal.dto.poddetails.AcState;
import org.openhab.binding.sensibo.internal.dto.poddetails.Measurement;
import org.openhab.binding.sensibo.internal.dto.poddetails.ModeCapability;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetails;
import org.openhab.binding.sensibo.internal.dto.poddetails.Temperature;

/**
 * @author Arne Seime - Initial contribution
 */
public class GetPodDetailsResponseTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testDeserialize() throws IOException {
        final PodDetails rsp = deSerializeResponse("/get_pod_details_response.json", PodDetails.class);

        assertEquals("MA:C:AD:DR:ES:S0", rsp.getMacAddress());
        assertEquals("IN010056", rsp.getFirmwareVersion());
        assertEquals("cc3100_stm32f0", rsp.getFirmwareType());
        assertEquals("SERIALNUMASSTRING", rsp.getSerialNumber());
        assertEquals("C", rsp.getTemperatureUnit());
        assertEquals("skyv2", rsp.getProductModel());
        assertNull(rsp.getSmartMode());

        assertAcState(rsp.getAcState());
        assertMeasurement(rsp.getLastMeasurement());
        assertRemoteCapabilities(rsp.getRemoteCapabilities());

    }

    private void assertRemoteCapabilities(final Map<String, ModeCapability> remoteCapabilities) {
        assertNotNull(remoteCapabilities);

        assertEquals(5, remoteCapabilities.size());
        final ModeCapability mode = remoteCapabilities.get("heat");

        assertNotNull(mode.getSwingModes());
        assertNotNull(mode.getFanLevels());
        assertNotNull(mode.getTemperatures());
        final Map<String, Temperature> temperatures = mode.getTemperatures();
        final Temperature temperature = temperatures.get("C");
        assertNotNull(temperature);
        assertNotNull(temperature.getValidValues());

    }

    private void assertMeasurement(final Measurement lastMeasurement) {
        assertNotNull(lastMeasurement);
        assertNull(lastMeasurement.getBatteryVoltage());
        assertEquals(Double.valueOf("22.5"), lastMeasurement.getTemperature());
        assertEquals(Double.valueOf("24.2"), lastMeasurement.getHumidity());
        assertEquals(Integer.valueOf("-71"), lastMeasurement.getWifiSignalStrength());
        assertEquals(ZonedDateTime.parse("2019-05-05T07:52:11Z"), lastMeasurement.getMeasurementTimestamp());

    }

    private void assertAcState(final AcState acState) {
        assertNotNull(acState);

        assertTrue(acState.isOn());
        assertEquals("medium_high", acState.getFanLevel());
        assertEquals("C", acState.getTemperatureUnit());
        assertEquals(21, acState.getTargetTemperature());
        assertEquals("heat", acState.getMode());
        assertEquals("rangeFull", acState.getSwing());

    }
}
