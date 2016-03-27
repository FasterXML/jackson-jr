package com.fasterxml.jackson.jr.ob;

import java.util.Date;

import com.fasterxml.jackson.jr.ob.JSON.Feature;

public class WriteDatesTest extends TestBase
{
    // For [jackson-jr#29]
    public void testSimpleDates() throws Exception
    {
        final Date input = new Date(0L);
        JSON j = JSON.std;
        
        assertFalse(j.isEnabled(Feature.WRITE_DATES_AS_TIMESTAMP));

        String json = j.asString(input);
        // What to test? For now, accept two variants we may get, depending
        // on timezone (which we can not, alas, control)
        if (!json.contains("Dec 31")
                && !json.contains("Jan 01")) {
            fail("Invalid output: "+json);
        }

        j = j.with(Feature.WRITE_DATES_AS_TIMESTAMP);
        assertTrue(j.isEnabled(Feature.WRITE_DATES_AS_TIMESTAMP));

        json = j.asString(input);
        assertEquals("0", json);
    }

}
