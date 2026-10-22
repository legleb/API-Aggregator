package com.example.aggregator;

import com.example.aggregator.cli.CommandLineApp;
import com.example.aggregator.config.AppConfig;

public class Main {
    public static void main(String[] args) {
        CommandLineApp app = AppConfig.buildCommandLineApp();
        app.run(args);
    }
}