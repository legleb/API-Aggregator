package com.example.aggregator.rest.dto;

import java.util.List;
import java.util.Map;

public class StartPollingRequest {
    private Integer n;
    private Integer t;
    private String format;
    private String file;
    private String mode;
    private Map<String, List<Map<String, String>>> sources;

    public Integer getN() {
        return n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    public Integer getT() {
        return t;
    }

    public void setT(Integer t) {
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Map<String, List<Map<String, String>>> getSources() {
        return sources;
    }

    public void setSources(Map<String, List<Map<String, String>>> sources) {
        this.sources = sources;
    }
}