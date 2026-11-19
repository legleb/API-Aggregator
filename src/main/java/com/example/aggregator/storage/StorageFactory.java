package com.example.aggregator.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class StorageFactory {
    private final ObjectMapper mapper;

    public StorageFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public DataStorage create(String format, Path file) {
        if (format == null) {
            throw new IllegalArgumentException("Format is null");
        }
        return switch (format.toLowerCase()) {
            case "json" -> new JsonDataStorage(file, mapper);
            case "csv"  -> new CsvDataStorage(file, mapper);
            default -> throw new IllegalArgumentException("Unknown format: " + format);
        };
    }
}