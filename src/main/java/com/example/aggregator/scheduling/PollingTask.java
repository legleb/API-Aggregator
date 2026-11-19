package com.example.aggregator.scheduling;

import com.example.aggregator.http.HttpClientException;
import com.example.aggregator.model.AggregatedData;
import com.example.aggregator.model.WriteMode;
import com.example.aggregator.source.DataSource;
import com.example.aggregator.source.DataSourceException;
import com.example.aggregator.storage.DataStorage;
import com.example.aggregator.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class PollingTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(PollingTask.class);
    private final DataSource source;
    private final Map<String, String> params;
    private final DataStorage storage;
    private final WriteMode writeMode;

    public PollingTask(DataSource source, Map<String, String> params, DataStorage storage, WriteMode writeMode) {
        this.source = source;
        this.params = params;
        this.storage = storage;
        this.writeMode = writeMode;
    }

    @Override
    public void run() {
        log.info("[START] {} {}", source.getName(), params);
        try {
            List<AggregatedData> data = source.fetch(params);
            storage.write(data, writeMode);
            log.info("[END] {} {} OK", source.getName(), params);
        } catch (HttpClientException e) {
            log.info("[END] {} {} ERROR (network): {}", source.getName(), params, e.getMessage());
        } catch (DataSourceException e) {
            log.info("[END] {} {} ERROR (parsing): {}", source.getName(), params, e.getMessage());
        } catch (StorageException e) {
            log.info("[END] {} {} ERROR (storage): {}", source.getName(), params, e.getMessage());
        }
    }
}