package com.example.aggregator.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StorageFactoryTest {
    private final StorageFactory factory = new StorageFactory(new ObjectMapper());

    @Test
    void testCreateJson() {
        assertInstanceOf(JsonDataStorage.class, factory.create("json", Path.of("t")));
    }

    @Test
    void testCreateCsv() {
        assertInstanceOf(CsvDataStorage.class, factory.create("csv", Path.of("t")));
    }

    @Test
    void testCreateNull() {
        assertThrows(IllegalArgumentException.class, () -> factory.create(null, Path.of("t")));
    }

    @Test
    void testCreateUnknown() {
        assertThrows(IllegalArgumentException.class, () -> factory.create("xml", Path.of("t")));
    }
}