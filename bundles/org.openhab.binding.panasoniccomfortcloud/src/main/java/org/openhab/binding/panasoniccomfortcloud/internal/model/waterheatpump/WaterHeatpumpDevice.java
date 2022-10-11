package org.openhab.binding.panasoniccomfortcloud.internal.model.waterheatpump;

import java.util.Map;

import org.openhab.binding.panasoniccomfortcloud.internal.model.Device;
import org.openhab.binding.panasoniccomfortcloud.internal.model.Group;

public class WaterHeatpumpDevice extends Device {
    public WaterHeatpumpDevice(Group group) {
        super(group);
    }

    @Override
    public Map<String, String> getThingProperties() {
        return null;
    }
}
