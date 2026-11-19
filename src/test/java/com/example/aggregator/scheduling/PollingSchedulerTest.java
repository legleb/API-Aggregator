package com.example.aggregator.scheduling;

import com.example.aggregator.source.DataSource;
import com.example.aggregator.source.DataSourceRegistry;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PollingSchedulerTest {

    @Test
    void testStartAndStop() throws InterruptedException {
        DataSourceRegistry registry = mock(DataSourceRegistry.class);
        DataSource source = mock(DataSource.class);
        when(registry.get("mock")).thenReturn(source);
        when(source.getName()).thenReturn("mock");

        CountDownLatch latch = new CountDownLatch(1);
        when(source.fetch(anyMap())).thenAnswer(inv -> { latch.countDown(); return List.of(); });

        PollingScheduler scheduler = new PollingScheduler(registry);
        PollingConfig config = new PollingConfig();
        config.setN(1); config.setT(1); config.setFormat("json"); config.setFile("target/t.json");
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
        PollingScheduler scheduler = new PollingScheduler(registry);

        PollingConfig config = new PollingConfig();
        config.setN(1); config.setT(1); config.setFormat("json"); config.setFile("target/t.json");
        config.setSources(Map.of());

        scheduler.start(config);
        assertThrows(IllegalStateException.class, () -> scheduler.start(config));
    }

    @Test
    void testStopWhenNotRunning() {
        DataSourceRegistry registry = mock(DataSourceRegistry.class);
        PollingScheduler scheduler = new PollingScheduler(registry);

        scheduler.stop();
        assertFalse(scheduler.isRunning());
    }
}