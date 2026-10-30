package com.fasterxml.jackson.jr.ob;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReadNestedContainers223Test extends TestBase
{
    static class NestedLists {
        public List<List<Integer>> values;
    }

    static class NestedArrays {
        public List<String[]> values;
    }

    @Test
    public void testInvalidNestedList() throws Exception {
        JSONObjectException e = assertThrows(JSONObjectException.class,
                () -> JSON.std.beanFrom(NestedLists.class, "{\"values\":[5,[1]]}"));
        verifyException(e, "Unexpected token VALUE_NUMBER_INT; should get START_ARRAY");
    }

    @Test
    public void testInvalidNestedArray() throws Exception {
        JSONObjectException e = assertThrows(JSONObjectException.class,
                () -> JSON.std.beanFrom(NestedArrays.class,
                        a2q("{'values':['x',['a']]}")));
        verifyException(e, "Unexpected token VALUE_STRING; should get START_ARRAY");
    }

    @Test
    public void testValidNestedContainersWithNulls() throws Exception {
        NestedLists lists = JSON.std.beanFrom(NestedLists.class,
                "{\"values\":[null,[],[1,2]]}");
        assertEquals(Arrays.asList(null, Collections.emptyList(), Arrays.asList(1, 2)),
                lists.values);

        NestedArrays arrays = JSON.std.beanFrom(NestedArrays.class,
                a2q("{'values':[null,[],['a','b']]}"));
        assertEquals(3, arrays.values.size());
        assertNull(arrays.values.get(0));
        assertArrayEquals(new String[0], arrays.values.get(1));
        assertArrayEquals(new String[] { "a", "b" }, arrays.values.get(2));
    }
}
