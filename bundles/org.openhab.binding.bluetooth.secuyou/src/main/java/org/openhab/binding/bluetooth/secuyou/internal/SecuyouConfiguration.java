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
package org.openhab.binding.bluetooth.secuyou.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration class for Smart Lock device.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SecuyouConfiguration {
    public String address = "";

    public String pinCode = "";

    public String encryptionKey = "";

    public int keepAliveSeconds = 600;

    public boolean attemptLockRescue = false;

    public boolean treatLockingInProgressAsLocked = false;

    @Override
    public String toString() {
        return "SecuyouConfiguration{" + "address='" + address + '\'' + ", attemptLockRescue=" + attemptLockRescue
                + ", encryptionKey='<hidden>'" + ", keepAliveSeconds=" + keepAliveSeconds + ", pinCode='<hidden>'"
                + ", treatLockingInProgressAsLocked=" + treatLockingInProgressAsLocked + '}';
    }
}
