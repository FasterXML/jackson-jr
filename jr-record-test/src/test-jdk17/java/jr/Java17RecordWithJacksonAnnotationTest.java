package jr;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.jr.annotationsupport.JacksonAnnotationExtension;
import com.fasterxml.jackson.jr.ob.JSON;
import jr.TestClasses.NonAlphabeticWithAliases;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.fasterxml.jackson.jr.ob.JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test is in test module since the JDK version to be tested is higher than other, and hence supports Records.
 */
public class Java17RecordWithJacksonAnnotationTest extends AbstractJava17RecordTest
{
    @BeforeEach
    void setupJson() {
        JsonFactory jf = JsonFactory.builder()
                                    .build();
        JSON.Builder builder = JSON.builder(jf).enable(JSON.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        builder.register(JacksonAnnotationExtension.std);
        builder.enable(WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER);
        jsonHandler = builder.build();
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
        assertEquals("{\"z\":2,\"c\":3,\"b\":\"4\",\"a\":5}",
                jsonHandler.with(JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER).asString(input));
    }
}

