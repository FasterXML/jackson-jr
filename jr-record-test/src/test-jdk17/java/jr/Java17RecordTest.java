package jr;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.jr.ob.JSON;

import com.fasterxml.jackson.jr.ob.JSON.Feature;
import junit.framework.TestCase;

/**
 * This test is in test module since the JDK version to be tested is higher than other, and hence supports Records.
 */
public class Java17RecordTest extends TestCase
{

    private final JSON jsonParser = JSON.builder().enable(Feature.USE_FIELD_MATCHING_GETTERS).build();

    public record Cow(String message, Map<String, String> object) {
    }

    // [jackson-jr#94]: Record serialization
    public void testJava14RecordSerialization() throws Exception {
        var expectedString = """
                {"message":"MOO","object":{"Foo":"Bar"}}""";
        Cow expectedObject = new Cow("MOO", Map.of("Foo", "Bar"));

        var json = jsonParser.asString(expectedObject);
        assertEquals(expectedString, json);

        Cow object = jsonParser.beanFrom(Cow.class, json);
        assertEquals(expectedObject, object);
    }

    public void testDifferentOrder() throws IOException {
        var json = """
                {"object":{"Foo":"Bar"}, "message":"MOO"}""";

        Cow expectedObject = new Cow("MOO", Map.of("Foo", "Bar"));
        Cow object = jsonParser.beanFrom(Cow.class, json);
        assertEquals(expectedObject, object);
    }

    public void testNullAndRecord() throws IOException {
        var json = """
                {"object": null, "message":"MOO"}""";

        Cow expectedObject = new Cow("MOO", null);
        Cow object = jsonParser.beanFrom(Cow.class, json);
        assertEquals(expectedObject, object);

        assertEquals(new Cow(null, null), jsonParser.beanFrom(Cow.class,"{}"));
        assertNull(jsonParser.beanFrom(Cow.class, "null"));
    }

    public void testPartialParsing() throws IOException {
        var json = """
                { "message":"MOO"}""";

        Cow expectedObject = new Cow("MOO", null);
        Cow object = jsonParser.beanFrom(Cow.class, json);
        assertEquals(expectedObject, object);
    }

    public void testWhenInsideObject() throws IOException {
        var cowJson = """
                {"object": null, "message":"MOO"}""";
        var json = """
                { "cow": %s, "farmerName": "Bob" }""".formatted(cowJson);

        Wrapper wrapper = new Wrapper();
        wrapper.setCow(new Cow("MOO", null));
        wrapper.setFarmerName("Bob");

        Wrapper object = jsonParser.beanFrom(Wrapper.class, json);
        assertEquals(wrapper, object);

        var jsonNullCow = """
                { "cow": null, "farmerName": "Bob" }""";

        wrapper = new Wrapper();
        wrapper.setCow(null);
        wrapper.setFarmerName("Bob");

        object = jsonParser.beanFrom(Wrapper.class, jsonNullCow);
        assertEquals(wrapper, object);

        var jsonNoCow = """
                { "farmerName": "Bob" }""";

        wrapper = new Wrapper();
        wrapper.setCow(null);
        wrapper.setFarmerName("Bob");

        object = jsonParser.beanFrom(Wrapper.class, jsonNoCow);
        assertEquals(wrapper, object);
    }
}
