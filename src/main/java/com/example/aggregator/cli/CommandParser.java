package com.example.aggregator.cli;

import com.example.aggregator.model.WriteMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandParser {
    public Command parse(String[] args) {
        if (args == null || args.length == 0) {
            return new Command(Command.Type.HELP, List.of(), null, null, null, Map.of());
        }

        String first = args[0].toLowerCase();

        return switch (first) {
            case "poll" -> parsePoll(args);
            case "print" -> parsePrint(args);
            case "help", "--help", "-h" ->
                    new Command(Command.Type.HELP, List.of(), null, null, null, Map.of());
            default -> throw new CliException("Unknown command: " + first);
        };
    }

    private Command parsePoll(String[] args) {
        List<String> sources = new ArrayList<>();
        String format = "json";
        WriteMode mode = WriteMode.CREATE;
        Map<String, Map<String, String>> paramsBySource = new HashMap<>();

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--sources=")) {
                String value = arg.substring("--sources=".length());
                for (String s : value.split(",")) {
                    if (!s.isBlank()) {
                        sources.add(s.trim());
                    }
                }
            } else if (arg.startsWith("--format=")) {
                format = arg.substring("--format=".length()).trim().toLowerCase();
                if (!format.equals("json") && !format.equals("csv")) {
                    throw new CliException("Unknown format: " + format);
                }
            } else if (arg.startsWith("--mode=")) {
                String m = arg.substring("--mode=".length()).trim().toLowerCase();
                mode = switch (m) {
                    case "create" -> WriteMode.CREATE;
                    case "append" -> WriteMode.APPEND;
                    default -> throw new CliException("Unknown mode: " + m);
                };
            } else if (arg.startsWith("--") && arg.contains(".")) {
                String cleanArg = arg.substring(2);
                int eqIndex = cleanArg.indexOf('=');
                if (eqIndex == -1) {
                    throw new CliException("Invalid source parameter format: " + arg);
                }

                String keyPart = cleanArg.substring(0, eqIndex);
                String value = cleanArg.substring(eqIndex + 1);
                int dotIndex = keyPart.indexOf('.');
                if (dotIndex == -1) {
                    throw new CliException("Invalid source parameter format: " + arg);
                }

                String sourceName = keyPart.substring(0, dotIndex);
                String paramName = keyPart.substring(dotIndex + 1);
                paramsBySource.computeIfAbsent(sourceName, k -> new HashMap<>()).put(paramName, value);
            } else {
                throw new CliException("Unknown argument: " + arg);
            }
        }
        if (sources.isEmpty()) {
            throw new CliException("--sources is required for poll");
        }
        return new Command(Command.Type.POLL, sources, format, mode, null, paramsBySource);
    }

    private Command parsePrint(String[] args) {
        String sourceFilter = null;
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--source=")) {
                sourceFilter = arg.substring("--source=".length()).trim();
            } else {
                throw new CliException("Unknown argument: " + arg);
            }
        }
        Command.Type type = (sourceFilter == null) ? Command.Type.PRINT : Command.Type.PRINT_BY_SOURCE;
        return new Command(type, List.of(), null, null, sourceFilter, Map.of());
    }
}
