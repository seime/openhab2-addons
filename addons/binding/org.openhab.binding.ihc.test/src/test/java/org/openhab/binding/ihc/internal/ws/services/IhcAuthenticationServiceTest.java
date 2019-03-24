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
package org.openhab.binding.ihc.internal.ws.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.ihc.internal.ws.ResourceFileUtils;
import org.openhab.binding.ihc.internal.ws.datatypes.WSLoginResult;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcAuthenticationServiceTest {

    private IhcAuthenticationService ihcAuthenticationService;
    private final String host = "1.1.1.1";
    private final String url = "https://1.1.1.1/ws/AuthenticationService";
    private final int timeout = 100;

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcAuthenticationService = spy(new IhcAuthenticationService(host, timeout, new IhcConnectionPool()));

        final String querySuccesfulLogin = ResourceFileUtils
                .getFileContent("src/test/resources/SuccesfulLoginQuery.xml");
        final String responseSuccesfulLogin = ResourceFileUtils
                .getFileContent("src/test/resources/SuccesfulLoginResponse.xml");

        doReturn(responseSuccesfulLogin).when(ihcAuthenticationService).sendQuery(eq(url), any(),
                eq(querySuccesfulLogin), eq(timeout));

        final String queryLoginFailed = ResourceFileUtils.getFileContent("src/test/resources/LoginFailedQuery.xml");
        final String responseLoginFailed = ResourceFileUtils
                .getFileContent("src/test/resources/LoginFailedResponse.xml");

        doReturn(responseLoginFailed).when(ihcAuthenticationService).sendQuery(eq(url), any(), eq(queryLoginFailed),
                eq(timeout));
    }

    @Test
    public void testSuccesfulLogin() throws IhcExecption {
        final WSLoginResult result = ihcAuthenticationService.authenticate("user", "pass", "treeview");
        assertEquals(true, result.isLoginWasSuccessful());
        assertEquals(false, result.isLoginFailedDueToAccountInvalid());
        assertEquals(false, result.isLoginFailedDueToConnectionRestrictions());
        assertEquals(false, result.isLoginFailedDueToInsufficientUserRights());
    }

    @Test
    public void testFailedLogin() throws IhcExecption {
        final WSLoginResult result = ihcAuthenticationService.authenticate("user", "wrong", "treeview");
        assertEquals(false, result.isLoginWasSuccessful());
        assertEquals(true, result.isLoginFailedDueToAccountInvalid());
        assertEquals(false, result.isLoginFailedDueToConnectionRestrictions());
        assertEquals(false, result.isLoginFailedDueToInsufficientUserRights());
    }
}
