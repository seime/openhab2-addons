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
package org.openhab.binding.panasoniccomfortcloud.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.panasoniccomfortcloud.internal.dto.GroupDTO;

/**
 * @author Arne Seime - Initial contribution
 */
public class Group {
    private String id;
    private String name;
    private List<Device> devices = new ArrayList<>();

    public void addDevice(Device device) {
        devices.add(device);
    }

    public List<Device> getDevices() {
        return devices;
    }

    public Group() {
    }

    public void mergeFrom(GroupDTO dto) {
        this.id = dto.groupId;
        this.name = dto.groupName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
