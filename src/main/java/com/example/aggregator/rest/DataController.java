package com.example.aggregator.rest;

import com.example.aggregator.model.AggregatedData;
import com.example.aggregator.scheduling.PollingConfig;
import com.example.aggregator.scheduling.PollingScheduler;
import com.example.aggregator.storage.DataStorage;
import com.example.aggregator.storage.StorageFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/data")
public class DataController {
    private final PollingScheduler scheduler;
    private final PollingConfig defaultConfig;
    private final StorageFactory storageFactory;

    public DataController(PollingScheduler scheduler, PollingConfig defaultConfig, StorageFactory storageFactory) {
        this.scheduler = scheduler;
        this.defaultConfig = defaultConfig;
        this.storageFactory = storageFactory;
    }

    @GetMapping
    public ResponseEntity<List<AggregatedData>> getData(@RequestParam(required = false) String source) {
        String format = scheduler.getCurrentFormat() != null
                ? scheduler.getCurrentFormat()
                : defaultConfig.getFormat();

        String file = scheduler.getCurrentFile() != null
                ? scheduler.getCurrentFile()
                : defaultConfig.getFile();

        DataStorage storage = storageFactory.create(format, Path.of(file));
        List<AggregatedData> data = (source == null)
                ? storage.read()
                : storage.readBySource(source);

        return ResponseEntity.ok(data);
    }
}