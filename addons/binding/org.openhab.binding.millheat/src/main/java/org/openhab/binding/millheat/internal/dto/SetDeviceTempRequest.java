package org.openhab.binding.millheat.internal.dto;

import org.openhab.binding.millheat.internal.model.Heater;

public class SetDeviceTempRequest extends AbstractRequest {

    public int subDomain;
    public int deviceId;
    public boolean testStatus = true;
    public int operation = 0; // TODO
    public int status = 0; // TODO

    public int windStatus = 0; // TODO
    public int holdTemp = 0;
    public int tempType = 0; // FIXED?
    public int powerLevel = 0; // FIXED?

    @Override
    public String getRequestUrl() {
        return "deviceControl";
    }

    public SetDeviceTempRequest(Heater heater, int targetTemperature) {
        this.subDomain = heater.subDomain;
        this.deviceId = Integer.parseInt(heater.id);
        this.holdTemp = targetTemperature;

    }

}
