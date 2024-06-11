import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

/**
 * This test is in test module since the JDK version to be tested is higher than other, and hence supports Records.
 */
public class Java17RecordTest {

    @Test
    public void testJava14RecordSupport() throws IOException {
        JSON jsonParser = JSON.builder().enable(Feature.USE_FIELD_MATCHING_GETTERS).build();
        var expectedString = "{\"message\":\"MOO\",\"object\":{\"Foo\":\"Bar\"}}";
        Cow expectedObject = new Cow("MOO", Map.of("Foo", "Bar"));

        var json = jsonParser.asString(expectedObject);
        Assert.assertEquals(expectedString, json);

        Cow object = jsonParser.beanFrom(Cow.class, json);
        Assert.assertEquals(expectedObject, object);
    }

    record Cow(String message, Map<String, String> object) {
    }
}
