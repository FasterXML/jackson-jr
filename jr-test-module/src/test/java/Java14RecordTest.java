import com.fasterxml.jackson.jr.ob.JSON;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

/**
 * This test is in test module since the JDK version to be tested is higher than other, and hence supports Records.
 */
public class Java14RecordTest {

    @Test
    public void testJava14RecordSupport() throws IOException {
        var expectedString = "{\"message\":\"MOO\",\"object\":{\"Foo\":\"Bar\"}}";
        var json = JSON.std.asString(new Cow("MOO", Map.of("Foo", "Bar")));
        Assert.assertEquals(expectedString, json);
    }

    record Cow(String message, Map<String, String> object) {
    }
}
