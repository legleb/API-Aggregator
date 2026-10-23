package com.example.aggregator.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.file.Path;

public final class StorageFactory {
    private StorageFactory() {}

    public static DataStorage create(String format, Path file) {
        if (format == null) {
            throw new IllegalArgumentException("Format is null");
        }
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return switch (format.toLowerCase()) {
            case "json" -> new JsonDataStorage(file, mapper);
            case "csv"  -> new CsvDataStorage(file, mapper);
            default -> throw new IllegalArgumentException("Unknown format: " + format);
        };
    }
}
