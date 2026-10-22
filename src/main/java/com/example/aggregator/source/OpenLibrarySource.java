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

public class OpenLibrarySource extends AbstractDataSource {
    private static final String BASE_URL = "https://openlibrary.org/search.json";

    public OpenLibrarySource(HttpClientWrapper http, ObjectMapper mapper) {
        super(http, mapper);
    }

    @Override
    public String getName() {
        return "open-library";
    }

    @Override
    public String getCategory() {
        return "book";
    }

    @Override
    protected String buildUrl(Map<String, String> params) {
        String query = params.getOrDefault("q", "java");
        String limit = params.getOrDefault("limit", "1");
        String fields = "title,author_name,first_publish_year,subject,cover_i";

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("q", query);
        queryParams.put("limit", limit);
        queryParams.put("fields", fields);

        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> e : queryParams.entrySet()) {
            sj.add(encode(e.getKey()) + "=" + encode(e.getValue()));
        }
        return BASE_URL + "?" + sj;
    }

    @Override
    protected List<AggregatedData> mapToAggregated(JsonNode root, Map<String, String> params) {
        List<AggregatedData> result = new ArrayList<>();
        String query = params.getOrDefault("q", "java");
        JsonNode docs = root.path("docs");
        if (!docs.isArray()) {
            return result;
        }

        int numFound = root.path("numFound").asInt();
        Instant now = Instant.now();
        for (JsonNode book : docs) {
            Map<String, Object> fields = new LinkedHashMap<>();
            String title = book.path("title").asText("unknown");
            String key = query + " - " + title;

            fields.put("title", title);
            fields.put("first_publish_year", book.path("first_publish_year").asInt());
            fields.put("cover_i", book.path("cover_i").asInt());
            fields.put("numFound", numFound);
            fields.put("author_name", readArray(book.path("author_name")));
            fields.put("subject", readArray(book.path("subject")));

            result.add(new AggregatedData(getName(), getCategory(), key, now, fields));
        }
        return result;
    }

    private List<String> readArray(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(n -> list.add(n.asText()));
        }
        return list;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
