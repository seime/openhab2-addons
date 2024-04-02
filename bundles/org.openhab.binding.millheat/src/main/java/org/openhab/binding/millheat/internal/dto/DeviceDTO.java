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
package org.openhab.binding.millheat.internal.dto;

/**
 * The {@link DeviceDTO} class represents a heater device
 *
 * @author Arne Seime - Initial contribution
 */
public class DeviceDTO {
    public String deviceId;

    public String macAddress;

    public boolean isConnected;

    public String customName;

    public String roomId;

    public String houseId;

    public boolean isEnabled;

    public MetricsDTO lastMetrics;

    public DeviceSettingsDTO deviceSettings;
}
