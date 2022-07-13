package tools.jackson.jr.annotationsupport;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import tools.jackson.jr.annotationsupport.JacksonAnnotationExtension;
import tools.jackson.jr.ob.JSON;

public class BasicVisibilityTest extends ASTestBase
{
    static class FieldXYZ {
        public int x;
        protected int y;
        private int z;

        protected FieldXYZ() { }
        public FieldXYZ(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int x() { return x; }
        public int y() { return y; }
        public int z() { return z; }
    }

    static class FieldXYTransient {
        public int x;
        public transient int y;

        protected FieldXYTransient() { }
        public FieldXYTransient(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC)
    static class FieldXYZWithAnnotation {
        public int x;
        protected int y;
        private int z;

        protected FieldXYZWithAnnotation() { }
        public FieldXYZWithAnnotation(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int x() { return x; }
        public int y() { return y; }
        public int z() { return z; }
    }

    static class MethodXYEnabled {
        int _x;
        int _y;
        boolean _enabled;

        protected MethodXYEnabled() { }
        public MethodXYEnabled(int x, int y, boolean enabled) {
            _x = x;
            _y = y;
            _enabled = enabled;
        }

        public int getX() { return _x; }
        protected int getY() { return _y; }
        protected boolean isEnabled() { return _enabled; }

        public void setX(int x) { _x = x; }
        protected void setY(int y) { _y = y; }
        protected void setEnabled(boolean enabled) { _enabled = enabled; }
    }

    // Increase getter/is-getter levels to "protected", decrease setter
    // to require public
    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
            getterVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC,
            isGetterVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC
    )
    static class MethodXYEnabledWithAnnotations {
        int _x;
        int _y;
        boolean _enabled;

        protected MethodXYEnabledWithAnnotations() { }
        public MethodXYEnabledWithAnnotations(int x, int y, boolean enabled) {
            _x = x;
            _y = y;
            _enabled = enabled;
        }

        public int getX() { return _x; }
        protected int getY() { return _y; }
        protected boolean isEnabled() { return _enabled; }

        public void setX(int x) { _x = x; }
        protected void setY(int y) { _y = y; }
        protected void setEnabled(boolean enabled) { _enabled = enabled; }
    }
    
    private final JsonAutoDetect.Value VIS_FIELD_PROTECTED = JacksonAnnotationExtension.DEFAULT_VISIBILITY
            .withFieldVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);

    private final JsonAutoDetect.Value VIS_FIELD_ALL = JacksonAnnotationExtension.DEFAULT_VISIBILITY
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY);

    private final JsonAutoDetect.Value VIS_SETTER_PUBLIC = JacksonAnnotationExtension.DEFAULT_VISIBILITY
            .withSetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY);

    private final JsonAutoDetect.Value VIS_REGULAR_GETTER_PROTECTED = JacksonAnnotationExtension.DEFAULT_VISIBILITY
            .withGetterVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);

    private final JsonAutoDetect.Value VIS_IS_GETTER_PROTECTED = JacksonAnnotationExtension.DEFAULT_VISIBILITY
            .withIsGetterVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);

    /*
    /**********************************************************************
    /* Test methods, field visibility
    /**********************************************************************
     */

    public void testSimpleSerializeFieldVisibility() throws Exception
    {
        final FieldXYZ input = new FieldXYZ(1, 2, 3);
        // by default, only public field seen
        assertEquals(a2q("{'x':1}"), JSON.std.asString(input));
        // but various levels increase it
        assertEquals(a2q("{'x':1,'y':2}"), jsonWithVisibility(VIS_FIELD_PROTECTED)
                .asString(input));
        assertEquals(a2q("{'x':1,'y':2,'z':3}"), jsonWithVisibility(VIS_FIELD_ALL)
                .asString(input));

        // but must have fields enabled (handled in core jr-objects, not by annotations)
        assertEquals(a2q("{}"),
                jsonWithVisibility(VIS_FIELD_ALL)
                    .without(JSON.Feature.USE_FIELDS)
                .asString(input));

        // finally, consider class annotation version
        assertEquals(a2q("{'x':1,'y':2}"), jsonWithVisibility(null)
                .asString(new FieldXYZWithAnnotation(1, 2, 3)));
    }

    // Test to ensure we will not attempt deserializing transient fields
    public void testSimpleSerializeWrtTransient() throws Exception
    {
        final FieldXYTransient input = new FieldXYTransient(1, 2);

        // Alas, by default, jackson-jr does not skip transient
        assertEquals(a2q("{'x':1,'y':2}"), JSON.std.asString(input));

        // but with extension it does
        assertEquals(a2q("{'x':1}"), jsonWithVisibility(VIS_FIELD_PROTECTED)
                .asString(input));
        assertEquals(a2q("{'x':1}"), jsonWithVisibility(null)
                .asString(input));
    }

    public void testSimpleDeserializeFieldVisibility() throws Exception
    {
        final String input = a2q("{'x':1,'y':2,'z':3}");
        FieldXYZ result;

        // by default only 'x' visible:
        result = JSON.std.beanFrom(FieldXYZ.class, input);
        assertEquals(1, result.x());
        assertEquals(0, result.y());
        assertEquals(0, result.z());

        // but changes with minimum visibility
        result = jsonWithVisibility(VIS_FIELD_PROTECTED).beanFrom(FieldXYZ.class, input);
        assertEquals(1, result.x());
        assertEquals(2, result.y());
        assertEquals(0, result.z());

        result = jsonWithVisibility(VIS_FIELD_ALL).beanFrom(FieldXYZ.class, input);
        assertEquals(1, result.x());
        assertEquals(2, result.y());
        assertEquals(3, result.z());
    }

    // Test to ensure we will not attempt deserializing transient fields
    public void testSimpleDeserializeWrtTransient() throws Exception
    {
        final String input = a2q("{'x':1,'y':2}");
        FieldXYTransient result;

        // Alas, by default, jackson-jr does not skip transient
        result = JSON.std.beanFrom(FieldXYTransient.class, input);
        assertEquals(1, result.x);
//        assertEquals(0, result.y);

        // but visibility settings, should
        result = jsonWithVisibility(VIS_FIELD_ALL).beanFrom(FieldXYTransient.class, input);
        assertEquals(1, result.x);
        assertEquals(0, result.y);
    }

    /*
    /**********************************************************************
    /* Test methods, getter visibility (serialization)
    /**********************************************************************
     */

    public void testSimpleGetterVisibility() throws Exception
    {
        final MethodXYEnabled input = new MethodXYEnabled(1, 2, true);
        // by default, only public field seen
        assertEquals(a2q("{'x':1}"), JSON.std.asString(input));
        // but various levels increase it
        assertEquals(a2q("{'x':1,'y':2}"), jsonWithVisibility(VIS_REGULAR_GETTER_PROTECTED)
                .asString(input));
        assertEquals(a2q("{'enabled':true,'x':1}"), jsonWithVisibility(VIS_IS_GETTER_PROTECTED)
                .asString(input));
    }

    public void testSimpleGetterWithAnnotationVisibility() throws Exception
    {
        final MethodXYEnabledWithAnnotations input = new MethodXYEnabledWithAnnotations(1, 2, true);
        assertEquals(a2q("{'enabled':true,'x':1,'y':2}"), jsonWithVisibility(null)
                .asString(input));
    }

    /*
    /**********************************************************************
    /* Test methods, setter visibility (deserialization)
    /**********************************************************************
     */

    public void testSimpleSetterVisibility() throws Exception
    {
        final String input = a2q("{'enabled':true,'x':1,'y':2}");
        MethodXYEnabled result;

        // 25-Jan-2020, tatu: by default even non-public setters are actually found as of 2.10
        //    so need to keep behavior the same
        result = JSON.std.beanFrom(MethodXYEnabled.class, input);
        assertEquals(1, result._x);
        assertEquals(2, result._y);
        assertEquals(true, result._enabled);

        // but when defining visibility with detection settings, can change
        result = jsonWithVisibility(VIS_SETTER_PUBLIC).beanFrom(MethodXYEnabled.class, input);
        assertEquals(1, result._x);
        assertEquals(0, result._y);
        assertEquals(false, result._enabled);
    }

    public void testSimpleSetterWithAnnotationVisibility() throws Exception
    {
        final String input = a2q("{'enabled':true,'x':1,'y':2}");
        MethodXYEnabledWithAnnotations result;

        // 25-Jan-2020, tatu: by default even non-public setters are actually found as of 2.10
        //    so need to keep behavior the same
        result = jsonWithVisibility(null).beanFrom(MethodXYEnabledWithAnnotations.class, input);
        assertEquals(1, result._x);
        assertEquals(0, result._y);
        assertEquals(false, result._enabled);
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    private JSON jsonWithVisibility(JsonAutoDetect.Value vis) {
        return JSON.builder()
                .register(JacksonAnnotationExtension.builder()
                        .withVisibility(vis)
                        .build())
                .build();
    }
}
