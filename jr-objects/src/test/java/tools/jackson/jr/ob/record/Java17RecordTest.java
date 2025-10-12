package tools.jackson.jr.ob.record;

import org.junit.jupiter.api.BeforeEach;

import tools.jackson.jr.ob.JSON;

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

