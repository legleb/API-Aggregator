package com.example.aggregator.rest.dto;

public class PollingStatusResponse {
    private final boolean running;
    private final Integer n;
    private final Integer t;

    public PollingStatusResponse(boolean running, Integer n, Integer t) {
        this.running = running;
        this.n = n;
        this.t = t;
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
}