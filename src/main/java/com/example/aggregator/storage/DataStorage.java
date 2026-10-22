package com.example.aggregator.storage;

import com.example.aggregator.model.AggregatedData;
import com.example.aggregator.model.WriteMode;
import java.util.List;

public interface DataStorage {
    void write(List<AggregatedData> data, WriteMode mode);

    List<AggregatedData> read();

    List<AggregatedData> readBySource(String source);
}
