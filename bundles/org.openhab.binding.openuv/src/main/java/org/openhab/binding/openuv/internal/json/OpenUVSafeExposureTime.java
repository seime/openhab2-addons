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
package org.openhab.binding.openuv.internal.json;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;

/**
 * Wrapper type around values reported by OpenUV safe exposure time.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class OpenUVSafeExposureTime {

    private int st1;
    private int st2;
    private int st3;
    private int st4;
    private int st5;
    private int st6;

    public QuantityType<?> getSafeExposure(int index) {
        int result;
        switch (index) {
            case 1:
                result = st1;
            case 2:
                result = st2;
            case 3:
                result = st3;
            case 4:
                result = st4;
            case 5:
                result = st5;
            default:
                result = st6;
        }
        return new QuantityType<>(result, SmartHomeUnits.MINUTE);
    }
}
