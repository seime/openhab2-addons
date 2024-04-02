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
 * This DTO class wraps the login request
 * 
 * @author Arne Seime - Initial contribution
 */
public class LoginRequest implements AbstractRequest {
    public final String login;
    public final String password;

    public LoginRequest(final String login, final String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public String getRequestUrl() {
        return "customer/auth/sign-in";
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.POST;
    }
}
