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
package org.openhab.binding.sensibo.internal;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.openhab.binding.sensibo.internal.dto.SetAcStateReponse;

/**
 * @author Arne Seime - Initial contribution
 */
public class SetAcStateResponseTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testDeserialize() throws IOException {

        final SetAcStateReponse rsp = deSerializeResponse("/set_acstate_response.json", SetAcStateReponse.class);

        assertNotNull(rsp.getAcState());
        assertTrue(rsp.getAcState().isOn());
    }
}
