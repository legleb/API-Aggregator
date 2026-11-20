package com.example.aggregator.rest;

import com.example.aggregator.scheduling.PollingConfig;
import com.example.aggregator.scheduling.PollingScheduler;
import com.example.aggregator.storage.DataStorage;
import com.example.aggregator.storage.StorageFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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

    @MockBean
    private StorageFactory storageFactory;

    @ParameterizedTest
    @ValueSource(strings = {
            "/data",
            "/data?source=open-meteo"
    })
    void testGetDataWithAndWithoutFilter(String url) throws Exception {
        DataStorage storageMock = mock(DataStorage.class);
        when(storageMock.read()).thenReturn(List.of());
        when(storageMock.readBySource(anyString())).thenReturn(List.of());
        when(storageFactory.create(anyString(), any())).thenReturn(storageMock);
        when(scheduler.getCurrentFormat()).thenReturn("json");
        when(scheduler.getCurrentFile()).thenReturn("src/test/resources/test-data.json");
        mockMvc.perform(get(url)).andExpect(status().isOk());
    }

    @Test
    void testGetDataFallbackToDefaultConfig() throws Exception {
        DataStorage storageMock = mock(DataStorage.class);
        when(storageMock.read()).thenReturn(List.of());
        when(storageFactory.create(anyString(), any())).thenReturn(storageMock);
        when(scheduler.getCurrentFormat()).thenReturn(null);
        when(scheduler.getCurrentFile()).thenReturn(null);
        when(defaultConfig.getFormat()).thenReturn("json");
        when(defaultConfig.getFile()).thenReturn("data/data.json");

        mockMvc.perform(get("/data")).andExpect(status().isOk());
    }
}