package org.openhab.binding.sensibo.internal.dto.pods;

import org.openhab.binding.sensibo.internal.dto.AbstractRequest;

public class GetPodsRequest extends AbstractRequest {

    @Override
    public String getRequestUrl() {
        return "/users/me/pods";
    }

}
