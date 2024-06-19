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

    // [jackson-jr#148]
    public void testJava14RecordDeserialization() throws Exception {
        JSON json = JSON.std;
        String inputDoc = "{\"message\":\"MOO\",\"object\":{\"Foo\":\"Bar\"}}";

        Cow expected = new Cow("MOO", Map.of("Foo", "Bar"));

        Cow actual = json.beanFrom(Cow.class, inputDoc);
        assertEquals(expected, actual);
    }
}
