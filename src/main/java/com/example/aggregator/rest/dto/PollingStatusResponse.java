package com.example.aggregator.rest.dto;

import java.util.List;

public class PollingStatusResponse {
    private final boolean running;
    private final Integer n;
    private final Integer t;
    private final List<String> activeSources;

    public PollingStatusResponse(boolean running, Integer n, Integer t, List<String> activeSources) {
        this.running = running;
        this.n = n;
        this.t = t;
        this.activeSources = activeSources;
    }

    public boolean isRunning() {
        return running;
    }

    public Integer getN() {
        return n;
    }

    public Integer getT() {
        return t;
    }

    public List<String> getActiveSources() {
        return activeSources;
    }
}