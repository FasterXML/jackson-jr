package tools.jackson.jr.annotationsupport;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonValue;

import tools.jackson.jr.ob.JSON;
import tools.jackson.jr.ob.JSONObjectException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class BasicRenameTest extends ASTestBase
{
    static class NameSimple {
        @JsonProperty("firstName")
        public String _first;

        @JsonProperty // just explicit marker, no rename
        public String _last;

        protected NameSimple() { }
        public NameSimple(String f, String l) {
            _first = f;
            _last = l;
        }
    }

    private interface EnumIdentifier {
        @JsonValue
        String getId();
    }

    /**
     * enum that uses {@link JsonProperty} to alias values
     */
    protected enum ABCRename { @JsonProperty("A1") A, @JsonProperty("B1") B, C; }

    /**
     * enum that uses a {@link JsonValue} method to alias its values and a {@link JsonCreator} to
     * deserialize
     */
    protected enum ABCJsonValueJsonCreator
    {
        A("A1"),
        B("B1"),
        C("C");

        private String label;

        ABCJsonValueJsonCreator(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return "ABCJsonValueJsonCreator{" +
                    "label='" + label + '\'' +
                    '}';
        }

        @JsonValue
        public String serialize() {
            return label;
        }

        @JsonCreator
        public static ABCJsonValueJsonCreator fromLabel(String label) {
            for (ABCJsonValueJsonCreator value : ABCJsonValueJsonCreator.values()) {
                if (value.label.equals(label)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unexpected label '" + label + "'");
        }
    }

    /**
     * Enum values in this class are anonymous inner classes
     */
    protected enum SubclassingEnum implements EnumIdentifier {
        ENUM_A {
            @Override
            public String getId() {
                return "A";
            }
        },
        ENUM_B {
            @Override
            public String getId() {
                return "B";
            }
        },
        ENUM_NO_JSON_VALUE {
            // don't use this to make the serialized version
            // hiding the superclass's @JsonValue
            @JsonValue(false)
            @Override
            public String getId() {
                return "NONE";
            }
        };


        @JsonCreator
        public static SubclassingEnum from(String id) {
            if ("A".equals(id)) {
                return ENUM_A;
            }
            if ("B".equals(id)) {
                return ENUM_B;
            }
            return null;
        }
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
    public void testBasicRenameOnSerialize() throws Exception
    {
        final NameSimple input = new NameSimple("Bob", "Burger");
        // default, no ignorals:
        assertEquals(a2q("{'_first':'Bob','_last':'Burger'}"), JSON.std.asString(input));

        // but if we rename "_last"
        assertEquals(a2q("{'_last':'Burger','firstName':'Bob'}"), JSON_WITH_ANNO.asString(input));

        // and ensure no leakage to default one:
        assertEquals(a2q("{'_first':'Bob','_last':'Burger'}"), JSON.std.asString(input));
    }

    @Test
    public void testBasicRenameOnDeserialize() throws Exception
    {
        final String json = a2q("{'firstName':'Bob','_last':'Burger'}");
        final JSON j = JSON.std
                .with(JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY);

        try {
            j.beanFrom(NameSimple.class, json);
            fail("Should not pass");
        } catch (JSONObjectException e) {
            verifyException(e, "Unrecognized JSON property \"firstName\"");
        }
        NameSimple result = JSON_WITH_ANNO.beanFrom(NameSimple.class, json);
        assertEquals("Bob", result._first);
        assertEquals("Burger", result._last);
    }

    @Test
    public void testEnumRenameOnSerialize() throws Exception
    {
        ABCRename inputA = ABCRename.A;
        // default
        assertEquals(a2q("\"A\""), JSON.std.asString(inputA));
        // with annotations
        assertEquals(a2q("\"A1\""), JSON_WITH_ANNO.asString(inputA));

        ABCRename inputB = ABCRename.B;
        // default
        assertEquals(a2q("\"B\""), JSON.std.asString(inputB));
        // with annotations
        assertEquals(a2q("\"B1\""), JSON_WITH_ANNO.asString(inputB));

        ABCRename inputC = ABCRename.C;
        // default
        assertEquals(a2q("\"C\""), JSON.std.asString(inputC));
        // with annotations
        assertEquals(a2q("\"C\""), JSON_WITH_ANNO.asString(inputC));
    }

    @Test
    public void testEnumRenameOnDeserialize() throws Exception
    {
        String jsonA = a2q("\"A1\"");
        ABCRename resultA = JSON_WITH_ANNO.beanFrom(ABCRename.class, jsonA);
        assertEquals(ABCRename.A, resultA);

        String jsonB = a2q("\"B1\"");
        ABCRename resultB = JSON_WITH_ANNO.beanFrom(ABCRename.class, jsonB);
        assertEquals(ABCRename.B, resultB);

        String jsonC = a2q("\"C\"");
        ABCRename resultC = JSON_WITH_ANNO.beanFrom(ABCRename.class, jsonC);
        assertEquals(ABCRename.C, resultC);
    }

    @Test
    public void testJsonValueCreatorEnumRenameOnSerialize() throws Exception
    {
        ABCJsonValueJsonCreator inputA = ABCJsonValueJsonCreator.A;
        // default
        assertEquals("\"ABCJsonValueJsonCreator{label='A1'}\"", JSON.std.asString(inputA));
        // with annotations
        assertEquals(a2q("\"A1\""), JSON_WITH_ANNO.asString(inputA));

        ABCJsonValueJsonCreator inputB = ABCJsonValueJsonCreator.B;
        // default
        assertEquals("\"ABCJsonValueJsonCreator{label='B1'}\"", JSON.std.asString(inputB));
        // with annotations
        assertEquals(a2q("\"B1\""), JSON_WITH_ANNO.asString(inputB));

        ABCJsonValueJsonCreator inputC = ABCJsonValueJsonCreator.C;
        // default
        assertEquals("\"ABCJsonValueJsonCreator{label='C'}\"", JSON.std.asString(inputC));
        // with annotations
        assertEquals(a2q("\"C\""), JSON_WITH_ANNO.asString(inputC));
    }

    @Test
    public void testJsonValueCreatorEnumRenameOnDeserialize() throws Exception
    {
        String jsonA = a2q("\"A1\"");
        ABCJsonValueJsonCreator resultA = JSON_WITH_ANNO.beanFrom(ABCJsonValueJsonCreator.class, jsonA);
        assertEquals(ABCJsonValueJsonCreator.A, resultA);

        String jsonB = a2q("\"B1\"");
        ABCJsonValueJsonCreator resultB = JSON_WITH_ANNO.beanFrom(ABCJsonValueJsonCreator.class, jsonB);
        assertEquals(ABCJsonValueJsonCreator.B, resultB);

        String jsonC = a2q("\"C\"");
        ABCJsonValueJsonCreator resultC = JSON_WITH_ANNO.beanFrom(ABCJsonValueJsonCreator.class, jsonC);
        assertEquals(ABCJsonValueJsonCreator.C, resultC);
    }

    @Test
    public void testJsonValueCreatorHierarchicalEnumRenameOnSerialize() throws Exception
    {
        SubclassingEnum inputA = SubclassingEnum.ENUM_A;
        // default
        assertEquals("\"ENUM_A\"", JSON.std.asString(inputA));
        // with annotations
        assertEquals(a2q("\"A\""), JSON_WITH_ANNO.asString(inputA));

        SubclassingEnum inputB = SubclassingEnum.ENUM_B;
        // default
        assertEquals("\"ENUM_B\"", JSON.std.asString(inputB));
        // with annotations
        assertEquals(a2q("\"B\""), JSON_WITH_ANNO.asString(inputB));
    }

    @Test
    public void testJsonValueCreatorHierarchicalEnumRenameOnDeserialize() throws Exception
    {
        String jsonA = a2q("\"A\"");
        SubclassingEnum resultA = JSON_WITH_ANNO.beanFrom(SubclassingEnum.class, jsonA);
        assertEquals(SubclassingEnum.ENUM_A, resultA);

        String jsonB = a2q("\"B\"");
        SubclassingEnum resultB = JSON_WITH_ANNO.beanFrom(SubclassingEnum.class, jsonB);
        assertEquals(SubclassingEnum.ENUM_B, resultB);
    }

    @Test
    public void testJsonValueHidingSubclass() throws Exception
    {
        SubclassingEnum input = SubclassingEnum.ENUM_NO_JSON_VALUE;
        // default
        assertEquals("\"ENUM_NO_JSON_VALUE\"", JSON.std.asString(input));
        // with annotations
        assertEquals(a2q("\"ENUM_NO_JSON_VALUE\""), JSON_WITH_ANNO.asString(input));
    }
}
