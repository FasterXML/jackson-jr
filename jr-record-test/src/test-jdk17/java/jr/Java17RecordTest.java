package jr;

import java.util.Map;

import jr.TestClasses.Cow;
import jr.TestClasses.RecordNonAlphabetic171;
import jr.TestClasses.RecordWithWrapper;
import jr.TestClasses.WrapperRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.jr.ob.JSON;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * This test is in test module since the JDK version to be tested is higher than other, and hence supports Records.
 */
public class Java17RecordTest extends AbstractJava17RecordTest
{
    @BeforeEach
    void setUpJson() {
        jsonHandler = JSON.std;
    }
}

