package com.example.aggregator.source;

import com.example.aggregator.http.HttpClientWrapper;
import com.example.aggregator.model.AggregatedData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

public abstract class AbstractDataSource implements DataSource {
    protected final HttpClientWrapper http;
    protected final ObjectMapper mapper;

    protected AbstractDataSource(HttpClientWrapper http, ObjectMapper mapper) {
        this.http = http;
        this.mapper = mapper;
    }

    @Override
    public List<AggregatedData> fetch(Map<String, String> params) {
        String url = buildUrl(params);
        String json = http.get(url);

        try {
            JsonNode root = mapper.readTree(json);
            return mapToAggregated(root, params);
        } catch (JsonProcessingException e) {
            throw new DataSourceException("Failed to parse JSON response from " + getName(), e);
        }
    }

    protected abstract String buildUrl(Map<String, String> params);

    protected abstract List<AggregatedData> mapToAggregated(JsonNode root, Map<String, String> params);
}
