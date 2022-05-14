package org.openhab.binding.panasoniccomfortcloud.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
