package com.example.aggregator.source;

import java.util.HashMap;
import java.util.Map;

public class DataSourceRegistry {
    private final Map<String, DataSource> sources = new HashMap<>();

    public void register(DataSource source) {
        sources.put(source.getName(), source);
    }

    public DataSource get(String name) {
        DataSource src = sources.get(name);
        if (src == null) {
            throw new IllegalArgumentException("Unknown source: " + name);
        }
        return src;
    }
}
