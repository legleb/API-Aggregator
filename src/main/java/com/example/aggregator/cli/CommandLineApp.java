package com.example.aggregator.cli;

import com.example.aggregator.aggregation.Aggregator;
import com.example.aggregator.model.AggregatedData;
import com.example.aggregator.source.DataSourceRegistry;
import com.example.aggregator.storage.DataStorage;
import com.example.aggregator.storage.StorageException;
import com.example.aggregator.storage.StorageFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CommandLineApp {
    private static final Logger log = LoggerFactory.getLogger(CommandLineApp.class);
    private static final String DATA_DIR = "data";
    private final DataSourceRegistry registry;
    private final CommandParser parser;

    public CommandLineApp(DataSourceRegistry registry, CommandParser parser) {
        this.registry = registry;
        this.parser = parser;
    }

    public void run(String[] args) {
        Command command;
        try {
            command = parser.parse(args);
        } catch (CliException e) {
            log.error("Error parsing command: {}", e.getMessage());
            printHelp();
            return;
        }

        try {
            switch (command.getType()) {
                case POLL -> handlePoll(command);
                case PRINT -> handlePrint(readAllFormats(null));
                case PRINT_BY_SOURCE -> handlePrint(readAllFormats(command.getSourceFilter()));
                case HELP -> printHelp();
            }
        } catch (IllegalArgumentException | StorageException e) {
            log.error("Execution failed: {}", e.getMessage(), e);
        }
    }

    private void handlePoll(Command command) {
        String format = command.getFormat() != null ? command.getFormat() : "json";
        Path file = Path.of(DATA_DIR, "data." + format);
        DataStorage storage = StorageFactory.create(format, file);
        Aggregator aggregator = new Aggregator(registry, storage);

        aggregator.poll(command.getSources(), command.getParamsBySource(), command.getMode());

        log.info("Poll completed for sources: {} (format={}, file={})", command.getSources(), format, file);
    }

    private List<AggregatedData> readAllFormats(String sourceFilter) {
        List<AggregatedData> result = new ArrayList<>();

        for (String format : List.of("json", "csv")) {
            Path file = Path.of(DATA_DIR, "data." + format);
            if (!java.nio.file.Files.exists(file)) {
                continue;
            }
            try {
                DataStorage storage = StorageFactory.create(format, file);
                List<AggregatedData> data = (sourceFilter == null) ? storage.read() : storage.readBySource(sourceFilter);
                result.addAll(data);
            } catch (StorageException | IllegalArgumentException e) {
                log.warn("Failed to read {}: {}", file, e.getMessage());
            }
        }
        return result;
    }

    private void handlePrint(List<AggregatedData> data) {
        if (data.isEmpty()) {
            log.info("(no data)");
            return;
        }
        for (AggregatedData d : data) {
            log.info("{}", d);
        }
        log.info("Total: {}", data.size());
    }

    private void printHelp() {
        System.out.println("""
                Usage:
                  poll --sources=open-meteo,open-library,world-bank --format=json --mode=create
                  poll --sources=open-meteo,open-library,world-bank --format=csv --mode=append
                  print
                  print --source=open-meteo
                  help
                """);
    }
}
