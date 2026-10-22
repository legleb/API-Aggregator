package com.example.aggregator.storage;

import com.example.aggregator.model.AggregatedData;
import com.example.aggregator.model.WriteMode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonDataStorage implements DataStorage {
    private final Path file;
    private final ObjectMapper mapper;

    public JsonDataStorage(Path file, ObjectMapper mapper) {
        this.file = file;
        this.mapper = mapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public synchronized void write(List<AggregatedData> data, WriteMode mode) {
        if (data == null || data.isEmpty()) {
            return;
        }

        List<AggregatedData> existing = new ArrayList<>();
        if (mode == WriteMode.APPEND && Files.exists(file)) {
            existing = readInternal();
        }

        existing.addAll(data);
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            mapper.writeValue(file.toFile(), existing);
        } catch (IOException e) {
            throw new StorageException("Failed to write JSON to " + file, e);
        }
    }

    @Override
    public List<AggregatedData> read() {
        if (!Files.exists(file)) {
            return List.of();
        }
        return readInternal();
    }

    @Override
    public List<AggregatedData> readBySource(String source) {
        List<AggregatedData> allData = read();
        List<AggregatedData> result = new ArrayList<>();
        for (AggregatedData d : allData) {
            if (d.getSource().equals(source)) {
                result.add(d);
            }
        }
        return result;
    }

    private List<AggregatedData> readInternal() {
        try {
            return mapper.readValue(file.toFile(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new StorageException("Failed to read JSON from " + file, e);
        }
    }
}
