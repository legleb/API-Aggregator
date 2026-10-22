package com.example.aggregator.storage;

import com.example.aggregator.model.AggregatedData;
import com.example.aggregator.model.WriteMode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvDataStorage implements DataStorage {
    private static final String[] HEADERS = {"source", "category", "key", "timestamp", "fields"};
    private final Path file;
    private final ObjectMapper mapper;

    public CsvDataStorage(Path file, ObjectMapper mapper) {
        this.file = file;
        this.mapper = mapper;
    }

    @Override
    public synchronized void write(List<AggregatedData> data, WriteMode mode) {
        if (data == null || data.isEmpty()) {
            return;
        }

        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            boolean writeHeader = mode == WriteMode.CREATE || !Files.exists(file) || Files.size(file) == 0;

            StandardOpenOption[] options = (mode == WriteMode.CREATE)? new StandardOpenOption[]{
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE}
                    : new StandardOpenOption[]{
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE};

            try (BufferedWriter writer = Files.newBufferedWriter(file, options);
                 CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                         .setHeader(writeHeader ? HEADERS : null).build())) {

                for (AggregatedData d : data) {
                    printer.printRecord(d.getSource(), d.getCategory(), d.getKey(),
                            d.getTimestamp() != null ? d.getTimestamp().toString() : "",
                            mapper.writeValueAsString(d.getFields())
                    );
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to write CSV to " + file, e);
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
        List<AggregatedData> result = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(file);
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true)
                     .build().parse(reader)) {

            for (CSVRecord record : parser) {
                String source = record.get("source");
                String category = record.get("category");
                String key = record.get("key");
                String timestampStr = record.get("timestamp");
                String fieldsJson = record.get("fields");

                Instant ts = timestampStr.isBlank() ? null : Instant.parse(timestampStr);
                Map<String, Object> fields = mapper.readValue(fieldsJson,
                        new TypeReference<LinkedHashMap<String, Object>>() { }
                );
                result.add(new AggregatedData(source, category, key, ts, fields));
            }
        } catch (IOException | DateTimeParseException e) {
            throw new StorageException("Failed to read CSV from " + file, e);
        }
        return result;
    }
}
