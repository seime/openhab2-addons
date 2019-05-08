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
package org.openhab.binding.sensibo.internal.dto.poddetails;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class ModeCapability {
    @SerializedName("swing")
    private List<String> swingModes;
    private Map<String, Temperature> temperatures;
    private List<String> fanLevels;

    public List<String> getSwingModes() {
        return swingModes;
    }

    public Map<String, Temperature> getTemperatures() {
        return temperatures;
    }

    public List<String> getFanLevels() {
        return fanLevels;
    }
}
