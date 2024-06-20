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

    // [jackson-jr#94]: Record serialization
    public void testJava14RecordSerialization() throws Exception {
        assertEquals("{\"message\":\"MOO\",\"object\":{\"Foo\":\"Bar\"}}",
                JSON.std.asString(new Cow("MOO", Map.of("Foo", "Bar"))));
    }
}
