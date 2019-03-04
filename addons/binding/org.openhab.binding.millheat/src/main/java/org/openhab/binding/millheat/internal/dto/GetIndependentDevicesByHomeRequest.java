package org.openhab.binding.millheat.internal.dto;

public class GetIndependentDevicesByHomeRequest extends AbstractRequest {

    public GetIndependentDevicesByHomeRequest(Long homeId) {
        super();
        this.homeId = homeId;
    }

    public Long homeId;

    @Override
    public String getRequestUrl() {
        return "getIndependentDevices";
    }

}
