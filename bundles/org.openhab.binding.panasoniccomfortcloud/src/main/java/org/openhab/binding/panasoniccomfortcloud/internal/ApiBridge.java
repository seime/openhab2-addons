/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.panasoniccomfortcloud.internal;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.AbstractRequest;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.LoginRequest;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.LoginResponse;
import org.openhab.binding.panasoniccomfortcloud.internal.logging.RequestLogger;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link ApiBridge} is responsible for API login and communication
 *
 * @author Arne Seime - Initial contribution
 */
public class ApiBridge {
    private static final String API_ENDPOINT = "https://accsmart.panasonic.com";

    private HttpClient httpClient;
    private String username;
    private String password;
    private int refreshInterval;

    @Nullable
    private String accessToken = null;

    private Gson gson;
    @Nullable
    private RequestLogger requestLogger = null;

    public ApiBridge(HttpClient httpClient) {
        this.httpClient = httpClient;
        gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
    }

    public void init(ThingUID bridgeUid, String username, String password, int refreshInterval) {
        this.username = username;
        this.password = password;
        this.refreshInterval = refreshInterval;
        this.requestLogger = new RequestLogger(bridgeUid.getId(), gson);
    }

    private Request buildRequest(final AbstractRequest req) {
        Request request = httpClient.newRequest(API_ENDPOINT + req.getRequestUrl()).method(req.getMethod());

        request.getHeaders().remove(HttpHeader.USER_AGENT);
        request.getHeaders().remove(HttpHeader.ACCEPT);
        request.header(HttpHeader.USER_AGENT, "G-RAC");
        request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.header("X-APP-TYPE", "1");
        request.header("X-APP-VERSION", "1.20.0");
        request.header("X-User-Authorization", accessToken);

        if (!req.getMethod().contentEquals(HttpMethod.GET.asString())) { // POST, PATCH
            final String reqJson = gson.toJson(req);
            request = request.content(new BytesContentProvider(reqJson.getBytes(StandardCharsets.UTF_8)),
                    "application/json");
        }

        requestLogger.listenTo(request, new String[] { password });

        return request;
    }

    public <T> T sendRequest(final AbstractRequest req, final Type responseType) throws PanasonicComfortCloudException {

        try {
            if (accessToken == null) {
                // Login first
                LoginRequest loginRequest = new LoginRequest(username, password);
                LoginResponse loginResponse = sendRequestInternal(buildRequest(loginRequest), loginRequest,
                        new TypeToken<LoginResponse>() {
                        }.getType());

                accessToken = loginResponse.uToken;
            }

            return sendRequestInternal(buildRequest(req), req, responseType);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new CommunicationException(String.format("Error sending request to server: %s", e.getMessage()), e);
        }
    }

    public <T> T sendRequestInternal(final Request request, final AbstractRequest req, final Type responseType)
            throws PanasonicComfortCloudException, ExecutionException, InterruptedException, TimeoutException {

        final ContentResponse contentResponse = request.send();
        final String responseJson = contentResponse.getContentAsString();
        if (contentResponse.getStatus() == HttpStatus.OK_200) {
            final JsonObject o = JsonParser.parseString(responseJson).getAsJsonObject();
            if (o.has("message")) {
                throw new CommunicationException(req, o.get("message").getAsString());
            } else {
                return gson.fromJson(o, responseType);
            }
        } else if (contentResponse.getStatus() == HttpStatus.UNAUTHORIZED_401) {
            if (accessToken == null) {
                throw new CommunicationException("Could not renew token");
            } else {
                accessToken = null; // expired
                return sendRequest(req, responseType); // Retry login + request
            }

        } else if (contentResponse.getStatus() == HttpStatus.FORBIDDEN_403) {
            throw new ConfigurationException("Invalid credentials");
        } else {
            throw new CommunicationException("Error sending request to server. Server responded with "
                    + contentResponse.getStatus() + " and payload " + responseJson);
        }
    }
    /**
     * else {
     * try {
     * if(response.statusCode == 500 || response.statusCode == 503) {
     * if(body.code == 5005) {this.log("Warning - Device has lost connectivity to Comfort Cloud. Check your Wi-Fi
     * connectivity or restart the Heat Pump.", body.code, body.message);}
     * else {this.log("Error - 500 Internal Server Error. Comfort Cloud server is experiencing issues.", err);}
     * }
     * else if(response.statusCode == 403) {this.log("Error - 403 Forbidden. Check the configured email and password,
     * then restart Homebridge.", body.code, body.message, err);}
     * else if(response.statusCode == 401) {this.log("Warning - 401 Unauthorized. Login token has expired.", body.code,
     * body.message, err);}
     * else {this.log("Unknown error. An update to the plug-in may be required. Check for the latest version.",
     * response.statusCode, body.code, body.message);}
     * }
     * catch(err) {this.log("Unknown error. An update to the plug-in may be required. Check for the latest version.",
     * err);}
     * }
     * 
     * 
     */
}
