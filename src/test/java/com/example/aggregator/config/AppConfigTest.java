package com.example.aggregator.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AppConfigTest {
    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context.getBean("objectMapper"));
        assertNotNull(context.getBean("dataSourceRegistry"));
        assertNotNull(context.getBean("httpClientWrapper"));
    }
}