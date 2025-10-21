package tools.jackson.jr.annotationsupport;

import tools.jackson.jr.annotationsupport.TestClasses.*;
import tools.jackson.jr.ob.JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static tools.jackson.jr.ob.JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test is in test module since the JDK version to be tested is higher than other, and hence supports Records.
 */
public class Java17RecordWithJacksonAnnotationTest extends AbstractJava17RecordTest
{
    @BeforeEach
    void setupJson() {
        JSON.Builder builder = JSON.builder().enable(JSON.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        builder.register(JacksonAnnotationExtension.std);
        builder.enable(WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER);
        jsonHandler = builder.build();
    }

    @Test
    public void testAliasesWork() throws Exception {
        SnakeCaseRecord r = new SnakeCaseRecord("Tom");
        String json = jsonHandler.asString(r);
        SnakeCaseRecord r2 = jsonHandler.beanFrom(SnakeCaseRecord.class, json);
        assertEquals(r.firstName(), r2.firstName());
    }

    @Test
    public void testIgnoreWork() throws Exception {
        SnakeCaseRecordWithIgnore r = new SnakeCaseRecordWithIgnore(10, 13);
        String json = jsonHandler.asString(r);
        SnakeCaseRecordWithIgnore r2 = jsonHandler.beanFrom(SnakeCaseRecordWithIgnore.class, json);
        assertEquals(r.value(), r2.value());
        assertEquals(0, r2.x());
    }

    // [jackson-jr#171]: Whether to serialize Records in declaration or alphabetical order
    @Test
    public void testRecordFieldWriteOrderWithJsonProperty() throws Exception
    {
        NonAlphabeticWithAliases input = new NonAlphabeticWithAliases(1, 2, 3, "4", 5);

        // Alphabetical order:
        assertEquals("{\"a\":5,\"b\":\"4\",\"c\":3,\"z\":2}",
                jsonHandler.without(JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER).asString(input));

        // Declaration order:
        String expected = "{\"z\":2,\"c\":3,\"b\":\"4\",\"a\":5}";
        assertEquals(expected, jsonHandler.with(JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER).asString(input));
        NonAlphabeticWithAliases serializedAgain = jsonHandler.with(JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER)
                                                              .beanFrom(NonAlphabeticWithAliases.class, expected);
        assertEquals(input.clearX(), serializedAgain);
        serializedAgain = jsonHandler.without(JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER)
                                                              .beanFrom(NonAlphabeticWithAliases.class, expected);
        assertEquals(input.clearX(), serializedAgain);
    }
}

