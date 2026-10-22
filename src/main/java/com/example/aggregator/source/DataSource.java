package com.example.aggregator.source;

import com.example.aggregator.model.AggregatedData;
import java.util.List;
import java.util.Map;

public interface DataSource {
    String getName();

    String getCategory();

    List<AggregatedData> fetch(Map<String, String> params);
}