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
import java.util.Optional;

/**
 * @author Arne Seime - Initial contribution
 */
public class GroupModel {
    private final long lastUpdated;

    public GroupModel(final long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<Group> getGroups() {
        return groups;
    }

    private final List<Group> groups = new ArrayList<>();

    public void addGroup(final Group group) {
        groups.add(group);
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public Optional<Device> findDeviceByDeviceId(String deviceId) {
        return groups.stream().flatMap(e -> e.getDevices().stream()).filter(e -> deviceId.equals(e.getDeviceId()))
                .findFirst();
    }

    public void clear() {
        groups.clear();
    }
}
