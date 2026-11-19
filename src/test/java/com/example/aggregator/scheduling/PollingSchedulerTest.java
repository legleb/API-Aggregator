package com.example.aggregator.scheduling;

import com.example.aggregator.model.WriteMode;
import com.example.aggregator.source.DataSource;
import com.example.aggregator.source.DataSourceRegistry;
import com.example.aggregator.storage.StorageFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PollingSchedulerTest {
    private StorageFactory createStorageFactory() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return new StorageFactory(mapper);
    }

    @Test
    void testStartAndStop() throws InterruptedException {
        DataSourceRegistry registry = mock(DataSourceRegistry.class);
        DataSource source = mock(DataSource.class);
        when(registry.get("mock")).thenReturn(source);
        when(source.getName()).thenReturn("mock");

        CountDownLatch latch = new CountDownLatch(1);
        when(source.fetch(anyMap())).thenAnswer(inv -> { latch.countDown(); return List.of(); });

        PollingScheduler scheduler = new PollingScheduler(registry, createStorageFactory());

        PollingConfig config = new PollingConfig();
        config.setN(1);
        config.setT(1);
        config.setFormat("json");
        config.setFile("target/t.json");
        config.setMode(WriteMode.CREATE);
        config.setSources(Map.of("mock", List.of(Map.of("k", "v"))));

        scheduler.start(config);
        assertTrue(scheduler.isRunning());
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        scheduler.stop();
        assertFalse(scheduler.isRunning());
    }

    @Test
    void testStartAlreadyRunning() {
        DataSourceRegistry registry = mock(DataSourceRegistry.class);
        PollingScheduler scheduler = new PollingScheduler(registry, createStorageFactory());

        PollingConfig config = new PollingConfig();
        config.setN(1);
        config.setT(1);
        config.setFormat("json");
        config.setFile("target/t.json");
        config.setMode(WriteMode.CREATE);
        config.setSources(Map.of());

        scheduler.start(config);
        assertThrows(IllegalStateException.class, () -> scheduler.start(config));
        scheduler.stop();
    }

    @Test
    void testStopWhenNotRunning() {
        DataSourceRegistry registry = mock(DataSourceRegistry.class);
        PollingScheduler scheduler = new PollingScheduler(registry, createStorageFactory());

        scheduler.stop();
        assertFalse(scheduler.isRunning());
    }
}