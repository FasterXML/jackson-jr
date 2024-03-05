package com.fasterxml.jackson.jr.stree;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * Tests for reading content using {@link JSON} with proper
 * codec registration
 */
public class DupFieldNameInTree51Test extends JacksonJrTreeTestBase {

    // [jackson-jr#51]: test dup keys for trees too
    public void testFailOnDupMapKeys() throws Exception {
        JSON j = JSON.builder()
                .enable(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS)
                .build();

        final JSON treeJSON = jsonWithTreeCodec(j);

        assertTrue(j.isEnabled(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS));
        final String json = "{\"a\":1,\"b\":2,\"b\":3,\"c\":4}";
        try {
            /*TreeNode node =*/ treeJSON.treeFrom(json);
        } catch (JSONObjectException e) {
            verifyException(e, "Duplicate key");
        }
    }

    public void testFailOnDupMapKeys2() throws Exception {
        //missing flag - Enabled by default!
        JSON j = JSON.builder().build();

        final JSON treeJSON = jsonWithTreeCodec(j);

        assertTrue(j.isEnabled(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS));
        final String json = "{\"a\":1,\"b\":2,\"b\":3,\"c\":4}";
        try {
            /*TreeNode node =*/ treeJSON.treeFrom(json);
        } catch (JSONObjectException e) {
            verifyException(e, "Duplicate key");
        }
    }
}
