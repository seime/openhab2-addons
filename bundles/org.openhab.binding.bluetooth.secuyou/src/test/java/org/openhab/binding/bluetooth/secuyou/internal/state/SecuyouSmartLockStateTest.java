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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test cases
 * 
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SecuyouSmartLockStateTest {

    @Test
    void testGenerateChallengeResponse() {
        byte[] challenge = DatatypeConverter.parseHexBinary("FCE8C4904CF87611BE3A79B75CC24650");
        SecuyouSmartLockState lock = new SecuyouSmartLockState(false);
        lock.setChallenge(challenge);
        byte[] challengeResponse = lock.generateChallengeResponse("12345", "AA7E151628AED2A6ABF7158809CF4F3C");
        byte[] expectedChallengeResponse = DatatypeConverter.parseHexBinary("1B440138FC47F84D7B24905988C652E9");

        assertArrayEquals(expectedChallengeResponse, challengeResponse);
    }

    @Test
    void testParseStrangeStates() {
        // LOCK POSITION - PINCODE_CORRECT - BATTERY - HANDLE_POS - HOMELOCK/RESQUE_HPR

        byte[] firstUpdate = DatatypeConverter
                .parseHexBinary("00-10-00-02-01-00-F6BB62426A41B2AA236A".replace("-", ""));
        SecuyouSmartLockState state = new SecuyouSmartLockState(false);
        state.setLockStatus(firstUpdate);

        // next
        byte[] secondUpdate = DatatypeConverter
                .parseHexBinary("00-10-00-01-01-00-F6BB62426A41B2AA236A".replace("-", ""));
        state.setLockStatus(secondUpdate);

        // undefined lock state
        byte[] thirdUpdate = DatatypeConverter
                .parseHexBinary("02-10-00-01-01-00-F6BB62426A41B2AA236A".replace("-", ""));
        state.setLockStatus(thirdUpdate);

        byte[] fourth = DatatypeConverter.parseHexBinary("00-10-00-01-03-00-5B67DC7AA78381F2AD3D".replace("-", ""));
        state.setLockStatus(fourth);
    }
}
