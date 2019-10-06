package org.openhab.binding.sensibo.internal.handler;

import static org.junit.Assert.*;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.Thing;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.sensibo.internal.SensiboCommunicationException;
import org.openhab.binding.sensibo.internal.WireHelper;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetails;
import org.openhab.binding.sensibo.internal.handler.SensiboSkyHandler.StateChange;
import org.openhab.binding.sensibo.internal.model.SensiboSky;

public class SensiboSkyHandlerTest {

    private WireHelper wireHelper = new WireHelper();

    @Mock
    private Thing thing;

    @Test
    public void testStateChangeValidation() throws IOException, SensiboCommunicationException {
        final PodDetails rsp = wireHelper.deSerializeResponse("/get_pod_details_response.json", PodDetails.class);
        SensiboSky sky = new SensiboSky(rsp);
        SensiboSkyHandler handler = new SensiboSkyHandler(thing);

        // Target temperature
        StateChange stateChangeCheck = handler.checkStateChangeValid(sky, "targetTemperature", 123);
        assertFalse(stateChangeCheck.valid);
        assertNotNull(stateChangeCheck.validationMessage);
        assertTrue(handler.checkStateChangeValid(sky, "targetTemperature", 10).valid);

        // Mode
        StateChange stateChangeCheckMode = handler.checkStateChangeValid(sky, "mode", "invalid");
        assertFalse(stateChangeCheckMode.valid);
        assertNotNull(stateChangeCheckMode.validationMessage);
        assertTrue(handler.checkStateChangeValid(sky, "mode", "auto").valid);

        // Swing
        StateChange stateChangeCheckSwing = handler.checkStateChangeValid(sky, "swing", "invalid");
        assertFalse(stateChangeCheckSwing.valid);
        assertNotNull(stateChangeCheckSwing.validationMessage);
        assertTrue(handler.checkStateChangeValid(sky, "swing", "stopped").valid);

        // FanLevel
        StateChange stateChangeCheckFanLevel = handler.checkStateChangeValid(sky, "fanLevel", "invalid");
        assertFalse(stateChangeCheckFanLevel.valid);
        assertNotNull(stateChangeCheckFanLevel.validationMessage);
        assertTrue(handler.checkStateChangeValid(sky, "fanLevel", "high").valid);

    }
}
