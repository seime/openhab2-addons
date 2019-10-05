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
package org.openhab.binding.sensibo.internal.dto;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.sensibo.internal.dto.pods.Pod;

import com.google.gson.reflect.TypeToken;

/**
 * @author Arne Seime - Initial contribution
 */
public class GetPodsResponseTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testDeserialize() throws IOException {

        final Type type = new TypeToken<ArrayList<Pod>>() {
        }.getType();

        final List<Pod> rsp = wireHelper.deSerializeResponse("/get_pods_response.json", type);

        assertEquals(1, rsp.size());
        assertEquals("PODID", rsp.get(0).id);
    }
}