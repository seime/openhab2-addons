package org.openhab.binding.panasoniccomfortcloud.internal.model;

import java.util.Map;

public abstract class Device {
    protected Group group;
    protected String deviceId;
    protected String type; // Figure out what this is
    protected String name;

    public Device(Group group) {
        this.group = group;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Group getGroup() {
        return group;
    }

    public void setType(String deviceType) {
        this.type = deviceType;
    }

    public abstract Map<String, String> getThingProperties();
}
