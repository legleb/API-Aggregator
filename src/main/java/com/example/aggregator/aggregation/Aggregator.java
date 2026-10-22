package com.example.aggregator.aggregation;

import com.example.aggregator.http.HttpClientException;
import com.example.aggregator.model.AggregatedData;
import com.example.aggregator.model.WriteMode;
import com.example.aggregator.source.DataSource;
import com.example.aggregator.source.DataSourceException;
import com.example.aggregator.source.DataSourceRegistry;
import com.example.aggregator.storage.DataStorage;
import com.example.aggregator.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Aggregator {
    private static final Logger log = LoggerFactory.getLogger(Aggregator.class);
    private final DataSourceRegistry registry;
    private final DataStorage storage;

    public Aggregator(DataSourceRegistry registry, DataStorage storage) {
        this.registry = registry;
        this.storage = storage;
    }

    public void poll(List<String> sourceNames, Map<String, Map<String, String>> paramsBySource, WriteMode mode) {
        Map<String, Map<String, String>> params = paramsBySource != null ? paramsBySource : new HashMap<>();
        boolean isFirstSource = true;

        for (String name : sourceNames) {
            try {
                DataSource source = registry.get(name);
                Map<String, String> sourceParams = params.getOrDefault(name, Map.of());
                List<AggregatedData> data = source.fetch(sourceParams);

                WriteMode actualMode = (isFirstSource && mode == WriteMode.CREATE) ? WriteMode.CREATE : WriteMode.APPEND;

                storage.write(data, actualMode);
                isFirstSource = false;
                log.info("Successfully polled source: {} ({} records)", name, data.size());
            } catch (IllegalArgumentException e) {
                log.warn("Source not found: '{}'", name);
            } catch (HttpClientException e) {
                log.warn("Network error for source '{}': {}", name, e.getMessage());
            } catch (DataSourceException e) {
                log.warn("Failed to fetch data from source '{}': {}", name, e.getMessage());
            } catch (StorageException e) {
                log.warn("Failed to save data from source '{}': {}", name, e.getMessage());
            }
        }
    }
}
