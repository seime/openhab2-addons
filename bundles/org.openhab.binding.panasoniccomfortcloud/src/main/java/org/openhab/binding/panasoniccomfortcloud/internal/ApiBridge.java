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
import java.util.concurrent.TimeUnit;

import org.openhab.binding.panasoniccomfortcloud.internal.dto.AbstractRequest;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.LoginRequest;
import org.openhab.binding.panasoniccomfortcloud.internal.dto.LoginResponse;
import org.openhab.core.storage.Storage;
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

    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final int ERROR_CODE_UPDATE_VERSION = 4106;

    private final Logger logger = LoggerFactory.getLogger(ApiBridge.class);

    private String username;
    private String password;

    private String appVersion;

    private Gson gson;

    private OkHttpClient client;

    private Storage<String> storage;

    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
    public static final MediaType JSON = MediaType.parse(APPLICATION_JSON_CHARSET_UTF_8);

    public ApiBridge(Storage<String> storage) {
        this.storage = storage;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> logger.debug(message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        client = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).addInterceptor(logging).build();
        gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
    }

    public void init(String username, String password, String appVersion) {
        this.username = username;
        this.password = password;
        this.appVersion = appVersion;
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
        request.header("Accept", APPLICATION_JSON_CHARSET_UTF_8);
        request.header("Content-Type", APPLICATION_JSON_CHARSET_UTF_8);
        request.header("X-APP-TYPE", "1");
        request.header("X-APP-VERSION", appVersion);
        if (storage.containsKey(ACCESS_TOKEN_KEY)) {
            request.header("X-User-Authorization", storage.get(ACCESS_TOKEN_KEY));
        }
        return request.build();
    }

    public <T> T sendRequest(final AbstractRequest req, final Type responseType) throws PanasonicComfortCloudException {

        if (!storage.containsKey(ACCESS_TOKEN_KEY)) {
            // No access token, send login with username+password
            LoginRequest loginRequest = new LoginRequest(username, password);
            LoginResponse loginResponse = sendRequestInternal(buildRequest(loginRequest), loginRequest,
                    new TypeToken<LoginResponse>() {
                    }.getType());

            storage.put(ACCESS_TOKEN_KEY, loginResponse.uToken);
        }

        return sendRequestInternal(buildRequest(req), req, responseType);
    }

    public <T> T sendRequestInternal(final Request request, final AbstractRequest req, final Type responseType)
            throws PanasonicComfortCloudException {

        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 200) {
                final JsonObject o = JsonParser.parseString(response.body().string()).getAsJsonObject();
                if (o.has("message")) {
                    throw new CommunicationException(req, o.get("message").getAsString());
                } else {
                    T responseJson = gson.fromJson(o, responseType);
                    if (responseJson != null) {
                        return responseJson;
                    } else {
                        throw new CommunicationException(
                                "Unable to unmarshal response from API: " + response.body().string());
                    }
                }

            } else if (response.code() == 401) {
                if (storage.get(ACCESS_TOKEN_KEY) == null) {
                    final JsonObject o = JsonParser.parseString(response.body().string()).getAsJsonObject();
                    int errorCode = o.has("code") ? o.get("code").getAsInt() : -1;
                    String errorMessage = o.has("message") ? o.get("message").getAsString() : "<not provided>";

                    if (errorCode == ERROR_CODE_UPDATE_VERSION) {
                        throw new CommunicationException(String.format(
                                "New app version published - check the version number of your mobile app and enter the value as account config parameter (currently using %s)",
                                appVersion));
                    } else {
                        throw new CommunicationException(
                                String.format("Could not renew token: code %d, message %s", errorCode, errorMessage));
                    }
                } else {
                    storage.remove(ACCESS_TOKEN_KEY);
                    return sendRequest(req, responseType); // Retry login + request

                }
            } else {
                throw new CommunicationException("Error sending request to server. Server responded with "
                        + response.code() + " and payload " + response.body().string());
            }
        } catch (IOException e) {
            throw new CommunicationException("General error communicating with service: " + e);
        }
    }
}
