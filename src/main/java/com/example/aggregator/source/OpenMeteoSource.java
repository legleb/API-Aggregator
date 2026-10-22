package com.example.aggregator.source;

import com.example.aggregator.http.HttpClientWrapper;
import com.example.aggregator.model.AggregatedData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class OpenMeteoSource extends AbstractDataSource {
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    public OpenMeteoSource(HttpClientWrapper http, ObjectMapper mapper) {
        super(http, mapper);
    }

    @Override
    public String getName() {
        return "open-meteo";
    }

    @Override
    public String getCategory() {
        return "weather";
    }

    @Override
    protected String buildUrl(Map<String, String> params) {
        String latitude = params.getOrDefault("latitude", "55.75");
        String longitude = params.getOrDefault("longitude", "37.61");
        Map<String, String> query = new LinkedHashMap<>();

        query.put("latitude", latitude);
        query.put("longitude", longitude);
        query.put("current_weather", "true");
        query.put("hourly", "temperature_2m,relativehumidity_2m");
        query.put("timezone", "auto");

        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> e : query.entrySet()) {
            sj.add(encode(e.getKey()) + "=" + encode(e.getValue()));
        }
        return BASE_URL + "?" + sj;
    }

    @Override
    protected List<AggregatedData> mapToAggregated(JsonNode root, Map<String, String> params) {
        List<AggregatedData> result = new ArrayList<>();
        String key = params.getOrDefault("latitude", "55.75")
                + "," + params.getOrDefault("longitude", "37.61");

        Instant now = Instant.now();
        Map<String, Object> fields = new LinkedHashMap<>();
        JsonNode current = root.path("current_weather");
        if (!current.isMissingNode()) {
            fields.put("temperature", current.path("temperature").asDouble());
            fields.put("windspeed", current.path("windspeed").asDouble());
            fields.put("winddirection", current.path("winddirection").asDouble());
            fields.put("weathercode", current.path("weathercode").asInt());
        }

        fields.put("latitude", root.path("latitude").asDouble());
        fields.put("longitude", root.path("longitude").asDouble());
        fields.put("timezone", root.path("timezone").asText());

        JsonNode hourly = root.path("hourly");
        if (!hourly.isMissingNode()) {
            JsonNode times = hourly.path("time");
            JsonNode temps = hourly.path("temperature_2m");
            JsonNode humidity = hourly.path("relativehumidity_2m");

            int limit = Math.min(3, times.size());
            List<Map<String, Object>> hourlyList = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                Map<String, Object> hour = new LinkedHashMap<>();
                hour.put("time", times.get(i).asText());
                hour.put("temperature", temps.get(i).asDouble());
                hour.put("humidity", humidity.get(i).asDouble());
                hourlyList.add(hour);
            }
            fields.put("hourly", hourlyList);
        }

        result.add(new AggregatedData(getName(), getCategory(), key, now, fields));
        return result;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
