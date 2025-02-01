package com.fasterxml.jackson.jr.annotationsupport;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.jr.ob.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicReorderTest extends ASTestBase
{
    // default is alphabetic so change
    @JsonPropertyOrder({ "last", "first" })
    static class OrderedNameBean extends NameBean {
        public OrderedNameBean() { }
        public OrderedNameBean(String f, String l) {
            super(f, l);
        }
    }

    @JsonPropertyOrder({ "last", "_middle", "bogus" })
    static class FullNameBean {
        protected String first;
        protected String _middle;
        protected String last;

        public FullNameBean() { }
        public FullNameBean(String f, String m, String l) {
            first = f;
            _middle = m;
            last = l;
        }

        public String getFirst() { return first; }
        public String getMiddle() { return _middle; }
        public String getLast() { return last; }

        public void setFirst(String n) { first = n; }
        public void setMiddle(String n) { _middle = n; }
        public void setLast(String n) { last = n; }
    }

    private final JSON JSON_WITH_ANNO = jsonWithAnnotationSupport();

    @Test
    public void testSimpleReorder() throws Exception
    {
        final OrderedNameBean input = new OrderedNameBean("Bob", "Burger");
        final String EXP_DEFAULT = a2q("{'first':'Bob','last':'Burger'}");

        // default, no reorder:
        assertEquals(EXP_DEFAULT, JSON.std.asString(input));

        // then with simple, complete reorder
        assertEquals(a2q("{'last':'Burger','first':'Bob'}"), JSON_WITH_ANNO.asString(input));

        // and ensure no leakage to default one:
        assertEquals(EXP_DEFAULT, JSON.std.asString(input));
    }

    @Test
    public void testPartialReorder() throws Exception
    {
        final FullNameBean input = new FullNameBean("Bob", "DeLorean", "Burger");
        final String EXP_DEFAULT = a2q("{'first':'Bob','last':'Burger','middle':'DeLorean'}");

        // default, no reorder:
        assertEquals(EXP_DEFAULT, JSON.std.asString(input));

        // then with simple, complete reorder
        assertEquals(a2q("{'last':'Burger','middle':'DeLorean','first':'Bob'}"), JSON_WITH_ANNO.asString(input));

        // and ensure no leakage to default one:
        assertEquals(EXP_DEFAULT, JSON.std.asString(input));
    }
}
