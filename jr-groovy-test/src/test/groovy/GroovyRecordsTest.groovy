import com.fasterxml.jackson.jr.ob.JSON

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A minor note on running/debugging this test on local, if you are using intellij, please
 * change `<packaging>pom</packaging>` to `<packaging>bundle</packaging>`. this is causing
 * some issue with the IDE.
*/
class GroovyRecordsTest
{
    @Test
    void testRecord() throws Exception {
        /* We need to use this since build (8, ubuntu-20.04), will fail Map.of() was added in Java 9*/
        def map = new HashMap<String, String>()
        map.put("foo", "bar")

        def json = JSON.builder().enable(JSON.Feature.USE_FIELD_MATCHING_GETTERS).build().asString(new Cow("foo", map))
        def expected = """{"message":"foo","object":{"foo":"bar"}}"""
        assertEquals(expected, json)
    }

    @Test
    void testRecordEquivalentObjects() throws Exception {
        def expected = """{"message":"foo","object":{"foo":"bar"}}"""

        /* We need to use this since build (8, ubuntu-20.04), will fail Map.of() was added in Java 9*/
        def map = new HashMap<String, String>()
        map.put("foo", "bar")

        def json = JSON.builder().enable(JSON.Feature.USE_FIELD_MATCHING_GETTERS).build().asString(new SimpleGroovyObject("foo", map))
        assertEquals(expected, json)

        def json2 = JSON.builder().enable(JSON.Feature.USE_FIELD_MATCHING_GETTERS).build().asString(new GroovyObjectWithNamedGetters("foo", map))
        assertEquals(expected, json2)
    }
}

class SimpleGroovyObject {
    public final String message
    public final Map<String, String> object

    SimpleGroovyObject(String message, Map<String, String> object) {
        this.message = message
        this.object = object
    }
}

class GroovyObjectWithNamedGetters {
    private final String message
    private final Map<String, String> object

    GroovyObjectWithNamedGetters(String message, Map<String, String> object) {
        this.message = message
        this.object = object
    }

    String message() {
        return message
    }

    Map<String, String> object() {
        return object
    }
}

record Cow(String message, Map<String, String> object) {}