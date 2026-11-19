package com.example.aggregator.storage;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class StorageFactoryTest {
    @Test
    void testCreateJson() {
        assertTrue(StorageFactory.create("json", Path.of("t")) instanceof JsonDataStorage);
    }
    @Test
    void testCreateCsv() {
        assertTrue(StorageFactory.create("csv", Path.of("t")) instanceof CsvDataStorage);
    }
    @Test
    void testCreateNull() {
        assertThrows(IllegalArgumentException.class, () -> StorageFactory.create(null, Path.of("t")));
    }
    @Test
    void testCreateUnknown() {
        assertThrows(IllegalArgumentException.class, () -> StorageFactory.create("xml", Path.of("t")));
    }
}