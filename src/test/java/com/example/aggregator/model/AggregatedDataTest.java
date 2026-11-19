package com.example.aggregator.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AggregatedDataTest {
    @Test
    void testDefaultConstructor() {
        AggregatedData data = new AggregatedData();
        assertNotNull(data.getFields());
        assertTrue(data.getFields().isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        AggregatedData data = new AggregatedData();
        data.setSource("src");
        data.setCategory("cat");
        data.setKey("k");
        Instant now = Instant.now();
        data.setTimestamp(now);
        Map<String, Object> fields = new HashMap<>();
        fields.put("a", 1);
        data.setFields(fields);

        assertEquals("src", data.getSource());
        assertEquals("cat", data.getCategory());
        assertEquals("k", data.getKey());
        assertEquals(now, data.getTimestamp());
        assertEquals(1, data.getFields().get("a"));
    }

    @Test
    void testSetFieldsNull() {
        AggregatedData data = new AggregatedData();
        data.setFields(null);
        assertNotNull(data.getFields());
    }

    @Test
    void testEqualsAndHashCode() {
        Instant now = Instant.now();
        AggregatedData d1 = new AggregatedData("s", "c", "k", now, Map.of());
        AggregatedData d2 = new AggregatedData("s", "c", "k", now, Map.of());
        AggregatedData d3 = new AggregatedData("other", "c", "k", now, Map.of());

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
        assertNotEquals(null, d1);
        assertNotEquals(new Object(), d1);
    }

    @Test
    void testToString() {
        AggregatedData data = new AggregatedData("src", "cat", "k", Instant.now(), Map.of());
        String str = data.toString();
        assertTrue(str.contains("source='src'"));
        assertTrue(str.contains("category='cat'"));
    }
}