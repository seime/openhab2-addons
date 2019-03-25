package org.openhab.binding.millheat.internal.dto;

import org.openhab.binding.millheat.internal.model.Heater;

public class SetDeviceTempRequest extends AbstractRequest {

    public int subDomain;
    public int deviceId;
    public boolean testStatus = true;
    public int operation = 0;
    public boolean status;

    public boolean windStatus;
    public int holdTemp = 0;
    public int tempType = 0; // FIXED?
    public int powerLevel = 0; // FIXED?

    @Override
    public String getRequestUrl() {
        return "deviceControl";
    }

    public SetDeviceTempRequest(Heater heater, int targetTemperature, boolean masterSwitch, boolean fanActive) {
        this.subDomain = heater.subDomain;
        this.deviceId = Integer.parseInt(heater.id);
        this.holdTemp = targetTemperature;
        this.status = masterSwitch;
        this.windStatus = fanActive;

        if (fanActive != heater.fanActive) {
            // Changed
            operation = 4;
        } else if (heater.targetTemp != targetTemperature) {
            operation = 1;
        } else {
            operation = 0;
        }
    }

}
