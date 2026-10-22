package com.example.aggregator.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;

public final class StorageFactory {
    private StorageFactory() {}

    public static DataStorage create(String format, Path file, ObjectMapper mapper) {
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
