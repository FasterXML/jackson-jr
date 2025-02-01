package tools.jackson.jr.stree;

import org.junit.jupiter.api.Test;

import tools.jackson.jr.ob.JSON;
import tools.jackson.jr.ob.JSONObjectException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for reading content using {@link JSON} with proper
 * codec registration
 */
public class DupFieldNameInTree51Test extends JacksonJrTreeTestBase
{
    private final JSON NO_DUPS_JSON = JSON.builder()
            .enable(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS)
            .register(new JrSimpleTreeExtension())
            .build();

    private final JSON DUPS_OK_JSON = JSON.builder()
            .disable(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS)
            .register(new JrSimpleTreeExtension())
            .build();
    
    // [jackson-jr#51]: test dup keys for trees too
    @Test
    public void testFailOnDupMapKeys() throws Exception
    {
        assertTrue(NO_DUPS_JSON.isEnabled(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS));
        final String json = "{\"a\":1,\"b\":2,\"b\":3,\"c\":4}";
        try {
            /*TreeNode node =*/ NO_DUPS_JSON.treeFrom(json);
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "Duplicate key");
        }

        assertFalse(DUPS_OK_JSON.isEnabled(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS));
        // But should pass fine without setting
        assertNotNull(DUPS_OK_JSON.treeFrom(json));
    }
}
