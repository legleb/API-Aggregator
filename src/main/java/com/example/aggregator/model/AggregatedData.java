package com.example.aggregator.model;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AggregatedData {
    private String source;
    private String category;
    private String key;
    private Instant timestamp;
    private Map<String, Object> fields;

    public AggregatedData() {
        this.fields = new LinkedHashMap<>();
    }

    public AggregatedData(String source, String category, String key, Instant timestamp, Map<String, Object> fields) {
        this.source = source;
        this.category = category;
        this.key = key;
        this.timestamp = timestamp;
        this.fields = fields != null ? fields : new LinkedHashMap<>();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields != null ? fields : new LinkedHashMap<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AggregatedData that)) {
            return false;
        }
        return Objects.equals(source, that.source) && Objects.equals(category, that.category)
                && Objects.equals(key, that.key) && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, category, key, timestamp, fields);
    }

    @Override
    public String toString() {
        return "AggregatedData{" +
                "source='" + source + '\'' +
                ", category='" + category + '\'' +
                ", key='" + key + '\'' +
                ", timestamp=" + timestamp +
                ", fields=" + fields +
                '}';
    }
}