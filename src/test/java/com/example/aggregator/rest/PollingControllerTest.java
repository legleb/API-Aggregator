package com.example.aggregator.rest;

import com.example.aggregator.scheduling.PollingConfig;
import com.example.aggregator.scheduling.PollingScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PollingController.class)
class PollingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PollingScheduler scheduler;

    @MockBean
    private PollingConfig defaultConfig;

    @Test
    void testStartSuccess() throws Exception {
        when(scheduler.isRunning()).thenReturn(false);
        when(defaultConfig.getN()).thenReturn(3);
        when(defaultConfig.getT()).thenReturn(10);
        mockMvc.perform(post("/polling/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Polling started"));
    }

    @Test
    void testStartConflict() throws Exception {
        when(scheduler.isRunning()).thenReturn(true);
        mockMvc.perform(post("/polling/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void testStopSuccess() throws Exception {
        when(scheduler.isRunning()).thenReturn(true);
        mockMvc.perform(post("/polling/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Polling stopped"));
    }

    @Test
    void testStopNotRunning() throws Exception {
        when(scheduler.isRunning()).thenReturn(false);
        mockMvc.perform(post("/polling/stop"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testStatus() throws Exception {
        when(scheduler.isRunning()).thenReturn(true);
        when(defaultConfig.getN()).thenReturn(3);
        when(defaultConfig.getT()).thenReturn(10);
        mockMvc.perform(get("/polling/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(true))
                .andExpect(jsonPath("$.n").value(3))
                .andExpect(jsonPath("$.t").value(10))
                .andExpect(jsonPath("$.activeSources").isArray());
    }

    @Test
    void testStartValidationError() throws Exception {
        when(scheduler.isRunning()).thenReturn(false);
        doThrow(new IllegalArgumentException("n must be positive"))
                .when(scheduler).start(any());
        mockMvc.perform(post("/polling/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"n\": 0}"))
                .andExpect(status().isBadRequest());
    }
}