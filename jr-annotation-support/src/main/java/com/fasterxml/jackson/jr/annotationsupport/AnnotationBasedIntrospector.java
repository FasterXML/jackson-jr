package com.fasterxml.jackson.jr.annotationsupport;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;
import com.fasterxml.jackson.jr.ob.impl.POJODefinition;

/**
 *
 * @since 2.11
 */
public class AnnotationBasedIntrospector
{
    protected final Class<?> _type;

    protected final Map<String, APropBuilder> _props = new HashMap<String, APropBuilder>();

    protected AnnotationBasedIntrospector(Class<?> type) {
        _type = type;
    }

    public static POJODefinition pojoDefinitionForDeserialization(JSONReader r, Class<?> pojoType) {
        return new AnnotationBasedIntrospector(pojoType).forReading();
    }

    public static POJODefinition pojoDefinitionForSerialization(JSONWriter w, Class<?> pojoType) {
        return new AnnotationBasedIntrospector(pojoType).forWriting();
    }

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    protected POJODefinition forReading() {

        // and then find necessary constructors
        Constructor<?> defaultCtor = null;
        Constructor<?> stringCtor = null;
        Constructor<?> longCtor = null;

        for (Constructor<?> ctor : _type.getDeclaredConstructors()) {
            Class<?>[] argTypes = ctor.getParameterTypes();
            if (argTypes.length == 0) {
                defaultCtor = ctor;
            } else if (argTypes.length == 1) {
                Class<?> argType = argTypes[0];
                if (argType == String.class) {
                    stringCtor = ctor;
                } else if (argType == Long.class || argType == Long.TYPE) {
                    longCtor = ctor;
                } else {
                    continue;
                }
            } else {
                continue;
            }
        }

        _findFields();
        _findMethods();

        return new POJODefinition(_type, _pruneReadProperties(),
                defaultCtor, stringCtor, longCtor);
    }
    
    protected POJODefinition forWriting()
    {
        _findFields();
        _findMethods();

        return new POJODefinition(_type, _pruneWriteProperties(),
                null, null,null);
    }

    /*
    /**********************************************************************
    /* Internal methods, main introspection
    /**********************************************************************
     */

    protected POJODefinition.Prop[] _pruneReadProperties() {
/*
        Map<String,PropBuilder> propsByName = new TreeMap<String,PropBuilder>();
        _introspect(_type, propsByName);

        final int len = propsByName.size();
        POJODefinition.Prop[] props;
        if (len == 0) {
            props = NO_PROPS;
        } else {
            props = new Prop[len];
            int i = 0;
            for (PropBuilder builder : propsByName.values()) {
                props[i++] = builder.build();
            }
        }
*/
        // !!! TODO
        return null;
    }

    protected POJODefinition.Prop[] _pruneWriteProperties() {
        // !!! TODO
        return null;
    }
    
    protected void _findFields() {
        for (Field f : _type.getDeclaredFields()) {
            // Does not include static fields, but there are couple of things we do
            // not include regardless:
            if (f.isEnumConstant() || f.isSynthetic()) {
                continue;
            }
            // otherwise, first things first; explicit ignoral?
            final String implName = f.getName();
            APropAccessor<Field> acc;

            if (Boolean.TRUE.equals(_hasIgnoreMarker(f))) {
                acc = APropAccessor.createIgnorable(implName, f);
            } else {
                final String explName = _findExplicitName(f);
                // Otherwise, do have explicit inclusion marker?
                if (explName != null) {
                    // ... with actual name?
                    if (explName.isEmpty()) { // `@JsonProperty("")`
                        acc = APropAccessor.createVisible(implName, f);
                    } else {
                        acc = APropAccessor.createExplicit(explName, f);
                    }
                } else {
                    // Otherwise may be visible
                    acc = APropAccessor.createImplicit(explName, f,
                            _isFieldVisible(f));
                }
            }
            _propBuilder(implName).field = acc;

        }
    }

    protected void _findMethods() {
        
    }

    /*
    /**********************************************************************
    /* Internal methods, visibility
    /**********************************************************************
     */

    protected boolean _isFieldVisible(Field f) {
        return Modifier.isPublic(f.getModifiers());
    }

    protected boolean _isGetterVisible(Field f) {
        return Modifier.isPublic(f.getModifiers());
    }
 
    protected boolean _isSetterVisible(Field f) {
        return Modifier.isPublic(f.getModifiers());
    }
    
    /*
    /**********************************************************************
    /* Internal methods, annotation introspection
    /**********************************************************************
     */

    // wrapper type just in case in future we want to detect existence of disables
    // ignoral marker for some reason
    protected Boolean _hasIgnoreMarker(AnnotatedElement m) {
        JsonIgnore ann = _find(m, JsonIgnore.class);
        return (ann != null) && ann.value();
    }

    protected final String _findExplicitName(AnnotatedElement m) {
        JsonProperty ann = _find(m, JsonProperty.class);
        return (ann == null) ? null : ann.value();
    }

    // Overridable accessor method
    protected <ANN extends Annotation> ANN _find(AnnotatedElement elem, Class<ANN> annotationType) {
        return elem.getAnnotation(annotationType);
    }
    
    /*
    /**********************************************************************
    /* Internal methods, other
    /**********************************************************************
     */
    
    protected APropBuilder _propBuilder(String name) {
        APropBuilder b = _props.get(name);
        if (b == null) {
            b = new APropBuilder(name);
            _props.put(name, b);
        }
        return b;
    }

    protected static String _decap(String name) {
        char c = name.charAt(0);
        char lowerC = Character.toLowerCase(c);

        if (c != lowerC) {
            // First: do NOT lower case if more than one leading upper case letters:
            if ((name.length() == 1)
                    || !Character.isUpperCase(name.charAt(1))) {
                char chars[] = name.toCharArray();
                chars[0] = lowerC;
                return new String(chars);
            }
        }
        return name;
    }
    
    /*
    private static void _introspect(Class<?> currType, Map<String,PropBuilder> props)
    {
        if (currType == null || currType == Object.class) {
            return;
        }
        // First, check base type
        _introspect(currType.getSuperclass(), props);


        // then get methods from within this class
        for (Method m : currType.getDeclaredMethods()) {
            final int flags = m.getModifiers();
            // 13-Jun-2015, tatu: Skip synthetic, bridge methods altogether, for now
            //    at least (add more complex handling only if absolutely necessary)
            if (Modifier.isStatic(flags)
                    || m.isSynthetic() || m.isBridge()) {
                continue;
            }
            Class<?> argTypes[] = m.getParameterTypes();
            if (argTypes.length == 0) { // getter?
                // getters must be public to be used
                if (!Modifier.isPublic(flags)) {
                    continue;
                }
                
                Class<?> resultType = m.getReturnType();
                if (resultType == Void.class) {
                    continue;
                }
                String name = m.getName();
                if (name.startsWith("get")) {
                    if (name.length() > 3) {
                        name = decap(name.substring(3));
                        _propFrom(props, name).withGetter(m);
                    }
                } else if (name.startsWith("is")) {
                    if (name.length() > 2) {
                        // May or may not be used, but collect for now all the same:
                        name = decap(name.substring(2));
                        _propFrom(props, name).withIsGetter(m);
                    }
                }
            } else if (argTypes.length == 1) { // setter?
                // Non-public setters are fine if we can force access, don't yet check
                // let's also not bother about return type; setters that return value are fine
                String name = m.getName();
                if (!name.startsWith("set") || name.length() == 3) {
                    continue;
                }
                name = decap(name.substring(3));
                _propFrom(props, name).withSetter(m);
            }
        }
    }
*/


    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */
    
    protected static class APropBuilder {
        public final String name;

        protected APropAccessor<Field> field;
        protected APropAccessor<Method> getter;
        protected APropAccessor<Method> setter;

        public APropBuilder(String n) {
            name = n;
        }
    }

    protected static class APropAccessor<ACC extends AccessibleObject> {
        public final String name;
        public final ACC accessor;

        public final boolean isExplicit, isNameExplicit;
        public final boolean isToIgnore, isVisible;

        protected APropAccessor(String n, ACC acc,
                boolean expl, boolean nameExpl,
                boolean ignore, boolean visible)
        {
            name = n;
            accessor = acc;
            isExplicit = expl;
            isNameExplicit = nameExpl;
            isToIgnore = ignore;
            isVisible = visible;
        }

        // We saw `@JsonIgnore` and that's all we need
        public static <T extends AccessibleObject> APropAccessor<T> createIgnorable(String name, T accessor) {
            return new APropAccessor<T>(name, accessor,
                    false, false, true, false);
        }

        // We didn't saw any relevant annotation
        public static <T extends AccessibleObject> APropAccessor<T> createImplicit(String name, T accessor,
                boolean visible) {
            return new APropAccessor<T>(name, accessor,
                    false, false, false, visible);
        }

        // We only saw "empty" `@JsonProperty` (or similar marker)
        public static <T extends AccessibleObject> APropAccessor<T> createVisible(String name, T accessor) {
            return new APropAccessor<T>(name, accessor,
                    true, false, false, true);
        }

        // We saw `@JsonProperty` with non-empty name
        public static <T extends AccessibleObject> APropAccessor<T> createExplicit(String name, T accessor) {
            return new APropAccessor<T>(name, accessor,
                    true, true, false, true);
        }
    }
}
