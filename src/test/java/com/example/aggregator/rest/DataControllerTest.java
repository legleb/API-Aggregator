package com.example.aggregator.rest;

import com.example.aggregator.scheduling.PollingConfig;
import com.example.aggregator.scheduling.PollingScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataController.class)
class DataControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PollingScheduler scheduler;

    @MockBean
    private PollingConfig defaultConfig;

    @Test
    void testGetData() throws Exception {
        when(scheduler.getCurrentFormat()).thenReturn("json");
        when(scheduler.getCurrentFile()).thenReturn("src/test/resources/test-data.json");

        mockMvc.perform(get("/data")).andExpect(status().isOk());
    }

    @Test
    void testGetDataWithSourceFilter() throws Exception {
        when(scheduler.getCurrentFormat()).thenReturn("json");
        when(scheduler.getCurrentFile()).thenReturn("src/test/resources/test-data.json");

        mockMvc.perform(get("/data?source=open-meteo")).andExpect(status().isOk());
    }
}