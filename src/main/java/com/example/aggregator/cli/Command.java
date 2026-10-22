package com.example.aggregator.cli;

import com.example.aggregator.model.WriteMode;
import java.util.List;
import java.util.Map;

public class Command {

    public enum Type {
        POLL,
        PRINT,
        PRINT_BY_SOURCE,
        HELP
    }

    private final Type type;
    private final List<String> sources;
    private final String format;
    private final WriteMode mode;
    private final String sourceFilter;
    private final Map<String, Map<String, String>> paramsBySource;

    public Command(Type type, List<String> sources, String format, WriteMode mode,
                   String sourceFilter, Map<String, Map<String, String>> paramsBySource) {
        this.type = type;
        this.sources = sources;
        this.format = format;
        this.mode = mode;
        this.sourceFilter = sourceFilter;
        this.paramsBySource = paramsBySource;
    }

    public Type getType() {
        return type;
    }

    public List<String> getSources() {
        return sources;
    }

    public String getFormat() {
        return format;
    }

    public WriteMode getMode() {
        return mode;
    }

    public String getSourceFilter() {
        return sourceFilter;
    }

    public Map<String, Map<String, String>> getParamsBySource() {
        return paramsBySource;
    }
}
