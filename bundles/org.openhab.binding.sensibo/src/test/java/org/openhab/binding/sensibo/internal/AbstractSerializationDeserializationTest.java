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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public abstract class AbstractSerializationDeserializationTest {

    private Gson gson;

    public AbstractSerializationDeserializationTest() {

        gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
            @Override
            public void write(JsonWriter out, ZonedDateTime value) throws IOException {
                out.value(value.toString());
            }

            @Override
            public ZonedDateTime read(JsonReader in) throws IOException {
                return ZonedDateTime.parse(in.nextString());
            }

        }).setPrettyPrinting().create();
    }

    protected <T> T deSerialize(String jsonClasspathName, Type type) throws IOException {
        String json = IOUtils
                .toString(AbstractSerializationDeserializationTest.class.getResourceAsStream(jsonClasspathName));

        JsonParser parser = new JsonParser();
        JsonObject o = parser.parse(json).getAsJsonObject();
        assertEquals("success", o.get("status").getAsString());

        return gson.fromJson(o.get("result"), type);

    }
}
