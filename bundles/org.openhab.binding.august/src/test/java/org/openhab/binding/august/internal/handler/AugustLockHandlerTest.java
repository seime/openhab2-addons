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
package org.openhab.binding.august.internal.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.august.internal.ApiBridge.HEADER_ACCESS_TOKEN;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.august.internal.ApiBridge;
import org.openhab.binding.august.internal.BindingConstants;
import org.openhab.binding.august.internal.config.LockConfiguration;
import org.openhab.binding.august.internal.dto.RemoteOperateLockRequest;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.internal.ThingImpl;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 *
 * @author Arne Seime - Initial contribution
 */

@ExtendWith(MockitoExtension.class)
public class AugustLockHandlerTest {

    private WireMockServer wireMockServer;

    private HttpClient httpClient;

    private @Mock Configuration configuration;
    private @Mock Bridge bridge;

    private Storage<String> storage;

    private ApiBridge apiBridge;

    @BeforeEach
    public void setUp() throws Exception {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();

        int port = wireMockServer.port();
        WireMock.configureFor("localhost", port);
        ApiBridge.API_ENDPOINT = "http://localhost:" + port;

        httpClient = new HttpClient();
        httpClient.start();

        apiBridge = new ApiBridge(httpClient);
        apiBridge.init(new ThingUID("august:bridge:1"), updatedAccessToken -> {
        });

        storage = new VolatileStorage<>();
    }

    @AfterEach
    public void shutdown() throws Exception {
        httpClient.stop();
    }

    @Test
    public void testInitialize() throws IOException {
        // Setup account
        final LockConfiguration lockConfiguration = new LockConfiguration();
        lockConfiguration.lockId = "LockId1";
        when(configuration.as(eq(LockConfiguration.class))).thenReturn(lockConfiguration);

        ThingImpl lockThing = createLockThing();

        apiBridge.setAccessToken("ACCESSTOKEN");

        // Setup get lock response
        prepareGetNetworkResponse("/locks/" + lockConfiguration.lockId, "/get_lock_response.json", 200);

        AugustLockHandler lockHandler = Mockito.spy(new AugustLockHandler(lockThing, apiBridge));
        ThingHandlerCallback thingHandlerCallback = Mockito.mock(ThingHandlerCallback.class);
        lockHandler.setCallback(thingHandlerCallback);

        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(lockHandler.getBridge()).thenReturn(bridge);
        lockHandler.initialize();
        lockHandler.doPoll();

        verify(thingHandlerCallback).stateUpdated(new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_BATTERY),
                new QuantityType<>(47.75072124321014, Units.PERCENT));
        verify(thingHandlerCallback)
                .stateUpdated(new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_LOCK_STATE), OnOffType.ON);
        verify(thingHandlerCallback).stateUpdated(
                new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_DOOR_STATE), OpenClosedType.CLOSED);
    }

    @Test
    public void testUnlockDoor() throws IOException {
        // Setup account
        final LockConfiguration lockConfiguration = new LockConfiguration();
        lockConfiguration.lockId = "LockId1";
        when(configuration.as(eq(LockConfiguration.class))).thenReturn(lockConfiguration);

        ThingImpl lockThing = createLockThing();

        apiBridge.setAccessToken("ACCESSTOKEN");

        // Setup get lock response
        prepareGetNetworkResponse("/locks/" + lockConfiguration.lockId, "/get_lock_response.json", 200);

        AugustLockHandler lockHandler = Mockito.spy(new AugustLockHandler(lockThing, apiBridge));
        ThingHandlerCallback thingHandlerCallback = Mockito.mock(ThingHandlerCallback.class);
        lockHandler.setCallback(thingHandlerCallback);

        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(lockHandler.getBridge()).thenReturn(bridge);
        lockHandler.initialize();
        lockHandler.doPoll();

        verify(thingHandlerCallback).stateUpdated(new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_BATTERY),
                new QuantityType<>(47.75072124321014, Units.PERCENT));
        verify(thingHandlerCallback)
                .stateUpdated(new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_LOCK_STATE), OnOffType.ON);
        verify(thingHandlerCallback).stateUpdated(
                new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_DOOR_STATE), OpenClosedType.CLOSED);

        preparePutNetworkResponse(
                String.format("/remoteoperate/%s/%s", lockConfiguration.lockId,
                        RemoteOperateLockRequest.Operation.UNLOCK.getUrlWord()),
                "/remoteoperate_lock_response.json", 200);

        lockHandler.handleCommand(new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_LOCK_STATE),
                OnOffType.OFF);

        verify(thingHandlerCallback)
                .stateUpdated(new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_LOCK_STATE), OnOffType.OFF);
    }

    private ThingImpl createLockThing() {
        ThingImpl lockThing = new ThingImpl(BindingConstants.THING_TYPE_LOCK, "LockId1");
        lockThing.addChannel(
                ChannelBuilder.create(new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_LOCK_STATE)).build());
        lockThing.addChannel(
                ChannelBuilder.create(new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_DOOR_STATE)).build());
        lockThing.addChannel(
                ChannelBuilder.create(new ChannelUID(lockThing.getUID(), BindingConstants.CHANNEL_BATTERY)).build());
        lockThing.setConfiguration(configuration);
        return lockThing;
    }

    private void preparePutNetworkResponse(String urlPath, String responseResource, int responseCode)
            throws IOException {
        stubFor(put(urlEqualTo(urlPath)).willReturn(aResponse().withStatus(responseCode)
                .withBody(getClasspathJSONContent(responseResource)).withHeader(HEADER_ACCESS_TOKEN, "ACCESSTOKEN")));
    }

    private void prepareGetNetworkResponse(String urlPath, String responseResource, int responseCode)
            throws IOException {
        stubFor(get(urlEqualTo(urlPath)).willReturn(aResponse().withStatus(responseCode)
                .withBody(getClasspathJSONContent(responseResource)).withHeader(HEADER_ACCESS_TOKEN, "ACCESSTOKEN")));
    }

    private String getClasspathJSONContent(String path) throws IOException {
        return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
    }
}
