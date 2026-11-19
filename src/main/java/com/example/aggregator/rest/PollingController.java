package com.example.aggregator.rest;

import com.example.aggregator.rest.dto.MessageResponse;
import com.example.aggregator.rest.dto.PollingStatusResponse;
import com.example.aggregator.rest.dto.StartPollingRequest;
import com.example.aggregator.scheduling.PollingConfig;
import com.example.aggregator.scheduling.PollingScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/polling")
public class PollingController {
    private final PollingScheduler scheduler;
    private final PollingConfig defaultConfig;

    public PollingController(PollingScheduler scheduler, PollingConfig defaultConfig) {
        this.scheduler = scheduler;
        this.defaultConfig = defaultConfig;
    }

    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestBody(required = false) StartPollingRequest request) {
        if (scheduler.isRunning()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse("Polling is already running"));
        }

        try {
            PollingConfig configToRun = createConfigToRun(request);
            if (configToRun.getN() <= 0) {
                throw new IllegalArgumentException("n must be positive");
            }
            if (configToRun.getT() <= 0) {
                throw new IllegalArgumentException("t must be positive");
            }
            scheduler.start(configToRun);
            return ResponseEntity.ok(new MessageResponse("Polling started"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @NonNull
    private PollingConfig createConfigToRun(StartPollingRequest request) {
        PollingConfig configToRun = new PollingConfig();
        if (request == null) {
            configToRun.setN(defaultConfig.getN());
            configToRun.setT(defaultConfig.getT());
            configToRun.setFormat(defaultConfig.getFormat());
            configToRun.setFile(defaultConfig.getFile());
            configToRun.setMode(defaultConfig.getMode());
            configToRun.setSources(defaultConfig.getSources());
        } else {
            configToRun.setN(request.getN() != null ? request.getN() : defaultConfig.getN());
            configToRun.setT(request.getT() != null ? request.getT() : defaultConfig.getT());
            configToRun.setFormat(request.getFormat() != null ? request.getFormat() : defaultConfig.getFormat());
            configToRun.setFile(request.getFile() != null ? request.getFile() : defaultConfig.getFile());
            configToRun.setMode(request.getMode() != null ? request.getMode() : defaultConfig.getMode());
            configToRun.setSources(request.getSources() != null ? request.getSources() : defaultConfig.getSources());
        }
        return configToRun;
    }

    @PostMapping("/stop")
    public ResponseEntity<MessageResponse> stop() {
        if (!scheduler.isRunning()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Polling is not running"));
        }
        scheduler.stop();
        return ResponseEntity.ok(new MessageResponse("Polling stopped"));
    }

    @GetMapping("/status")
    public ResponseEntity<PollingStatusResponse> status() {
        return ResponseEntity.ok(new PollingStatusResponse(
                scheduler.isRunning(),
                defaultConfig.getN(),
                defaultConfig.getT(),
                scheduler.getActiveSources()
        ));
    }
}