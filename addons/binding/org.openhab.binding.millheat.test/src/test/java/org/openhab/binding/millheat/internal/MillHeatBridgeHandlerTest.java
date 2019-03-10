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
package org.openhab.binding.millheat.internal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.millheat.internal.config.MillheatBridgeConfiguration;
import org.openhab.binding.millheat.internal.handler.MillheatBridgeHandler;
import org.osgi.framework.BundleContext;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 *
 * @author Arne Seime - Initial contribution
 */
public class MillHeatBridgeHandlerTest {

    private MillheatBridgeHandler subject;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999);

    @Mock
    private Bridge millheatThingMock;

    private HttpClient httpClient;

    @Mock
    private Configuration configuration;

    @Mock
    private BundleContext bundleContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        httpClient = new HttpClient();
        httpClient.start();
        subject = new MillheatBridgeHandler(millheatThingMock, httpClient, bundleContext);
        MillheatBridgeHandler.API_ENDPOINT_1 = "http://localhost:9999/zc-account/v1/";
        MillheatBridgeHandler.API_ENDPOINT_2 = "http://localhost:9999/millService/v1/";

    }

    @After
    public void shutdown() throws Exception {
        httpClient.stop();
    }

    @Test
    public void testLogin() throws InterruptedException, IOException {

        String loginResponse = IOUtils.toString(getClass().getResourceAsStream("/login_response_ok.json"));

        stubFor(get(urlEqualTo("/zc-account/v1/login"))
                .willReturn(aResponse().withStatus(200).withBody(loginResponse)));

        when(millheatThingMock.getConfiguration()).thenReturn(configuration);
        MillheatBridgeConfiguration bridgeConfig = new MillheatBridgeConfiguration();
        when(configuration.as(eq(MillheatBridgeConfiguration.class))).thenReturn(bridgeConfig);
        bridgeConfig.username = "username";
        bridgeConfig.password = "password";

        subject.initialize();

        verify(postRequestedFor(urlMatching("/zc-account/v1/login"))
                .withRequestBody(matching(".*username.*password.*")));
    }

}
