package com.example.aggregator.config;

import com.example.aggregator.http.HttpClientWrapper;
import com.example.aggregator.http.JavaHttpClientWrapper;
import com.example.aggregator.source.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public HttpClientWrapper httpClientWrapper() {
        return new JavaHttpClientWrapper();
    }

    @Bean
    public DataSourceRegistry dataSourceRegistry(HttpClientWrapper http, ObjectMapper mapper) {
        DataSourceRegistry registry = new DataSourceRegistry();
        registry.register(new OpenMeteoSource(http, mapper));
        registry.register(new OpenLibrarySource(http, mapper));
        registry.register(new WorldBankSource(http, mapper));
        return registry;
    }
}