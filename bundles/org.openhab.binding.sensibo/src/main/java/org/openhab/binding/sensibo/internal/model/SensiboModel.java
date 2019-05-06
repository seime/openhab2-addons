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
package org.openhab.binding.sensibo.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link SensiboModel} represents the home structure as designed by the user in the Sensibo app.
 *
 * @author Arne Seime - Initial contribution
 */
public class SensiboModel {
    private long lastUpdated;
    private List<SensiboSky> pods = new ArrayList<>();

    public SensiboModel(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void addPod(SensiboSky pod) {
        pods.add(pod);
    }

    public List<SensiboSky> getPods() {
        return pods;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public Optional<@NonNull SensiboSky> findSensiboSkyByMacAddress(String macAddress) {
        String macAddressWithoutColons = StringUtils.remove(macAddress, ':');
        return pods.stream().filter(pod -> macAddressWithoutColons.equals(pod.getMacAddress())).findFirst();
    }

}
