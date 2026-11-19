package com.example.aggregator.storage;

import com.example.aggregator.model.AggregatedData;
import com.example.aggregator.model.WriteMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CsvDataStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void testWriteAndRead() {
        Path file = tempDir.resolve("test.csv");
        CsvDataStorage storage = new CsvDataStorage(file, new ObjectMapper().registerModule(new JavaTimeModule()));

        storage.write(List.of(new AggregatedData("A", "c", "k", Instant.now(), Map.of("val", 1))), WriteMode.CREATE);
        List<AggregatedData> result = storage.read();

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getSource());
    }

    @Test
    void testAppend() {
        Path file = tempDir.resolve("test2.csv");
        CsvDataStorage storage = new CsvDataStorage(file, new ObjectMapper().registerModule(new JavaTimeModule()));

        storage.write(List.of(new AggregatedData("A", "c", "k", Instant.now(), Map.of())), WriteMode.CREATE);
        storage.write(List.of(new AggregatedData("B", "c", "k", Instant.now(), Map.of())), WriteMode.APPEND);

        assertEquals(2, storage.read().size());
    }

    @Test
    void testReadNonExistentFile() {
        Path file = tempDir.resolve("missing.csv");
        CsvDataStorage storage = new CsvDataStorage(file, new ObjectMapper());
        assertTrue(storage.read().isEmpty());
    }
}