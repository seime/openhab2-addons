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

/**
 * @author Arne Seime - Initial contribution
 */
public class ModeCapability {
    @SerializedName("swing")
    public List<String> swingModes;
    public Map<String, Temperature> temperatures;
    public List<String> fanLevels;
}