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
package org.openhab.binding.sensibo.internal.dto.pods;

import org.openhab.binding.sensibo.internal.dto.AbstractRequest;

/**
 * @author Arne Seime - Initial contribution
 */
public class GetPodsRequest extends AbstractRequest {
    @Override
    public String getRequestUrl() {
        return "/v2/users/me/pods";
    }

}