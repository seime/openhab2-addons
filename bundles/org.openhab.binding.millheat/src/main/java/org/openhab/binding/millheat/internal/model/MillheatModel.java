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
package org.openhab.binding.millheat.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MillheatModel} represents the home structure as designed by the user in the Millheat app.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class MillheatModel {
    private final long lastUpdated;
    private final List<Home> homes = new ArrayList<>();

    public MillheatModel(final long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void addHome(final Home home) {
        homes.add(home);
    }

    public List<Home> getHomes() {
        return homes;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public Optional<Heater> findHeaterByMac(final String macAddress) {
        if (macAddress == null) {
            return Optional.empty();
        }

        return findHeaters().filter(heater -> macAddress.equals(heater.getMacAddress())).findFirst();
    }

    private Stream<Heater> findHeaters() {
        return Stream.concat(
                homes.stream().flatMap(home -> home.getRooms().stream()).flatMap(room -> room.getHeaters().stream()),
                homes.stream().flatMap(room -> room.getIndependentHeaters().stream()));
    }

    public Optional<Room> findRoomById(final String id) {
        if (id == null) {
            return Optional.empty();
        }
        return homes.stream().flatMap(home -> home.getRooms().stream()).filter(room -> id.equals(room.getId()))
                .findFirst();
    }

    public Optional<Home> findHomeById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        return homes.stream().filter(e -> e.getId().equals(id)).findFirst();
    }
}
