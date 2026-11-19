package com.example.aggregator.scheduling;

import com.example.aggregator.model.WriteMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "aggregator")
public class PollingConfig {
    private int n;
    private int t;
    private String format;
    private String file;
    private WriteMode mode;
    private Map<String, List<Map<String, String>>> sources;

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public WriteMode getMode() {
        return mode;
    }

    public void setMode(WriteMode mode) {
        this.mode = mode;
    }

    public Map<String, List<Map<String, String>>> getSources() {
        return sources;
    }

    public void setSources(Map<String, List<Map<String, String>>> sources) {
        this.sources = sources;
    }
}