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
package org.openhab.binding.august.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AccountConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class AccountConfiguration {

    @Nullable
    public String phone;

    @Nullable
    public String email;

    @Nullable
    public String password;

    @Nullable
    public String validationCode;

    public int refreshIntervalSeconds = 3600;

    @java.lang.Override
    public java.lang.String toString() {
        return "AccountConfiguration{" + "email='" + email + '\'' + ", password='REDACTED'" + ", phone='" + phone + '\''
                + ", refreshIntervalSeconds=" + refreshIntervalSeconds + '}';
    }
}
