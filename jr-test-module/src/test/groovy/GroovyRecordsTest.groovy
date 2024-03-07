import com.fasterxml.jackson.jr.ob.JSON
import org.junit.Assert
import org.junit.Test

/**
 * A minor note on running/debugging this test on local, if you are using intellij, please
 * change `<packaging>pom</packaging>` to `<packaging>bundle</packaging>`. this is causing
 * some issue with the IDE.
*/
class GroovyRecordsTest {

    @Test
    void testRecord() throws Exception {
        def json = JSON.builder().enable(JSON.Feature.USE_FIELD_NAME_GETTERS).build().asString(new Cow("foo", Map<String, String>.of("foo", "bar")))
        def expected = """{"message":"foo","object":{"foo":"bar"}}"""
        Assert.assertEquals(expected, json)
    }

    @Test
    void testRecordEquivalentObjects() throws Exception {
        def expected = """{"message":"foo","object":{"foo":"bar"}}"""

        def json = JSON.builder().enable(JSON.Feature.USE_FIELD_NAME_GETTERS).build().asString(new SimpleGroovyObject("foo", Map<String, String>.of("foo", "bar")))
        Assert.assertEquals(expected, json)

        def json2 = JSON.builder().enable(JSON.Feature.USE_FIELD_NAME_GETTERS).build().asString(new GroovyObjectWithNamedGetters("foo", Map<String, String>.of("foo", "bar")))
        Assert.assertEquals(expected, json2)
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