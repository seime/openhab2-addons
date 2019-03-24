package org.openhab.binding.millheat.internal.dto;

public class GetIndependentDevicesByHomeRequest extends AbstractRequest {

    public Long homeId;

    public GetIndependentDevicesByHomeRequest(Long homeId, String timeZone) {
        super();
        this.homeId = homeId;

    }

    @Override
    public String getRequestUrl() {
        return "getIndependentDevices";
    }

}
