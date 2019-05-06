package org.openhab.binding.sensibo.internal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.sensibo.internal.dto.pods.Pod;

import com.google.gson.reflect.TypeToken;

public class GetPodsResponseTest extends AbstractSerializationDeserializationTest {

    @Test
    public void testDeserialize() throws IOException {

        Type type = new TypeToken<ArrayList<Pod>>() {
        }.getType();

        List<Pod> rsp = deSerialize("/get_pods_response.json", type);

        assertEquals(1, rsp.size());
        assertEquals("PODID", rsp.get(0).getId());
    }
}
