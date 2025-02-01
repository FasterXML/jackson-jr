package com.fasterxml.jackson.jr.ob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// For [jackson-jr#94]: support for Serializing JDK 17/Groovy records
// (minimal one; full test in separate test package)
//
// @since 2.17
public class ReadRecordLikeTest extends TestBase
{
    static class RecordLike94 {
        int count = 3;
        int STATUS = 500;
        int foobar;

        // should be discovered:
        public int count() { return count; }
        // likewise:
        public int STATUS() { return STATUS; }

        // should NOT be discovered (takes argument(s))
        public int foobar(int value) {
            foobar = value;
            return value;
        }

        // also not to be discovered
        public int mismatched() { return 42; }
    }

    @Test
    public void testRecordLikePOJO() throws Exception
    {
        // By default, do not auto-detect "record-style" accessors
        assertEquals("{}", JSON.std.asString(new RecordLike94()));

        assertEquals(a2q("{'STATUS':500,'count':3}"), JSON.std.with(JSON.Feature.USE_FIELD_MATCHING_GETTERS)
                .asString(new RecordLike94()));
    }
}
