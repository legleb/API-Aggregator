package com.example.aggregator.rest.dto;

import com.example.aggregator.model.WriteMode;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DtoTest {
    @Test
    void testMessageResponse() {
        MessageResponse msg = new MessageResponse("test");
        assertEquals("test", msg.getMessage());
    }

    @Test
    void testPollingStatusResponse() {
        List<String> activeSources = List.of("open-meteo", "world-bank");
        PollingStatusResponse res = new PollingStatusResponse(true, 3, 10, activeSources);
        assertTrue(res.isRunning());
        assertEquals(3, res.getN());
        assertEquals(10, res.getT());
        assertEquals(2, res.getActiveSources().size());
        assertEquals("open-meteo", res.getActiveSources().get(0));
    }

    @Test
    void testStartPollingRequest() {
        StartPollingRequest req = new StartPollingRequest();
        req.setN(2);
        req.setT(5);
        req.setFormat("csv");
        req.setFile("test.csv");
        req.setMode(WriteMode.CREATE);
        req.setSources(Map.of("src", List.of(Map.of("k", "v"))));

        assertEquals(2, req.getN());
        assertEquals(5, req.getT());
        assertEquals("csv", req.getFormat());
        assertEquals("test.csv", req.getFile());
        assertEquals(WriteMode.CREATE, req.getMode());
        assertEquals(1, req.getSources().size());
    }
}