/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Factory for creating a GSON instance with the appropriate datatype converters for api calls
 * 
 * @author Arne Seime - Initial contribution
 */

public class GsonFactory {

    public static Gson create() {

        return new GsonBuilder().setPrettyPrinting().setLenient().create();
    }
}
