package jr;

import java.util.Map;

import com.fasterxml.jackson.jr.ob.JSON;

import junit.framework.TestCase;

/**
 * This test is in test module since the JDK version to be tested is higher than other, and hence supports Records.
 */
public class Java17RecordTest extends TestCase
{
    public record Cow(String message, Map<String, String> object) {
    }

    // [jackson-jr#94]
    public void testJava14RecordSerialization() throws Exception {
        JSON json = JSON.std;
        var expectedDoc = "{\"message\":\"MOO\",\"object\":{\"Foo\":\"Bar\"}}";
        Cow input = new Cow("MOO", Map.of("Foo", "Bar"));

        assertEquals(expectedDoc, json.asString(input));
    }

    // [jackson-jr#148]: simple deserialization
    public void testRecordDeserializationSimple() throws Exception {
        String inputDoc = "{\"message\":\"MOO\",\"object\":{\"Foo\":\"Bar\"}}";
        assertEquals(new Cow("MOO", Map.of("Foo", "Bar")),
                JSON.std.beanFrom(Cow.class, inputDoc));
    }

    // [jackson-jr#157]: deserialization should work with different ordering
    public void testRecordDeserializationReordered() throws Exception {
        String inputDoc = "{\"object\":{\"Foo\":\"Bar\"}, \"message\":\"MOO\"}";
        assertEquals(new Cow("MOO", Map.of("Foo", "Bar")),
                JSON.std.beanFrom(Cow.class, inputDoc));
    }
    // [jackson-jr#157]: deserialization should work with missing, as well
    public void testRecordDeserializationPartial() throws Exception {
        String inputDoc = "{\"message\":\"MOO\"}";
        assertEquals(new Cow("MOO", null),
                JSON.std.beanFrom(Cow.class, inputDoc));
    }
}
