package com.example.aggregator.config;

import com.example.aggregator.cli.CommandLineApp;
import com.example.aggregator.cli.CommandParser;
import com.example.aggregator.http.HttpClientWrapper;
import com.example.aggregator.http.JavaHttpClientWrapper;
import com.example.aggregator.source.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class AppConfig {

    private AppConfig() {}

    public static CommandLineApp buildCommandLineApp() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        HttpClientWrapper http = new JavaHttpClientWrapper();

        DataSourceRegistry registry = new DataSourceRegistry();
        registry.register(new OpenMeteoSource(http, mapper));
        registry.register(new OpenLibrarySource(http, mapper));
        registry.register(new WorldBankSource(http, mapper));

        CommandParser parser = new CommandParser();

        return new CommandLineApp(registry, mapper, parser);
    }
}
