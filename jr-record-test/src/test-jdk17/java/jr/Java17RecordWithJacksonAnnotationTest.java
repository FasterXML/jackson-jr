package jr;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.jr.annotationsupport.JacksonAnnotationExtension;
import com.fasterxml.jackson.jr.ob.JSON;
import org.junit.jupiter.api.BeforeEach;

import static com.fasterxml.jackson.jr.ob.JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER;

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
}

