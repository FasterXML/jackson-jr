package jr;

import java.util.Map;

import com.fasterxml.jackson.jr.ob.JSON;

import junit.framework.TestCase;

/**
 * This test is in test module since the JDK version to be tested is higher than other, and hence supports Records.
 */
public class Java17RecordTest extends TestCase
{
    public record Cow(String message, Map<String, String> object,Integer anInt) {
    }

    // [jackson-jr#94]
    public void testJava14RecordSerialization() throws Exception {
        // 13-Jun-2024, tatu: why is this explicitly needed?
        JSON json = JSON.std;
        var expectedDoc = "{\"anInt\":5,\"message\":\"MOO\",\"object\":{\"Foo\":\"Bar\"}}";
        Cow input = new Cow("MOO", Map.of("Foo", "Bar"),5);

        assertEquals(expectedDoc, json.asString(input));
    }

    // [jackson-jr#148]
    public void testJava14RecordDeserialization() throws Exception {
        // 13-Jun-2024, tatu: why is this explicitly needed?
        JSON json = JSON.std;
        var inputDoc = "{\"message\":\"MOO\",\"object\":{\"Foo\":\"Bar\"},\"anInt\":5}";

        Cow expected = new Cow("MOO", Map.of("Foo", "Bar"), 5);

        Cow actual = json.beanFrom(Cow.class, inputDoc);
        assertEquals(expected, actual);
    }

    public void testJava14RecordDeserialization2() throws Exception {
        // 13-Jun-2024, tatu: why is this explicitly needed?
        JSON json = JSON.std;
        var inputDoc = "{\"message\":\"MOO\",\"object\":null,\"anInt\":5}";

        Cow expected = new Cow("MOO", null, 5);

        Cow actual = json.beanFrom(Cow.class, inputDoc);
        assertEquals(expected, actual);
    }

    public void testJava14RecordDeserializationWithPrimitives() throws Exception {
        // 13-Jun-2024, tatu: why is this explicitly needed?
        JSON json = JSON.std;
        String inputDoc = "{\"message\":\"MOO\",\"object\":{\"Foo\":\"Bar\"}}";

        Cow expected = new Cow("MOO", Map.of("Foo", "Bar"), null);

        Cow actual = json.beanFrom(Cow.class, inputDoc);
        assertEquals(expected, actual);
    }
}
