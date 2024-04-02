/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.dto;

import org.eclipse.jetty.http.HttpMethod;

/**
 * This DTO class wraps the select room by home request
 * 
 * @author Arne Seime - Initial contribution
 */
public class GetRoomsRequest implements AbstractRequest {
    public final transient String homeId;

    public GetRoomsRequest(final String homeId) {
        this.homeId = homeId;
    }

    @Override
    public String getRequestUrl() {
        return "houses/" + homeId + "/rooms";
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.GET;
    }
}
