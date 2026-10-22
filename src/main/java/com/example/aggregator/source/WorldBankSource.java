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

public class WorldBankSource extends AbstractDataSource {
    private static final String BASE_URL = "https://api.worldbank.org/v2/country/";

    public WorldBankSource(HttpClientWrapper http, ObjectMapper mapper) {
        super(http, mapper);
    }

    @Override
    public String getName() {
        return "world-bank";
    }

    @Override
    public String getCategory() {
        return "country";
    }

    @Override
    protected String buildUrl(Map<String, String> params) {
        String code = params.getOrDefault("code", "RUS");
        return BASE_URL + encode(code) + "?format=json";
    }

    @Override
    protected List<AggregatedData> mapToAggregated(JsonNode root, Map<String, String> params) {
        List<AggregatedData> result = new ArrayList<>();
        if (!root.isArray() || root.size() < 2) {
            return result;
        }

        JsonNode countries = root.get(1);
        if (!countries.isArray()) {
            return result;
        }

        for (JsonNode country : countries) {
            String code = country.path("id").asText("unknown");
            String name = country.path("name").asText("unknown");
            Map<String, Object> fields = new LinkedHashMap<>();

            fields.put("iso2Code", country.path("iso2Code").asText());
            fields.put("name", name);
            fields.put("capitalCity", country.path("capitalCity").asText());
            fields.put("longitude", country.path("longitude").asText());
            fields.put("latitude", country.path("latitude").asText());

            fields.put("region", nested(country.path("region")));
            fields.put("adminregion", nested(country.path("adminregion")));
            fields.put("incomeLevel", nested(country.path("incomeLevel")));
            fields.put("lendingType", nested(country.path("lendingType")));

            result.add(new AggregatedData(getName(), getCategory(), code, Instant.now(), fields));
        }
        return result;
    }

    private Map<String, Object> nested(JsonNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (node.isObject()) {
            map.put("id", node.path("id").asText());
            map.put("iso2code", node.path("iso2code").asText());
            map.put("value", node.path("value").asText());
        }
        return map;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
