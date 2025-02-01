package tools.jackson.jr.annotationsupport;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonAlias;

import tools.jackson.jr.ob.JSON;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BasicAliasTest extends ASTestBase
{
    static class AliasedName {
        @JsonAlias({ "firstName", "fn" })
        public String first;

        public String middle;

        public String last;

        protected AliasedName() { }
        public AliasedName(String f, String m, String l) {
            first = f;
            middle = m;
            last = l;
        }

        @JsonAlias("middleName")
        public String getMiddle() {
            return middle;
        }

        @JsonAlias({ "lastName", "ln" })
        public void setLast(String str) { last = str; }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    // for stricter validation, fail on unknown properties
    private final JSON JSON_WITH_ANNO = jsonWithAnnotationSupport()
            .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY);

    @Test
    public void testSimpleAliases() throws Exception
    {
        final String input = a2q("{ 'fn':'Billy', 'middleName':'Bob', 'lastName':'Burger' }");
        AliasedName result;

        // First: without setting, nothing matches
        result = JSON.std.beanFrom(AliasedName.class, input);
        assertNull(result.first);
        assertNull(result.middle);
        assertNull(result.last);

        // but with aliases it's all good...
        result = JSON_WITH_ANNO.beanFrom(AliasedName.class, input);
        assertEquals("Billy", result.first);
        assertEquals("Bob", result.middle);
        assertEquals("Burger", result.last);
    }
}
