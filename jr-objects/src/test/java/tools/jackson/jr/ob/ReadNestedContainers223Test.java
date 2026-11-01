package tools.jackson.jr.ob;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.jr.ob.api.ReaderWriterProvider;
import tools.jackson.jr.ob.api.ValueReader;
import tools.jackson.jr.ob.impl.JSONReader;

import static org.junit.jupiter.api.Assertions.*;

public class ReadNestedContainers223Test extends TestBase
{
    // Failure mode of the original problem is a busy loop: needs to be preemptive
    // since that does not react to interrupts, so non-preemptive timeout never fires
    private final static Duration HANG_TIMEOUT = Duration.ofSeconds(10);

    static class NestedLists {
        public List<List<Integer>> values;
    }

    static class NestedArrays {
        public List<String[]> values;
    }

    static class ArraysOfArrays {
        public String[][] values;
    }

    static class NestedMaps {
        public List<Map<String, Integer>> values;
    }

    static class MapOfNestedLists {
        public Map<String, List<List<Integer>>> values;
    }

    static class Greedy { }

    // Skips one token too many, and (unlike standard readers) does not
    // fail if that leads to end-of-input
    static class GreedyReader extends ValueReader {
        GreedyReader() {
            super(Greedy.class);
        }

        @Override
        public Object read(JSONReader r, JsonParser p) throws JacksonException {
            p.nextToken();
            return new Greedy();
        }
    }

    static class GreedyList {
        public List<Greedy> values;
    }

    static class GreedyArray {
        public Greedy[] values;
    }

    private final JSON GREEDY_JSON = jsonWithProvider(new ReaderWriterProvider() {
        @Override
        public ValueReader findValueReader(JSONReader ctxt, Class<?> type) {
            return (type == Greedy.class) ? new GreedyReader() : null;
        }
    });

    /*
    /**********************************************************************
    /* Test methods: invalid element, followed by more content
    /**********************************************************************
     */

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

    /*
    /**********************************************************************
    /* Test methods: invalid element as the last one (used to never return)
    /**********************************************************************
     */

    @Test
    public void testInvalidLastElementInBean() throws Exception {
        _verifyFails(() -> JSON.std.beanFrom(NestedLists.class, "{\"values\":[5]}"),
                "Unexpected token VALUE_NUMBER_INT; should get START_ARRAY");
        _verifyFails(() -> JSON.std.beanFrom(NestedLists.class, "{\"values\":[[1],5]}"),
                "Unexpected token VALUE_NUMBER_INT; should get START_ARRAY");
        _verifyFails(() -> JSON.std.beanFrom(NestedLists.class, "{\"values\":[true]}"),
                "Unexpected token VALUE_TRUE; should get START_ARRAY");
        _verifyFails(() -> JSON.std.beanFrom(NestedLists.class, "{\"values\":[{}]}"),
                "Unexpected token START_OBJECT; should get START_ARRAY");
        _verifyFails(() -> JSON.std.beanFrom(NestedArrays.class, a2q("{'values':['x']}")),
                "Unexpected token VALUE_STRING; should get START_ARRAY");
        _verifyFails(() -> JSON.std.beanFrom(ArraysOfArrays.class, "{\"values\":[5]}"),
                "Unexpected token VALUE_NUMBER_INT; should get START_ARRAY");
        _verifyFails(() -> JSON.std.beanFrom(MapOfNestedLists.class, a2q("{'values':{'a':[5]}}")),
                "Unexpected token VALUE_NUMBER_INT; should get START_ARRAY");
    }

    @Test
    public void testInvalidLastElementAtRoot() throws Exception {
        _verifyFails(() -> JSON.std.arrayOfFrom(String[].class, "[5]"),
                "Unexpected token VALUE_NUMBER_INT; should get START_ARRAY");
        _verifyFails(() -> JSON.std.listOfFrom(String[].class, "[5]"),
                "Unexpected token VALUE_NUMBER_INT; should get START_ARRAY");
    }

    /*
    /**********************************************************************
    /* Test methods: end-of-input, regardless of element reader used
    /**********************************************************************
     */

    @Test
    public void testEndOfInputWithGreedyReaderAtRoot() throws Exception {
        _verifyFails(() -> GREEDY_JSON.listOfFrom(Greedy.class, "[1]"),
                "Unexpected end-of-input");
        _verifyFails(() -> GREEDY_JSON.arrayOfFrom(Greedy.class, "[1]"),
                "Unexpected end-of-input");
        // Above fail right after the first element; these after later ones
        _verifyFails(() -> GREEDY_JSON.listOfFrom(Greedy.class, "[1,2,3]"),
                "Unexpected end-of-input");
        _verifyFails(() -> GREEDY_JSON.arrayOfFrom(Greedy.class, "[1,2,3]"),
                "Unexpected end-of-input");
    }

    // Not hanging, but used to call element reader with no current token
    @Test
    public void testEndOfInputAfterFirstElement() throws Exception {
        _verifyFails(() -> JSON.std.listOfFrom(int[].class, "[5]"),
                "Unexpected end-of-input");
        _verifyFails(() -> JSON.std.arrayOfFrom(int[].class, "[5]"),
                "Unexpected end-of-input");
    }

    @Test
    public void testEndOfInputWithGreedyReaderInBean() throws Exception {
        _verifyFails(() -> GREEDY_JSON.beanFrom(GreedyList.class, "{\"values\":[1]}"),
                "Unexpected end-of-input");
        _verifyFails(() -> GREEDY_JSON.beanFrom(GreedyArray.class, "{\"values\":[1]}"),
                "Unexpected end-of-input");
    }

    /*
    /**********************************************************************
    /* Test methods: Map elements
    /**********************************************************************
     */

    @Test
    public void testInvalidNestedMap() throws Exception {
        _verifyFails(() -> JSON.std.beanFrom(NestedMaps.class, "{\"values\":[5]}"),
                "Unexpected token VALUE_NUMBER_INT; should get START_OBJECT");
        _verifyFails(() -> JSON.std.beanFrom(NestedMaps.class, a2q("{'values':[5,{'a':1}]}")),
                "Unexpected token VALUE_NUMBER_INT; should get START_OBJECT");
        _verifyFails(() -> JSON.std.beanFrom(NestedMaps.class, "{\"values\":[[]]}"),
                "Unexpected token START_ARRAY; should get START_OBJECT");
    }

    @Test
    public void testValidNestedMapsWithNulls() throws Exception {
        NestedMaps maps = JSON.std.beanFrom(NestedMaps.class,
                a2q("{'values':[null,{},{'a':1,'b':null}]}"));
        Map<String, Integer> expected = new LinkedHashMap<>();
        expected.put("a", 1);
        expected.put("b", null);
        assertEquals(Arrays.asList(null, Collections.emptyMap(), expected), maps.values);
    }

    /*
    /**********************************************************************
    /* Test methods: valid content
    /**********************************************************************
     */

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

    @Test
    public void testValidSingleNullElement() throws Exception {
        NestedLists lists = assertTimeoutPreemptively(HANG_TIMEOUT,
                () -> JSON.std.beanFrom(NestedLists.class, "{\"values\":[null]}"));
        assertEquals(Collections.singletonList(null), lists.values);
    }

    private void _verifyFails(Executable read, String expectedMessage) {
        JSONObjectException e = assertTimeoutPreemptively(HANG_TIMEOUT,
                () -> assertThrows(JSONObjectException.class, read));
        verifyException(e, expectedMessage);
    }
}
