package org.openhab.binding.sensibo.internal.dto.poddetails;

import org.openhab.binding.sensibo.internal.dto.AbstractRequest;

public class GetPodsDetailsRequest extends AbstractRequest {

    private String id;

    public GetPodsDetailsRequest(String id) {
        super();
        this.id = id;
    }

    @Override
    public String getRequestUrl() {
        return String.format("/pods/%s", id);
    }

}
