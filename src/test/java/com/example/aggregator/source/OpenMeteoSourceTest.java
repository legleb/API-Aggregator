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

class OpenMeteoSourceTest {
    @Test
    void testFetchAndMapSuccess() {
        HttpClientWrapper http = mock(HttpClientWrapper.class);
        String fakeJson = """
                {
                  "current_weather": {"temperature": 15.5, "windspeed": 10.0, "winddirection": 180, "weathercode": 1},
                  "hourly": {"time": ["t1", "t2"], "temperature_2m": [10.0, 11.0], "relativehumidity_2m": [80, 85]}
                }
                """;
        when(http.get(anyString())).thenReturn(fakeJson);

        OpenMeteoSource source = new OpenMeteoSource(http, new ObjectMapper());
        List<AggregatedData> result = source.fetch(Map.of("latitude", "1", "longitude", "2"));

        assertEquals(1, result.size());
        assertEquals(15.5, result.get(0).getFields().get("temperature"));
        assertEquals("1,2", result.get(0).getKey());
    }

    @Test
    void testFetchEmptyJson() {
        HttpClientWrapper http = mock(HttpClientWrapper.class);
        when(http.get(anyString())).thenReturn("{}");

        OpenMeteoSource source = new OpenMeteoSource(http, new ObjectMapper());
        List<AggregatedData> result = source.fetch(Map.of());

        assertEquals(1, result.size());
    }

    @Test
    void testFetchBadJson() {
        HttpClientWrapper http = mock(HttpClientWrapper.class);
        when(http.get(anyString())).thenReturn("not a json");

        OpenMeteoSource source = new OpenMeteoSource(http, new ObjectMapper());

        assertThrows(DataSourceException.class, () -> source.fetch(Map.of()));
    }
}