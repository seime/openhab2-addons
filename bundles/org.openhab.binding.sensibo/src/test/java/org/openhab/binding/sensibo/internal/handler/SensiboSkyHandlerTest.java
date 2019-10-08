package org.openhab.binding.sensibo.internal.handler;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.sensibo.internal.SensiboCommunicationException;
import org.openhab.binding.sensibo.internal.WireHelper;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetails;
import org.openhab.binding.sensibo.internal.handler.SensiboSkyHandler.StateChange;
import org.openhab.binding.sensibo.internal.model.SensiboSky;

public class SensiboSkyHandlerTest {

    private WireHelper wireHelper = new WireHelper();

    @Test
    public void testStateChangeValidation() throws IOException, SensiboCommunicationException {
        final PodDetails rsp = wireHelper.deSerializeResponse("/get_pod_details_response.json", PodDetails.class);
        SensiboSky sky = new SensiboSky(rsp);
        Thing thing = Mockito.mock(Thing.class);
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

    @Test
    public void testAddDynamicChannelsMarco() throws IOException, SensiboCommunicationException {
        testAddDynamicChannels("/get_pod_details_response_marco.json");
    }

    @Test
    public void testAddDynamicChannels() throws IOException, SensiboCommunicationException {
        testAddDynamicChannels("/get_pod_details_response.json");
    }

    private void testAddDynamicChannels(String podDetailsResponse) throws IOException, SensiboCommunicationException {
        final PodDetails rsp = wireHelper.deSerializeResponse(podDetailsResponse, PodDetails.class);
        SensiboSky sky = new SensiboSky(rsp);
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getUID()).thenReturn(new ThingUID("sensibo:account:thinguid"));
        SensiboSkyHandler handler = Mockito.spy(new SensiboSkyHandler(thing));
        List<@NonNull Channel> dynamicChannels = handler.createDynamicChannels(sky);
        assertTrue(dynamicChannels.size() > 0);
    }
}
