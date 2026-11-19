package com.example.aggregator.http;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaHttpClientWrapperTest {
    @Test
    void testGetInvalidUrl() {
        JavaHttpClientWrapper client = new JavaHttpClientWrapper();
        HttpClientException exception = assertThrows(HttpClientException.class, () -> {
            client.get("https://invalid.domain.that.does.not.exist.local");
        });
        assertTrue(exception.getMessage().contains("I/O error"));
    }

    @Test
    void testInterruptedRequest() {
        JavaHttpClientWrapper client = new JavaHttpClientWrapper();
        Thread.currentThread().interrupt();

        assertThrows(HttpClientException.class, () -> {
            client.get("http://localhost:8080");
        });
        Thread.interrupted();
    }
}