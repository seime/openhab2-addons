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
package org.openhab.binding.august.internal;

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
import org.openhab.binding.august.internal.dto.AbstractRequest;
import org.openhab.binding.august.internal.handler.AccessTokenUpdatedListener;
import org.openhab.binding.august.internal.logging.RequestLogger;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link ApiBridge} is responsible for API login and communication
 *
 * @author Arne Seime - Initial contribution
 */
public class ApiBridge {
    public static final String HEADER_ACCESS_TOKEN = "x-august-access-token";
    public static String API_ENDPOINT = "https://api-production.august.com";
    private static final String API_KEY = "79fd0eb6-381d-4adf-95a0-47721289d1d9";

    private HttpClient httpClient;

    @Nullable
    private String accessToken = null;

    private Gson gson;
    @Nullable
    private RequestLogger requestLogger = null;
    private AccessTokenUpdatedListener listener;

    public ApiBridge(HttpClient httpClient) {
        this.httpClient = httpClient;
        gson = GsonFactory.create();
    }

    public void init(ThingUID bridgeUid, AccessTokenUpdatedListener listener) {
        this.requestLogger = new RequestLogger(bridgeUid.getId(), gson);
        this.listener = listener;
    }

    private Request buildRequest(final AbstractRequest req) {
        Request request = httpClient.newRequest(API_ENDPOINT + req.getRequestUrl()).method(req.getMethod());

        request.getHeaders().remove(HttpHeader.USER_AGENT);
        request.getHeaders().remove(HttpHeader.ACCEPT);
        request.header(HttpHeader.USER_AGENT, "August/2019.12.16.4708 CFNetwork/1121.2.2 Darwin/19.3.0");
        request.header(HttpHeader.ACCEPT, "application/json");
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.header("Accept-Version", "0.0.1");
        request.header("x-kease-api-key", API_KEY);
        request.header("x-august-api-key", API_KEY);
        if (accessToken != null) {
            request.header(HEADER_ACCESS_TOKEN, accessToken);
        }

        if (!req.getMethod().contentEquals(HttpMethod.GET.asString())) { // POST, PATCH, PUT
            final String reqJson = gson.toJson(req);
            request = request.content(new BytesContentProvider(reqJson.getBytes(StandardCharsets.UTF_8)),
                    "application/json");
        }

        requestLogger.listenTo(request, new String[] {});

        return request;
    }

    public <T> T sendRequest(final AbstractRequest req, final Type responseType) throws AugustException {

        try {

            return sendRequestInternal(buildRequest(req), req, responseType);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new CommunicationException(String.format("Error sending request to server: %s", e.getMessage()), e);
        }
    }

    public <T> T sendRequestInternal(final Request httpRequest, final AbstractRequest req, final Type responseType)
            throws AugustException, ExecutionException, InterruptedException, TimeoutException {

        final ContentResponse contentResponse = httpRequest.send();
        String newAccessToken = contentResponse.getHeaders().get(HEADER_ACCESS_TOKEN);
        if (accessToken == null || !accessToken.equals(newAccessToken)) {
            listener.onAccessTokenUpdated(newAccessToken);
        }
        accessToken = newAccessToken;

        final String responseJson = contentResponse.getContentAsString();
        if (contentResponse.getStatus() == HttpStatus.OK_200) {
            final JsonObject o = JsonParser.parseString(responseJson).getAsJsonObject();
            if (o.has("message")) {
                throw new CommunicationException(req, o.get("message").getAsString());
            } else {
                return gson.fromJson(responseJson, responseType);
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

    public String getLastAccessTokenFromHeader() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
