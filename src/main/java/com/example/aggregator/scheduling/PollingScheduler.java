package com.example.aggregator.scheduling;

import com.example.aggregator.model.WriteMode;
import com.example.aggregator.source.DataSource;
import com.example.aggregator.source.DataSourceRegistry;
import com.example.aggregator.storage.DataStorage;
import com.example.aggregator.storage.StorageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class PollingScheduler {
    private static final Logger log = LoggerFactory.getLogger(PollingScheduler.class);
    private final DataSourceRegistry registry;
    private ScheduledExecutorService executor;
    private boolean isRunning = false;
    private String currentFormat;
    private String currentFile;

    public PollingScheduler(DataSourceRegistry registry) {
        this.registry = registry;
    }

    public synchronized void start(PollingConfig config) {
        if (isRunning) {
            throw new IllegalStateException("Polling is already running");
        }

        log.info("Starting polling with n={}, t={}, mode={}", config.getN(), config.getT(), config.getMode());
        this.executor = Executors.newScheduledThreadPool(config.getN());
        this.currentFormat = config.getFormat();
        this.currentFile = config.getFile();

        DataStorage storage = StorageFactory.create(
                config.getFormat(),
                Path.of(config.getFile())
        );

        if ("create".equalsIgnoreCase(config.getMode())) {
            log.info("Clearing file before start (mode=create)");
            storage.write(List.of(), WriteMode.CREATE);
        }

        List<Runnable> tasks = new ArrayList<>();
        if (config.getSources() != null) {
            for (Map.Entry<String, List<Map<String, String>>> entry : config.getSources().entrySet()) {
                String sourceName = entry.getKey();
                DataSource source = registry.get(sourceName);
                for (Map<String, String> params : entry.getValue()) {
                    tasks.add(new PollingTask(source, params, storage, WriteMode.APPEND));
                }
            }
        }
        for (Runnable task : tasks) {
            executor.scheduleWithFixedDelay(task, 0, config.getT(), TimeUnit.SECONDS);
        }
        this.isRunning = true;
    }

    public synchronized void stop() {
        if (!isRunning) {
            return;
        }
        log.info("Stopping polling...");
        if (executor != null) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("Some tasks did not terminate in time");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.isRunning = false;
        log.info("Polling stopped.");
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getCurrentFormat() {
        return currentFormat;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    @PreDestroy
    public void onDestroy() {
        stop();
    }
}