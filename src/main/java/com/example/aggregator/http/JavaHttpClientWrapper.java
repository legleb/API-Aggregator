package com.example.aggregator.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class JavaHttpClientWrapper implements HttpClientWrapper {
    private final HttpClient client;

    public JavaHttpClientWrapper() {
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    @Override
    public String get(String url) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(30))
                .GET().header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/120.0 Safari/537.36")
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new HttpClientException("HTTP " + response.statusCode() + " for " + url);
            }
            return response.body();
        } catch (IOException e) {
            throw new HttpClientException("I/O error for " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HttpClientException("Request interrupted for " + url, e);
        }
    }
}
