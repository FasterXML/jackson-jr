package tools.jackson.jr.annotationsupport;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndexedMapTest
{
    @Test
    void putRemoveTest() {
        IndexedMap<String, String> map = new IndexedMap<>();

        map.put("d", "d");
        map.put("c", "c");
        map.put("b", "b");
        map.put("a", "a");

        assertEquals("d", map.get("d"));
        assertEquals("c", map.get("c"));
        assertEquals("b", map.get("b"));
        assertEquals("a", map.get("a"));

        map.remove("b");
        assertEquals("d", map.get("d"));
        assertEquals("c", map.get("c"));
        assertNull(map.get("b"));
        assertEquals("a", map.get("a"));

        map.replaceAtIndexOf("c", "z", "z");
        assertEquals("z", map.get("z"));
        assertEquals(Arrays.asList("d", "z", "a"), map.values());
    }
}
