package jr;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.jr.ob.JSON;

import com.fasterxml.jackson.jr.ob.JSON.Feature;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * This test is in test module since the JDK version to be tested is higher than other, and hence supports Records.
 */
public class Java17RecordTest extends TestCase
{
    public record Cow(String message, Map<String, String> object) {
    }

    // [jackson-jr#94]: Record serialization
    public void testJava14RecordSerialization() throws Exception {
        JSON jsonParser = JSON.builder().enable(Feature.USE_FIELD_MATCHING_GETTERS).build();
        var expectedString = "{\"message\":\"MOO\",\"object\":{\"Foo\":\"Bar\"}}";
        Cow expectedObject = new Cow("MOO", Map.of("Foo", "Bar"));

        var json = jsonParser.asString(expectedObject);
        Assert.assertEquals(expectedString, json);

        Cow object = jsonParser.beanFrom(Cow.class, json);
        Assert.assertEquals(expectedObject, object);
    }

    public void testDifferentOrder() throws IOException {
        JSON jsonParser = JSON.builder().enable(Feature.USE_FIELD_MATCHING_GETTERS).build();
        var json = "{\"object\":{\"Foo\":\"Bar\"}, \"message\":\"MOO\"}";

        Cow expectedObject = new Cow("MOO", Map.of("Foo", "Bar"));
        Cow object = jsonParser.beanFrom(Cow.class, json);
        Assert.assertEquals(expectedObject, object);
    }
}
