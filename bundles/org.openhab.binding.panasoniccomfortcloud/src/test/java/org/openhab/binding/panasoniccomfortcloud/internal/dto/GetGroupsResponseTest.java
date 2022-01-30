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
package org.openhab.binding.panasoniccomfortcloud.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;

import com.google.gson.reflect.TypeToken;

/**
 * @author Arne Seime - Initial contribution
 */
public class GetGroupsResponseTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testDeserialize() throws IOException {
        final Type type = new TypeToken<GetGroupsResponse>() {
        }.getType();

        final GetGroupsResponse rsp = wireHelper.deSerializeFromClasspathResource("/get_groups_response.json", type);

        assertEquals(1, rsp.groupList.length);
        assertEquals(3, rsp.groupList[0].devices.length);
    }
}
