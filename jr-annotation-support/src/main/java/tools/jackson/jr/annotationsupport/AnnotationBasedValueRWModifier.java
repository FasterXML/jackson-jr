package tools.jackson.jr.annotationsupport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;

import tools.jackson.jr.ob.JSONObjectException;
import tools.jackson.jr.ob.api.ReaderWriterModifier;
import tools.jackson.jr.ob.api.ValueReader;
import tools.jackson.jr.ob.api.ValueWriter;
import tools.jackson.jr.ob.impl.JSONReader;
import tools.jackson.jr.ob.impl.JSONWriter;
import tools.jackson.jr.ob.impl.POJODefinition;
import tools.jackson.jr.ob.impl.ValueLocatorBase;

public class AnnotationBasedValueRWModifier extends ReaderWriterModifier
{
    // Has to match SER_ENUM code in ValueLocatorBase
    protected static final int SER_ENUM_ID = ValueLocatorBase.SER_ENUM;

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
        if (stdTypeId == SER_ENUM_ID) {
            ValueWriter writeByJsonValue = EnumJsonValueWriter.of(type);
            if (writeByJsonValue != null) {
                return writeByJsonValue;
            }
            // the EnumWriter requires this to be the actual enum type, rather than a subclass of Enum
            if (type.isEnum()) {
                return new EnumWriter(type);
            }
        }
        return null;
    }

    @Override // since 2.14
    public ValueReader modifyValueReader(JSONReader readContext, Class<?> type, ValueReader defaultReader) {
        if (type.isEnum()) {
            ValueReader readUsingJsonCreator = EnumJsonCreatorReader.of(type);
            if (readUsingJsonCreator != null) {
                return readUsingJsonCreator;
            }
        }
        return super.modifyValueReader(readContext, type, defaultReader);
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
        public void writeValue(JSONWriter context, JsonGenerator g, Object value)
            throws JacksonException
        {
            context.writeValue(enumMap.get(((Enum<?>) value).name()));
        }

        @Override
        public Class<?> valueType() {
            return _valueType;
        }
    }

    /**
     * Serialize an enum using the {@link JsonValue} tagged method.
     *
     * @since 2.14
     */
    private static class EnumJsonValueWriter implements ValueWriter
    {
        private final Class<?> _valueType;
        private final Method _jsonValueMethod;

        private EnumJsonValueWriter(Class<?> _valueType, Method _jsonValueMethod) {
            this._valueType = _valueType;
            this._jsonValueMethod = _jsonValueMethod;
            _jsonValueMethod.setAccessible(true);
        }

        @Override
        public void writeValue(JSONWriter context, JsonGenerator g, Object pojo)
                throws JacksonException
        {
            final Object value;
            try {
                value = _jsonValueMethod.invoke(pojo);
            } catch (Exception e) {
                throw new JSONObjectException("Cannot call JsonValue method", e);
            }
            context.writeValue(value);
        }

        @Override
        public Class<?> valueType() {
            return _valueType;
        }

        /**
         * Scan the methods of the enum to find a {@link JsonValue} tagged method
         * which takes no parameters and returns a {@link String}
         * @param type the type
         * @return either a {@link EnumJsonValueWriter} to write this enum with, or <code>null</code> if no suitable
         * method found
         */
        public static EnumJsonValueWriter of(Class<?> type) {
            return getJsonValueFunction(type, type);
        }

        private static EnumJsonValueWriter getJsonValueFunction(Class<?> type, Class<?> inspectionType) {
            for (Method method : inspectionType.getDeclaredMethods()) {
                JsonValue jsonValueAnnotation = method.getDeclaredAnnotation(JsonValue.class);
                if (!Modifier.isStatic(method.getModifiers()) &&
                        jsonValueAnnotation != null &&
                        method.getParameterCount() == 0 &&
                        method.getReturnType().equals(String.class)) {
                    // this is the @JsonValue on the lowest descendent of any hierarchy
                    // it may be put here to disable it
                    if (jsonValueAnnotation.value()) {
                        return new EnumJsonValueWriter(type, method);
                    }

                    // if @JsonValue is disabled, then we deliberately stop searching for another one
                    return null;
                }
            }

            Class<?> superClass = inspectionType.getSuperclass();
            if (superClass != null) {
                EnumJsonValueWriter writer = getJsonValueFunction(type, superClass);
                if (writer != null) {
                    return writer;
                }
            }

            for (Class<?> parentInterface : inspectionType.getInterfaces()) {
                EnumJsonValueWriter writer = getJsonValueFunction(type, parentInterface);
                if (writer != null) {
                    return writer;
                }
            }

            return null;
        }
    }

    /**
     * Deserialize into an enum using the {@link JsonCreator} tagged method
     *
     * @since 2.14
     */
    private static class EnumJsonCreatorReader extends ValueReader
    {
        private final Method _jsonCreatorMethod;

        private EnumJsonCreatorReader(Class<?> valueType, Method jsonCreatorMethod) {
            super(valueType);
            this._jsonCreatorMethod = jsonCreatorMethod;
            jsonCreatorMethod.setAccessible(true);
        }

        @Override
        public Object read(JSONReader reader, JsonParser p) throws JacksonException
        {
            try {
                return _jsonCreatorMethod.invoke(_valueType, p.getText());
            } catch (Exception e) {
                throw new JSONObjectException("Cannot call JsonCreator method", e);
            }
        }

        /**
         * Scan the methods of the enum type to find a static method tagged with {@link JsonCreator}
         * which takes exactly one parameter and returns a value of the enum's type
         * @param type the type to scan
         * @return a new {@link EnumJsonCreatorReader} to deserialize with, or <code>null</code> if there
         * is no {@link JsonCreator} method to use
         */
        public static EnumJsonCreatorReader of(Class<?> type) {
            for (Method method : type.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) &&
                        method.getDeclaredAnnotation(JsonCreator.class) != null &&
                        method.getParameterCount() == 1 &&
                        method.getReturnType().equals(type)) {
                    return new EnumJsonCreatorReader(type, method);
                }
            }
            return null;
        }
    }
}
