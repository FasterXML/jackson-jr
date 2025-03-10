package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.*;
import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.impl.POJODefinition.Prop;
import com.fasterxml.jackson.jr.ob.impl.POJODefinition.PropBuilder;

/**
 * Helper class that jackson-jr uses by default to introspect POJO properties
 * (represented as {@link POJODefinition}) to build general POJO readers
 * (deserializers) and writers (serializers).
 *<p>
 * Note that most of the usage is via {@link ValueReaderLocator} and
 * {@link ValueWriterLocator}
 *
 * @since 2.11
 */
public class BeanPropertyIntrospector
{
    protected final static Prop[] NO_PROPS = new Prop[0];

    private final static BeanPropertyIntrospector INSTANCE = new BeanPropertyIntrospector();

    public BeanPropertyIntrospector() { }

    public static BeanPropertyIntrospector instance() { return INSTANCE; }

    public POJODefinition pojoDefinitionForDeserialization(JSONReader r, Class<?> pojoType) {
        return _introspectDefinition(pojoType, false, r.features(),
                RecordsHelpers.isRecordType(pojoType));
    }

    public POJODefinition pojoDefinitionForSerialization(JSONWriter w, Class<?> pojoType) {
        return _introspectDefinition(pojoType, true, w.features(),
                RecordsHelpers.isRecordType(pojoType));
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    private POJODefinition _introspectDefinition(Class<?> beanType,
            boolean forSerialization, int features, boolean isRecord)
    {
        // 21-Oct-2024, tatu: [jackson-jr#167] Need to retain property order
        //   for Deserialization, to keep Record properties ordered.
        //   For Serialization OTOH we need sorting (although would probably
        //   be better to sort after the fact, maybe in future)

        Map<String,PropBuilder> propsByName;
        // 04-Nov-2024, tatu [jackson-jr#171] May need to retain order
        //  for Record serialization too
        final boolean recordSerInDeclOrder = isRecord && forSerialization
                && JSON.Feature.WRITE_RECORD_FIELDS_IN_DECLARATION_ORDER.isEnabled(features);

        // Alphabetic ordering unnecessary for Deserialization (and some serialization too)
        if (forSerialization && !recordSerInDeclOrder) {
            propsByName = new TreeMap<>();  
        } else {
            propsByName = new LinkedHashMap<>();
        }

        final BeanConstructors constructors;
        if (forSerialization) {
            if (recordSerInDeclOrder) {
                Constructor<?> canonical = _getCanonicalRecordConstructor(beanType);
                for (Parameter ctorParam : canonical.getParameters()) {
                    _propFrom(propsByName, ctorParam.getName());
                }
            }
            constructors = null;
        } else {
            constructors = new BeanConstructors(beanType);
            if (isRecord) {
                Constructor<?> canonical = _getCanonicalRecordConstructor(beanType);
                constructors.addRecordConstructor(canonical);
                // And then let's "seed" properties to ensure correct ordering
                // of Properties wrt Canonical constructor parameters:
                for (Parameter ctorParam : canonical.getParameters()) {
                    _propFrom(propsByName, ctorParam.getName());
                }
            } else {
                for (Constructor<?> ctor : beanType.getDeclaredConstructors()) {
                    Class<?>[] argTypes = ctor.getParameterTypes();
                    if (argTypes.length == 0) {
                        constructors.addNoArgsConstructor(ctor);
                    } else if (argTypes.length == 1) {
                        Class<?> argType = argTypes[0];
                        if (argType == String.class) {
                            constructors.addStringConstructor(ctor);
                        } else if (argType == Integer.class || argType == Integer.TYPE) {
                            constructors.addIntConstructor(ctor);
                        } else if (argType == Long.class || argType == Long.TYPE) {
                            constructors.addLongConstructor(ctor);
                        }
                    }
                }
            }
        }
        _introspect(beanType, propsByName, features, isRecord);

        final int len = propsByName.size();
        Prop[] props;
        if (len == 0) {
            props = NO_PROPS;
        } else {
            props = new Prop[len];
            int i = 0;
            for (PropBuilder builder : propsByName.values()) {
                props[i++] = builder.build();
            }
        }
        return new POJODefinition(beanType, props, constructors);
    }

    private Constructor<?> _getCanonicalRecordConstructor(Class<?> beanType) {
        Constructor<?> canonical = RecordsHelpers.findCanonicalConstructor(beanType);
        if (canonical == null) { // should never happen
            throw new IllegalArgumentException(
"Unable to find canonical constructor of Record type `"+beanType.getClass().getName()+"`");
        }
        return canonical;
    }
        
    private static void _introspect(Class<?> currType, Map<String, PropBuilder> props,
            int features, boolean isRecord)
    {
        if (currType == null || currType == Object.class) {
            return;
        }
        // First, check base type.
        // 21-Oct-2024, tatu: ... but not for Records (no need; it's `java.lang.Record`)
        if (!isRecord) {
            _introspect(currType.getSuperclass(), props, features, isRecord);
        }

        final boolean noStatics = JSON.Feature.INCLUDE_STATIC_FIELDS.isDisabled(features);

        // 14-Jun-2024, tatu: Need to enable "matching getters" naming style for Java Records
        //   too, regardless of `Feature.USE_FIELD_MATCHING_GETTERS`
        final boolean isFieldNameGettersEnabled = isRecord
                || JSON.Feature.USE_FIELD_MATCHING_GETTERS.isEnabled(features);

        final Map<String, Field> fieldNameMap = isFieldNameGettersEnabled ? new LinkedHashMap<>() : null;

        // then public fields (since 2.8); may or may not be ultimately included
        // but at this point still possible
        for (Field f : currType.getDeclaredFields()) {
            // First things first: skip synthetics, Enum constants
            if (f.isEnumConstant() || f.isSynthetic()) {
                continue;
            }
            // Only include static members if (a) inclusion feature enabled and
            // (b) not final (cannot deserialize final fields)
            if (Modifier.isStatic(f.getModifiers()) && (noStatics || Modifier.isFinal(f.getModifiers()))) {
                continue;
            }
            // But for possible renaming, even non-public Fields have effect so:
            if (fieldNameMap != null) {
                fieldNameMap.put(f.getName(), f);
            }
            // Otherwise we will only include public Fields
            if (Modifier.isPublic(f.getModifiers())) {
                _propFrom(props, f.getName()).withField(f);
            }
        }

        // then get methods from within this class
        for (Method m : currType.getDeclaredMethods()) {
            final int flags = m.getModifiers();
            // 13-Jun-2015, tatu: Skip synthetic, bridge methods altogether, for now
            //    at least (add more complex handling only if absolutely necessary)
            if (Modifier.isStatic(flags) || m.isSynthetic() || m.isBridge() || isGroovyMetaClass(m.getReturnType())) {
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
                } else if (isFieldNameGettersEnabled) {
                    // 10-Mar-2024: [jackson-jr#94]:
                    //  This will allow getters with field name as their getters,
                    // like the ones generated by Groovy (or JDK 17 for Records).
                    // If method name matches with field name, & method return
                    // type matches the field type only then it can be considered a getter.
                    Field field = fieldNameMap.get(name);
                    if (field != null && Modifier.isPublic(m.getModifiers())
                            && m.getReturnType().equals(field.getType())) {
                        // NOTE: do NOT decap, field name should be used as-is
                        _propFrom(props, name).withGetter(m);
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

    private static PropBuilder _propFrom(Map<String,PropBuilder> props, String name) {
        return props.computeIfAbsent(name, Prop::builder);
    }

    private static String decap(String name) {
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

    /**
     * Helper method to detect Groovy's problematic metadata accessor type.
     *<p>
     * NOTE: Groovy MetaClass have cyclic reference, and hence the class containing
     * it should not be serialized without either removing that reference,
     * or skipping over such references.
     */
    protected static boolean isGroovyMetaClass(Class<?> clazz) {
        return "groovy.lang.MetaClass".equals(clazz.getName());
    }
}
