package org.openhab.binding.sensibo.internal;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.openhab.binding.sensibo.internal.dto.SetAcStateReponse;

public class SetAcStateResponseTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testDeserialize() throws IOException {

        SetAcStateReponse rsp = deSerialize("/set_acstate_response.json", SetAcStateReponse.class);

        assertNotNull(rsp.getAcState());

        assertTrue(rsp.getAcState().isOn());
    }

}
