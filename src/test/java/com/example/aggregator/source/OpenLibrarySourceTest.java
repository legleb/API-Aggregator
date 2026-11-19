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

class OpenLibrarySourceTest {
    @Test
    void testFetchAndMapSuccess() {
        HttpClientWrapper http = mock(HttpClientWrapper.class);
        String fakeJson = """
                {
                  "numFound": 1,
                  "docs": [
                    {"title": "Java Book", "first_publish_year": 2000, "author_name": ["Author"]}
                  ]
                }
                """;
        when(http.get(anyString())).thenReturn(fakeJson);

        OpenLibrarySource source = new OpenLibrarySource(http, new ObjectMapper());
        List<AggregatedData> result = source.fetch(Map.of("q", "test"));

        assertEquals(1, result.size());
        assertEquals("Java Book", result.get(0).getFields().get("title"));
    }

    @Test
    void testFetchNoDocs() {
        HttpClientWrapper http = mock(HttpClientWrapper.class);
        when(http.get(anyString())).thenReturn("{\"numFound\": 0}");

        OpenLibrarySource source = new OpenLibrarySource(http, new ObjectMapper());
        List<AggregatedData> result = source.fetch(Map.of());

        assertTrue(result.isEmpty());
    }
}