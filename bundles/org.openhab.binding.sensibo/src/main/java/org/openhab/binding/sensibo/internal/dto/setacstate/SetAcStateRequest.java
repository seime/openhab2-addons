/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.sensibo.internal.dto.setacstate;

import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.sensibo.internal.dto.AbstractRequest;
import org.openhab.binding.sensibo.internal.dto.poddetails.AcState;

/**
 * @author Arne Seime - Initial contribution
 */
public class SetAcStateRequest extends AbstractRequest {
    public SetAcStateRequest(String podId, AcState acState) {
        this.podId = podId;
        this.acState = acState;
    }

    public transient String podId; // Transient fields are ignored by gson
    public AcState acState;

    @Override
    public String getRequestUrl() {
        return String.format("/pods/%s/acStates", podId);
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.POST;
    }
}
