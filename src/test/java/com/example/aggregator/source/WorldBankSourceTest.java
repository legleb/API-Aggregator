package com.example.aggregator.source;

import com.example.aggregator.http.HttpClientWrapper;
import com.example.aggregator.model.AggregatedData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorldBankSourceTest {
    @Test
    void testFetchAndMapSuccess() {
        HttpClientWrapper http = mock(HttpClientWrapper.class);
        String fakeJson = """
                [
                  {"page": 1},
                  [
                    {"id": "RUS", "name": "Russia", "region": {"value": "Europe"}}
                  ]
                ]
                """;
        when(http.get(anyString())).thenReturn(fakeJson);

        WorldBankSource source = new WorldBankSource(http, new ObjectMapper());
        List<AggregatedData> result = source.fetch(Map.of("code", "RUS"));

        assertEquals(1, result.size());
        assertEquals("Russia", result.get(0).getFields().get("name"));
    }

    @Test
    void testFetchUnexpectedStructure() {
        HttpClientWrapper http = mock(HttpClientWrapper.class);
        when(http.get(anyString())).thenReturn("{\"error\": \"bad request\"}");

        WorldBankSource source = new WorldBankSource(http, new ObjectMapper());
        List<AggregatedData> result = source.fetch(Map.of());

        assertTrue(result.isEmpty());
    }
}