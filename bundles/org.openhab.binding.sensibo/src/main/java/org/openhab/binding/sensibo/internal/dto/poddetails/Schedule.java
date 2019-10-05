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
package org.openhab.binding.sensibo.internal.dto.poddetails;

import org.openhab.binding.sensibo.internal.model.SensiboSky;

/**
 * The {@link SensiboSky} represents a Sensibo Sky schedule
 *
 * @author Arne Seime - Initial contribution
 */
public class Schedule {
    public String targetLocalTime;
    public String nextTime;
    public String[] recurringDays;
    public AcState acState;
    public boolean enabled;

}
