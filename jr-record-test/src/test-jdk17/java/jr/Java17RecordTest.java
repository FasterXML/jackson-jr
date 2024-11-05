package jr;

import java.util.Map;

import junit.framework.TestCase;

import tools.jackson.jr.ob.JSON;

/**
 * This test is in test module since the JDK version to be tested is higher than other, and hence supports Records.
 */
public class Java17RecordTest extends TestCase
{
    private final JSON jsonHandler = JSON.std;

    public record Cow(String message, Map<String, String> object) {
    }

    public record WrapperRecord(Cow cow, String hello) {
    }

    public record RecordWithWrapper(Cow cow, Wrapper nested, int someInt) {
    }

    // [jackson-jr#171]: Whether to serialize Records in declaration or alphabetical order
    public record RecordNonAlphabetic171(int c, int b, int a) {
    }
    
    record SingleIntRecord(int value) { }
    record SingleLongRecord(long value) { }
    record SingleStringRecord(String value) { }

    // Degenerate case but supported:
    record NoFieldsRecord() { }
    
    // [jackson-jr#94]: Record serialization
    public void testJava14RecordSerialization() throws Exception {
        var expectedString = """
                {"message":"MOO","object":{"Foo":"Bar"}}""";
        Cow expectedObject = new Cow("MOO", Map.of("Foo", "Bar"));

        var json = jsonHandler.asString(expectedObject);
        assertEquals(expectedString, json);

        Cow object = jsonHandler.beanFrom(Cow.class, json);
        assertEquals(expectedObject, object);
    }

    public void testDifferentOrder() throws Exception {
        var json = """
                {"object":{"Foo":"Bar"}, "message":"MOO"}""";

        Cow expectedObject = new Cow("MOO", Map.of("Foo", "Bar"));
        Cow object = jsonHandler.beanFrom(Cow.class, json);
        assertEquals(expectedObject, object);
    }

    public void testNullAndRecord() throws Exception {
        var json = """
                {"object": null, "message":"MOO"}""";

        Cow expectedObject = new Cow("MOO", null);
        Cow object = jsonHandler.beanFrom(Cow.class, json);
        assertEquals(expectedObject, object);

        assertEquals(new Cow(null, null), jsonHandler.beanFrom(Cow.class,"{}"));
        assertNull(jsonHandler.beanFrom(Cow.class, "null"));
    }

    public void testPartialParsing() throws Exception {
        var json = """
                { "message":"MOO"}""";

        Cow expectedObject = new Cow("MOO", null);
        Cow object = jsonHandler.beanFrom(Cow.class, json);
        assertEquals(expectedObject, object);
    }

    public void testWhenInsideObject() throws Exception {
        var cowJson = """
                {"object": null, "message":"MOO"}""";
        var json = """
                { "cow": %s, "farmerName": "Bob" }""".formatted(cowJson);

        Wrapper wrapper = new Wrapper();
        wrapper.setCow(new Cow("MOO", null));
        wrapper.setFarmerName("Bob");

        Wrapper object = jsonHandler.beanFrom(Wrapper.class, json);
        assertEquals(wrapper, object);

        var jsonNullCow = """
                { "cow": null, "farmerName": "Bob" }""";

        wrapper = new Wrapper();
        wrapper.setCow(null);
        wrapper.setFarmerName("Bob");

        object = jsonHandler.beanFrom(Wrapper.class, jsonNullCow);
        assertEquals(wrapper, object);

        var jsonNoCow = """
                { "farmerName": "Bob" }""";

        wrapper = new Wrapper();
        wrapper.setCow(null);
        wrapper.setFarmerName("Bob");

        object = jsonHandler.beanFrom(Wrapper.class, jsonNoCow);
        assertEquals(wrapper, object);
    }

    public void testNested() throws Exception {
        var json = """
                {
                    "hello": "world",
                    "cow": { "message":"MOO"}
                }
               """;

        var expected = new WrapperRecord(new Cow("MOO", null), "world");
        var object = jsonHandler.beanFrom(WrapperRecord.class, json);
        assertEquals(expected, object);
    }

    public void testNestedObjects() throws Exception {
        var json = """
                {
                    "nested": {
                        "farmerName": "Bob",
                        "cow": { "message":"MOOO"}
                    },
                    "someInt": 1337,
                    "cow": { "message":"MOO"}
                }
               """;

        Wrapper nested = new Wrapper();
        nested.setCow(new Cow("MOOO", null));
        nested.setFarmerName("Bob");
        var expected = new RecordWithWrapper(new Cow("MOO", null), nested, 1337);
        var object = jsonHandler.beanFrom(RecordWithWrapper.class, json);
        assertEquals(expected, object);
    }

    public void testNoFieldRecords() throws Exception {
        String json = jsonHandler.asString(new NoFieldsRecord());
        assertEquals("{}", json);
        assertEquals(new NoFieldsRecord(),
                jsonHandler.beanFrom(NoFieldsRecord.class, json));
    }

    public void testSingleFieldRecords() throws Exception {
        SingleIntRecord inputInt = new SingleIntRecord(42);
        String json = jsonHandler.asString(inputInt);
        assertEquals("{\"value\":42}", json);
        assertEquals(inputInt, jsonHandler.beanFrom(SingleIntRecord.class, json));

        SingleLongRecord inputLong = new SingleLongRecord(-1L);
        json = jsonHandler.asString(inputLong);
        assertEquals("{\"value\":-1}", json);
        assertEquals(inputLong, jsonHandler.beanFrom(SingleLongRecord.class, json));

        SingleStringRecord inputStr = new SingleStringRecord("abc");
        json = jsonHandler.asString(inputStr);
        assertEquals("{\"value\":\"abc\"}", json);
        assertEquals(inputStr, jsonHandler.beanFrom(SingleStringRecord.class, json));
    }

    // [jackson-jr#171]: Whether to serialize Records in declaration or alphabetical order
    public void testRecordFieldWriteOrder() throws Exception
    {
        RecordNonAlphabetic171 input = new RecordNonAlphabetic171(1, 2, 3);

        // Alphabetical order:
        assertEquals("{\"a\":3,\"b\":2,\"c\":1}",
                jsonHandler.without(JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER).asString(input));

        // Declaration order:
        assertEquals("{\"c\":1,\"b\":2,\"a\":3}",
                jsonHandler.with(JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER).asString(input));
    }
}

