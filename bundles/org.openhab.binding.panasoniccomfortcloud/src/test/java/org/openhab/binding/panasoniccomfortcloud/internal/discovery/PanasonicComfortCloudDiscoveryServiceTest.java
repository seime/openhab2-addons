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
package org.openhab.binding.panasoniccomfortcloud.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author Arne Seime - Initial contribution
 */
public class PanasonicComfortCloudDiscoveryServiceTest {

    @Test
    public void testIllegalCharactersInThingUID() {
        assertEquals("A-B", PanasonicComfortCloudDiscoveryService.createCleanDeviceId("A+B"));
        assertEquals("A-B", PanasonicComfortCloudDiscoveryService.createCleanDeviceId("A/B"));
        assertEquals("A-B", PanasonicComfortCloudDiscoveryService.createCleanDeviceId("A#B"));
        assertEquals("A_B", PanasonicComfortCloudDiscoveryService.createCleanDeviceId("A_B"));
        assertEquals("A_B", PanasonicComfortCloudDiscoveryService.createCleanDeviceId("A_B"));
        assertEquals("A-B", PanasonicComfortCloudDiscoveryService.createCleanDeviceId("A-B"));
        assertEquals("a-b", PanasonicComfortCloudDiscoveryService.createCleanDeviceId("a-b"));
        assertEquals("1-2", PanasonicComfortCloudDiscoveryService.createCleanDeviceId("1-2"));
    }
}
