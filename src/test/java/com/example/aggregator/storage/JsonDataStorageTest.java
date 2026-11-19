package com.example.aggregator.storage;

import com.example.aggregator.model.AggregatedData;
import com.example.aggregator.model.WriteMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class JsonDataStorageTest {

    @TempDir
    Path tempDir;

    private ObjectMapper createMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void testWriteAndRead() {
        Path file = tempDir.resolve("test.json");
        JsonDataStorage storage = new JsonDataStorage(file, createMapper());

        storage.write(List.of(new AggregatedData("A", "c", "k", Instant.now(), Map.of())), WriteMode.CREATE);
        List<AggregatedData> result = storage.read();

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getSource());
    }

    @Test
    void testAppend() {
        Path file = tempDir.resolve("test2.json");
        JsonDataStorage storage = new JsonDataStorage(file, createMapper());

        storage.write(List.of(new AggregatedData("A", "c", "k", Instant.now(), Map.of())), WriteMode.CREATE);
        storage.write(List.of(new AggregatedData("B", "c", "k", Instant.now(), Map.of())), WriteMode.APPEND);

        assertEquals(2, storage.read().size());
    }

    @Test
    void testReadNonExistentFile() {
        Path file = tempDir.resolve("missing.json");
        JsonDataStorage storage = new JsonDataStorage(file, createMapper());
        assertTrue(storage.read().isEmpty());
    }

    @Test
    void testWriteEmptyListWithCreate() throws IOException {
        Path file = tempDir.resolve("empty.json");
        JsonDataStorage storage = new JsonDataStorage(file, createMapper());
        storage.write(List.of(), WriteMode.CREATE);
        assertTrue(Files.exists(file));
        String content = Files.readString(file).trim();
        assertEquals("[ ]", content);
    }

    @Test
    void testWriteEmptyListWithAppend() {
        Path file = tempDir.resolve("empty_append.json");
        JsonDataStorage storage = new JsonDataStorage(file, createMapper());
        storage.write(List.of(), WriteMode.APPEND);
        assertFalse(Files.exists(file));
    }

    @Test
    void testReadCorruptedFile() throws IOException {
        Path file = tempDir.resolve("corrupt.json");
        Files.writeString(file, "bad json");
        JsonDataStorage storage = new JsonDataStorage(file, createMapper());
        assertThrows(StorageException.class, storage::read);
    }
}