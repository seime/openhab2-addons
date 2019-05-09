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
import org.openhab.binding.sensibo.internal.dto.SetAcStateRequest;
import org.openhab.binding.sensibo.internal.dto.poddetails.AcState;

/**
 * @author Arne Seime - Initial contribution
 */
public class SetAcStateRequestTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testSerializeDeserialize() throws IOException {
        AcState acState = new AcState(true, "fanLevel", "C", 21, "mode", "swing");
        SetAcStateRequest req = new SetAcStateRequest("PODID", acState);
        String serializedJson = serialize(req);

        final SetAcStateRequest deSerializedRequest = deSerializeFromString(serializedJson, SetAcStateRequest.class);
        assertNotNull(deSerializedRequest.getAcState());
        assertTrue(deSerializedRequest.getAcState().isOn());
    }

}
