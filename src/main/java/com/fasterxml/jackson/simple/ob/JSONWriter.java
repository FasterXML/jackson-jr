package com.fasterxml.jackson.simple.ob;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.simple.ob.impl.SimpleType;
import com.fasterxml.jackson.simple.ob.impl.TypeDetector;

/**
 * Object that handles serialization of simple Objects into
 * JSON.
 *<p>
 * Life-cycle is such that initial instance (called blueprint)
 * is constructed first (including possible configuration 
 * using mutant factory methods). This blueprint object
 * acts as a factory, and is never used for direct writing;
 * instead, per-call instance is created by calling
 * {@link #newWriter}.
 */
public class JSONWriter
{
    /*
    /**********************************************************************
    /* Blueprint config
    /**********************************************************************
     */

    protected final int _features;
    
    /**
     * Object that is used to 
     */
    protected final TypeDetector _typeDetector;

    /*
    /**********************************************************************
    /* Instance config
    /**********************************************************************
     */

    protected final JsonGenerator _generator;
    
    /*
    /**********************************************************************
    /* Blueprint construction, configuration
    /**********************************************************************
     */

    /**
     * Constructor used for creating the default blueprint instance.
     */
    protected JSONWriter(int features)
    {
        this(features, TypeDetector.rootDetector());
    }

    /**
     * Constructor used for creating differently configured blueprint
     * instances
     */
    protected JSONWriter(int features, TypeDetector td)
    {
        _features = features;
        _typeDetector = td;
        _generator = null;
    }

    /**
     * Constructor for non-blueprint instances
     */
    protected JSONWriter(JSONWriter base, JsonGenerator jgen)
    {
        _features = base._features;
        _typeDetector = base._typeDetector.perOperationInstance();
        _generator = jgen;
    }
    
    /*
    /**********************************************************************
    /* New instance creation
    /**********************************************************************
     */

    public JSONWriter newWriter(JsonGenerator jg)
    {
        return new JSONWriter(this, jg);
    }
    
    /*
    /**********************************************************************
    /* Public entry methods
    /**********************************************************************
     */

    public void writeValue(Object value) throws IOException, JsonProcessingException
    {
        if (value == null) {
            writeNullValue();
            return;
        }
        SimpleType type = _typeDetector.findType(value.getClass());
        switch (type) {

        // Textual types, similar:

        case STRING:
            writeStringValue((String) value);
            return;
        case CHAR_ARRAY:
            writeStringValue(new String((char[]) value));
            return;
        case CHAR:
            writeStringValue(String.valueOf(value));
            return;
        case CHARACTER_SEQUENCE:
            writeStringValue(((CharSequence) value).toString());
            return;
        case BYTE_ARRAY:
            writeBinaryValue((byte[]) value);
            return;
        case INT_ARRAY:
            writeIntArrayValue((int[]) value);
            return;

            // Number types:
        
        case NUMBER_BIG_DECIMAL:
            writeBigDecimalValue((BigDecimal) value);
            return;
        case NUMBER_BIG_INTEGER:
            writeBigIntegerValue((BigInteger) value);
            return;
        case NUMBER_FLOAT: // fall through
        case NUMBER_DOUBLE:
            writeDoubleValue(((Number) value).doubleValue());
            return;
        case NUMBER_BYTE: // fall through
        case NUMBER_SHORT: // fall through
        case NUMBER_INTEGER:
            writeIntValue(((Number) value).intValue());
            return;
        case NUMBER_LONG:
            writeLongValue(((Number) value).longValue());
            return;
        case NUMBER_OTHER:
            writeNumberValue((Number) value);
            return;

        // Scalar types:

        case BOOLEAN:
            writeBooleanValue(((Boolean) value).booleanValue());
            return;
        case DATE:
            writeDateValue((Date) value);
            return;
        case ENUM:
            writeEnumValue((Enum<?>) value);
            return;
            
        // Structured types:

        case COLLECTION:
            writeCollectionValue((Collection<?>) value);
            return;
        case ITERABLE:
            writeIterableValue((Iterable<?>) value);
            return;
        case LIST:
            writeListValue((List<?>) value);
            return;
        case MAP:
            writeMapValue((Map<?,?>) value);
            return;
        case OBJECT_ARRAY:
            writeObjectArrayValue((Object[]) value);
            return;
            
        case OTHER:
            writeUnknownValue(value);
            return;
        }
        throw new IllegalStateException("Unsupported type: "+type);
    }

    public void writeField(String fieldName, Object value) throws IOException, JsonProcessingException
    {
        if (value == null) {
            writeNullField(fieldName);
            return;
        }

        SimpleType type = _typeDetector.findType(value.getClass());
        switch (type) {

        // Textual types, similar:

        case STRING:
            writeStringField(fieldName, (String) value);
            return;
        case CHAR_ARRAY:
            writeStringField(fieldName, new String((char[]) value));
            return;
        case CHAR:
            writeStringField(fieldName, String.valueOf(value));
            return;
        case CHARACTER_SEQUENCE:
            writeStringField(fieldName, ((CharSequence) value).toString());
            return;
        case BYTE_ARRAY:
            writeBinaryField(fieldName, (byte[]) value);
            return;
        case INT_ARRAY:
            writeIntArrayField(fieldName, (int[]) value);
            return;

            // Number types:
        
        case NUMBER_BIG_DECIMAL:
            writeBigDecimalField(fieldName, (BigDecimal) value);
            return;
        case NUMBER_BIG_INTEGER:
            writeBigIntegerField(fieldName, (BigInteger) value);
            return;
        case NUMBER_FLOAT: // fall through
        case NUMBER_DOUBLE:
            writeDoubleField(fieldName, ((Number) value).doubleValue());
            return;
        case NUMBER_BYTE: // fall through
        case NUMBER_SHORT: // fall through
        case NUMBER_INTEGER:
            writeIntField(fieldName, ((Number) value).intValue());
            return;
        case NUMBER_LONG:
            writeLongField(fieldName, ((Number) value).longValue());
            return;
        case NUMBER_OTHER:
            writeNumberField(fieldName, (Number) value);
            return;

        // Scalar types:

        case BOOLEAN:
            writeBooleanField(fieldName, ((Boolean) value).booleanValue());
            return;
        case DATE:
            writeDateField(fieldName, (Date) value);
            return;
        case ENUM:
            writeEnumField(fieldName, (Enum<?>) value);
            return;
            
        // Structured types:

        case COLLECTION:
            writeCollectionField(fieldName, (Collection<?>) value);
            return;
        case ITERABLE:
            writeIterableField(fieldName, (Iterable<?>) value);
            return;
        case LIST:
            writeListField(fieldName, (List<?>) value);
            return;
        case MAP:
            writeMapField(fieldName, (Map<?,?>) value);
            return;
        case OBJECT_ARRAY:
            writeObjectArrayField(fieldName, (Object[]) value);
            return;
            
        case OTHER:
            writeUnknownField(fieldName, value);
            return;
        }
        throw new IllegalStateException("Unsupported type: "+type);
    }
    
    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, structured types
    /**********************************************************************
     */
    
    protected void writeCollectionValue(Collection<?> v) throws IOException, JsonProcessingException
    {
        _generator.writeStartArray();
        for (Object ob : v) {
            writeValue(ob);
        }
        _generator.writeEndArray();
    }

    protected void writeCollectionField(String fieldName, Collection<?> v) throws IOException, JsonProcessingException
    {
        _generator.writeFieldName(fieldName);
        writeCollectionValue(v);
    }
    
    protected void writeIterableValue(Iterable<?> v) throws IOException, JsonProcessingException
    {
        _generator.writeStartArray();
        for (Object ob : v) {
            writeValue(ob);
        }
        _generator.writeEndArray();
    }

    protected void writeIterableField(String fieldName, Iterable<?> v) throws IOException, JsonProcessingException
    {
        _generator.writeFieldName(fieldName);
        writeIterableValue(v);
    }
    
    protected void writeListValue(List<?> v) throws IOException, JsonProcessingException
    {
        _generator.writeStartArray();
        for (int i = 0, len = v.size(); i < len; ++i) {
            writeValue(v.get(i));
        }
        _generator.writeEndArray();
    }

    protected void writeListField(String fieldName, List<?> v) throws IOException, JsonProcessingException
    {
        _generator.writeFieldName(fieldName);
        writeListValue(v);
    }
    
    protected void writeMapValue(Map<?,?> v) throws IOException, JsonProcessingException
    {
        _generator.writeStartObject();
        if (!v.isEmpty()) {
            for (Map.Entry<?,?> entry : v.entrySet()) {
                writeField(keyToString(entry.getKey()), entry.getValue());
            }
        }
        _generator.writeEndObject();
    }

    protected void writeMapField(String fieldName, Map<?,?> v) throws IOException, JsonProcessingException
    {
        _generator.writeFieldName(fieldName);
        writeMapValue(v);
    }

    protected void writeObjectArrayValue(Object[] v) throws IOException, JsonProcessingException {
        _generator.writeStartArray();
        for (int i = 0, len = v.length; i < len; ++i) {
            writeValue(v[i]);
        }
        _generator.writeEndArray();
    }

    protected void writeObjectArrayField(String fieldName, Object[] v) throws IOException, JsonProcessingException
    {
        _generator.writeFieldName(fieldName);
        writeObjectArrayValue(v);
    }
    
    protected void writeIntArrayValue(int[] v) throws IOException, JsonProcessingException {
        _generator.writeStartArray();
        for (int i = 0, len = v.length; i < len; ++i) {
            _generator.writeNumber(v[i]);
        }
        _generator.writeEndArray();
    }

    protected void writeIntArrayField(String fieldName, int[] v) throws IOException, JsonProcessingException
    {
        _generator.writeFieldName(fieldName);
        writeIntArrayValue(v);
    }
    
    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, primitives
    /**********************************************************************
     */

    protected void writeBooleanValue(boolean v) throws IOException, JsonProcessingException {
        _generator.writeBoolean(v);
    }

    protected void writeBooleanField(String fieldName, boolean v) throws IOException, JsonProcessingException {
        _generator.writeBooleanField(fieldName, v);
    }

    protected void writeIntValue(int v) throws IOException, JsonProcessingException {
        _generator.writeNumber(v);
    }

    protected void writeIntField(String fieldName, int v) throws IOException, JsonProcessingException {
        _generator.writeNumberField(fieldName, v);
    }

    protected void writeLongValue(long v) throws IOException, JsonProcessingException {
        _generator.writeNumber(v);
    }

    protected void writeBigIntegerValue(BigInteger v) throws IOException, JsonProcessingException {
        _generator.writeNumber(v);
    }

    protected void writeBigIntegerField(String fieldName, BigInteger v) throws IOException, JsonProcessingException {
        _generator.writeFieldName(fieldName);
        writeBigIntegerValue(v);
    }
    
    protected void writeLongField(String fieldName, long v) throws IOException, JsonProcessingException {
        _generator.writeNumberField(fieldName, v);
    }
    
    protected void writeDoubleValue(double v) throws IOException, JsonProcessingException {
        _generator.writeNumber(v);
    }

    protected void writeDoubleField(String fieldName, double v) throws IOException, JsonProcessingException {
        _generator.writeNumberField(fieldName, v);
    }

    protected void writeBigDecimalValue(BigDecimal v) throws IOException, JsonProcessingException {
        _generator.writeNumber(v);
    }

    protected void writeBigDecimalField(String fieldName, BigDecimal v) throws IOException, JsonProcessingException {
        _generator.writeNumberField(fieldName, v);
    }

    protected void writeNumberValue(Number v) throws IOException, JsonProcessingException {
        // Unknown type; must use fallback method
        _generator.writeNumber(v.toString());
    }

    protected void writeNumberField(String fieldName, Number v) throws IOException, JsonProcessingException {
        _generator.writeFieldName(fieldName);
        writeNumberValue(v);
    }
    
    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, textual
    /**********************************************************************
     */

    protected void writeStringValue(String v) throws IOException, JsonProcessingException {
        _generator.writeString(v);
    }

    protected void writeStringField(String fieldName, String v) throws IOException, JsonProcessingException {
        _generator.writeStringField(fieldName, v);
    }

    protected void writeBinaryValue(byte[] data) throws IOException, JsonProcessingException {
        _generator.writeBinary(data);
    }

    protected void writeBinaryField(String fieldName, byte[] data) throws IOException, JsonProcessingException {
        _generator.writeBinaryField(fieldName, data);
    }

    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, other
    /**********************************************************************
     */

    protected void writeNullValue() throws IOException, JsonProcessingException {
        _generator.writeNull();
    }

    protected void writeNullField(String fieldName) throws IOException, JsonProcessingException {
        _generator.writeNullField(fieldName);
    }

    protected void writeDateValue(Date v) throws IOException, JsonProcessingException {
        // TODO: maybe allow serialization using timestamp?
        writeStringValue(v.toString());
    }

    protected void writeDateField(String fieldName, Date v) throws IOException, JsonProcessingException {
        writeStringField(fieldName, v.toString());
    }

    protected void writeEnumValue(Enum<?> v) throws IOException, JsonProcessingException {
        // TODO: maybe allow serialization using index?
        writeStringValue(v.toString());
    }

    protected void writeEnumField(String fieldName, Enum<?> v) throws IOException, JsonProcessingException {
        writeStringField(fieldName, v.toString());
    }

    protected void writeUnknownValue(Object data) throws IOException, JsonProcessingException {
        writeStringValue(data.toString());
    }

    protected void writeUnknownField(String fieldName, Object data) throws IOException, JsonProcessingException {
        writeStringField(fieldName, data.toString());
    }

    /*
    /**********************************************************************
    /* Overridable concrete typed write methods; key conversions:
    /**********************************************************************
     */

    protected String keyToString(Object rawKey)
    {
        if (rawKey instanceof String) {
            return (String) rawKey;
        }
        return String.valueOf(rawKey);
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
}
