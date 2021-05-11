package com.fasterxml.jackson.jr.annotationsupport;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.jr.ob.api.ReaderWriterModifier;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;
import com.fasterxml.jackson.jr.ob.impl.POJODefinition;

public class AnnotationBasedValueRWModifier extends ReaderWriterModifier {
    // Matches SER_ENUM code in ValueLocatorBase
    protected static final int SER_ENUM = 23;

    /**
     * Visibility settings to use for auto-detecting accessors.
     */
    protected final JsonAutoDetect.Value _visibility;

    public AnnotationBasedValueRWModifier(JsonAutoDetect.Value visibility) {
        _visibility = visibility;
    }

    @Override
    public POJODefinition pojoDefinitionForDeserialization(JSONReader readContext, Class<?> pojoType) {
        return AnnotationBasedIntrospector.pojoDefinitionForDeserialization(readContext, pojoType, _visibility);
    }

    @Override
    public POJODefinition pojoDefinitionForSerialization(JSONWriter writeContext, Class<?> pojoType) {
        return AnnotationBasedIntrospector.pojoDefinitionForSerialization(writeContext, pojoType, _visibility);
    }

    @Override
    public ValueWriter overrideStandardValueWriter(JSONWriter writeContext, Class<?> type, int stdTypeId) {
        if (stdTypeId == SER_ENUM) {
            return new EnumWriter(type);
        }
        return null;
    }

    private static class EnumWriter implements ValueWriter {
        private final Class<?> _valueType;
        private final Map<String, String> enumMap;

        public EnumWriter(Class<?> type) {
            _valueType = type;
            enumMap = new HashMap<String, String>();
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(JsonProperty.class)) {
                    enumMap.put(field.getName(), field.getAnnotation(JsonProperty.class).value());
                } else {
                    enumMap.put(field.getName(), field.getName());
                }
            }
        }

        @Override
        public void writeValue(JSONWriter context, JsonGenerator g, Object value) throws IOException {
            context.writeValue(enumMap.get(((Enum) value).name()));
        }

        @Override
        public Class<?> valueType() {
            return _valueType;
        }
    }
}
