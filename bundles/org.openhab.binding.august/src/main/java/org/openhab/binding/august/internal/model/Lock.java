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
package org.openhab.binding.august.internal.model;

import org.openhab.binding.august.internal.dto.LockDTO;

/**
 * The {@link Lock} represents a simplified view of a lock, used for discovery
 *
 * @author Arne Seime - Initial contribution
 */
public class Lock {

    private String houseName;

    private String lockName;

    public Lock(LockDTO dto) {
        this.houseName = dto.houseName;
        this.lockName = dto.lockName;
    }

    public String getHouseName() {
        return houseName;
    }

    public String getLockName() {
        return lockName;
    }
}
