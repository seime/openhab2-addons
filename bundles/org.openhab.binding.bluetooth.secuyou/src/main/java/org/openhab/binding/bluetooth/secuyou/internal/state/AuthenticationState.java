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
package org.openhab.binding.bluetooth.secuyou.internal.state;

/**
 * Enum defining client (binding) authentication states
 * 
 * @author Arne Seime - Initial contribution
 */
public enum AuthenticationState {

    UNAUTHENTICATED, // Initial mode and default mode if pin/key is missing
    AUTHENTICATION_IN_PROGRESS, // Authentication in progress (if pin/key is provided)
    AUTHENTICATED // Authenticated using pin/key
}
