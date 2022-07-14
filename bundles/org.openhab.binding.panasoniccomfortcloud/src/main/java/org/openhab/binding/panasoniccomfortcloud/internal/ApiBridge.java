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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.AbstractRequest;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.LoginRequest;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.LoginResponse;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * The {@link ApiBridge} is responsible for API login and communication
 *
 * @author Arne Seime - Initial contribution
 */
public class ApiBridge {
    private static final String API_ENDPOINT = "https://accsmart.panasonic.com";

    private final Logger logger = LoggerFactory.getLogger(ApiBridge.class);

    private String username;
    private String password;
    private int refreshInterval;

    @Nullable
    private String accessToken = null;

    private Gson gson;

    OkHttpClient client;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public ApiBridge() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> logger.debug(message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        client = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).addInterceptor(logging).build();
        gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
    }

    public void init(ThingUID bridgeUid, String username, String password, int refreshInterval) {
        this.username = username;
        this.password = password;
        this.refreshInterval = refreshInterval;
    }

    private Request buildRequest(final AbstractRequest req) {

        Request.Builder request = new Request.Builder().url(API_ENDPOINT + req.getRequestUrl());

        if (req.getMethod().equals("POST")) {
            final String reqJson = gson.toJson(req);
            request = request.post(RequestBody.create(JSON, reqJson));
        }

        request.removeHeader("User-Agent");
        request.removeHeader("Accept");
        request.header("User-Agent", "G-RAC");
        request.header("Accept", "application/json; charset=utf-8");
        request.header("Content-Type", "application/json; charset=utf-8");
        request.header("X-APP-TYPE", "1");
        request.header("X-APP-VERSION", "1.15.0");
        if (accessToken != null) {
            request.header("X-User-Authorization", accessToken);
        }
        return request.build();
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

        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 200) {

                final JsonObject o = JsonParser.parseString(response.body().string()).getAsJsonObject();
                if (o.has("message")) {
                    throw new CommunicationException(req, o.get("message").getAsString());
                } else {
                    return gson.fromJson(o, responseType);
                }

            } else if (response.code() == 401) {
                if (accessToken == null) {
                    throw new CommunicationException("Could not renew token");
                } else {
                    accessToken = null; // expired
                    return sendRequest(req, responseType); // Retry login + request

                }
            } else {
                throw new CommunicationException("Error sending request to server. Server responded with "
                        + response.code() + " and payload " + response.body().string());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
