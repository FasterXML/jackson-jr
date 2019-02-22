package com.fasterxml.jackson.jr.ob.impl;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

import static com.fasterxml.jackson.jr.ob.impl.WriteTypeDetector.*;

/**
 * Object that handles serialization of simple Objects into underlying
 * data format (usually JSON).
 * Unlike {@link JSONReader}, writer does actually implement write methods itself
 * and uses delegation for only some special cases.
 *<p>
 * Life-cycle is such that initial instance (called blueprint)
 * is constructed first (including possible configuration 
 * using mutant factory methods). This blueprint object
 * acts as a factory, and is never used for direct writing;
 * instead, per-call instance is created by calling
 * {@link #perOperationInstance}.
 */
public class JSONWriter
{
    private final static TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("UTC");

    /*
    /**********************************************************************
    /* Blueprint config
    /**********************************************************************
     */

    protected final int _features;

    protected final boolean _writeNullValues;

    /**
     * Object that is used to resolve types of values dynamically.
     */
    protected final WriteTypeDetector _typeDetector;

    protected final TreeCodec _treeCodec;

    /*
    /**********************************************************************
    /* Instance config
    /**********************************************************************
     */

    protected final JsonGenerator _generator;

    protected final TimeZone _timezone;

    /*
    /**********************************************************************
    /* Blueprint construction
    /**********************************************************************
     */

    /**
     * Constructor used for creating differently configured blueprint
     * instances
     */
    public JSONWriter(int features, WriteTypeDetector td, TreeCodec tc)
    {
        _features = features;
        _writeNullValues = JSON.Feature.WRITE_NULL_PROPERTIES.isEnabled(features);
        _typeDetector = td;
        _treeCodec = tc;
        _generator = null;
        _timezone = DEFAULT_TIMEZONE;
    }

    /**
     * Constructor for non-blueprint instances
     */
    protected JSONWriter(JSONWriter base, int features, WriteTypeDetector td, JsonGenerator g)
    {
        _features = features;
        _writeNullValues = JSON.Feature.WRITE_NULL_PROPERTIES.isEnabled(features);
        _typeDetector = td;
        _treeCodec = base._treeCodec;
        _generator = g;
        _timezone = DEFAULT_TIMEZONE;
    }

    /*
    /**********************************************************************
    /* Mutant factories for blueprint
    /**********************************************************************
     */

    public JSONWriter with(TreeCodec tc) {
        if (_treeCodec == tc) {
            return this;
        }
        return _with(_features, _typeDetector, tc);
    }

    /**
     * Overridable method that all mutant factories call if a new instance
     * is to be constructed
     */
    protected JSONWriter _with(int features, WriteTypeDetector td, TreeCodec tc)
    {
        if (getClass() != JSONWriter.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override _with(...)");
        }
        return new JSONWriter(features, td, tc);
    }

    /*
    /**********************************************************************
    /* New instance creation
    /**********************************************************************
     */

    public JSONWriter perOperationInstance(int features, JsonGenerator g)
    {
        if (getClass() != JSONWriter.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override perOperationInstance(...)");
        }
        return new JSONWriter(this, features,
                _typeDetector.perOperationInstance(this, features), g);
    }

    /*
    /**********************************************************************
    /* Public entry methods
    /**********************************************************************
     */

    /**
     * Main entry point for non-blueprint instances: called for the root value to
     * write it out.
     */
    public void writeValue(Object value) throws IOException
    {
        if (value == null) {
            writeNullValue();
            return;
        }
        _writeValue(value, _typeDetector.findSerializationType(value.getClass()));
    }

    @Deprecated // since 2.8
    public void writeField(String fieldName, Object value) throws IOException
    {
        if (value == null) {
            if (_writeNullValues) {
                writeNullField(fieldName);
            }
            return;
        }
        writeField(fieldName, value, _typeDetector.findSerializationType(value.getClass()));
    }

    public void writeField(String fieldName, Object value, int type) throws IOException
    {
        switch (type) {
        // Structured types:
        case SER_MAP:
            writeMapField(fieldName, (Map<?,?>) value);
            return;
        case SER_LIST:
            writeListField(fieldName, (List<?>) value);
            return;
        case SER_COLLECTION:
            writeCollectionField(fieldName, (Collection<?>) value);
            return;
        case SER_OBJECT_ARRAY:
            writeObjectArrayField(fieldName, (Object[]) value);
            return;
        case SER_INT_ARRAY:
            writeIntArrayField(fieldName, (int[]) value);
            return;
        case SER_LONG_ARRAY:
            writeLongArrayField(fieldName, (long[]) value);
            return;
        case SER_BOOLEAN_ARRAY:
            writeBooleanArrayField(fieldName, (boolean[]) value);
            return;
        case SER_TREE_NODE:
            writeTreeNodeField(fieldName, (TreeNode) value);
            return;

        // Textual types, similar:

        case SER_STRING:
            writeStringField(fieldName, (String) value);
            return;
        case SER_CHAR_ARRAY:
            writeStringField(fieldName, new String((char[]) value));
            return;
        case SER_CHARACTER_SEQUENCE:
            writeStringField(fieldName, ((CharSequence) value).toString());
            return;
        case SER_BYTE_ARRAY:
            writeBinaryField(fieldName, (byte[]) value);
            return;

        // Number types:
        case SER_NUMBER_BIG_DECIMAL:
            writeBigDecimalField(fieldName, (BigDecimal) value);
            return;
        case SER_NUMBER_BIG_INTEGER:
            writeBigIntegerField(fieldName, (BigInteger) value);
            return;
        case SER_NUMBER_FLOAT: // fall through
        case SER_NUMBER_DOUBLE:
            writeDoubleField(fieldName, ((Number) value).doubleValue());
            return;
        case SER_NUMBER_BYTE: // fall through
        case SER_NUMBER_SHORT: // fall through
        case SER_NUMBER_INTEGER:
            writeIntField(fieldName, ((Number) value).intValue());
            return;
        case SER_NUMBER_LONG:
            writeLongField(fieldName, ((Number) value).longValue());
            return;

        // Scalar types:

        case SER_BOOLEAN:
            writeBooleanField(fieldName, ((Boolean) value).booleanValue());
            return;
        case SER_CHAR:
            writeStringField(fieldName, String.valueOf(value));
            return;
        case SER_CALENDAR:
            writeDateField(fieldName, ((Calendar) value).getTime());
            return;
        case SER_DATE:
            writeDateField(fieldName, (Date) value);
            return;
        case SER_ENUM:
            writeEnumField(fieldName, (Enum<?>) value);
            return;
        case SER_CLASS:
            writeStringLikeField(fieldName, ((Class<?>) value).getName(), type);
            return;
        case SER_FILE:
            writeStringLikeField(fieldName, ((File) value).getAbsolutePath(), type);
            return;
        case SER_UUID:
        case SER_URL:
        case SER_URI:
            writeStringLikeField(fieldName, value.toString(), type);
            return;

        // Others
            
        case SER_ITERABLE:
            writeIterableField(fieldName, (Iterable<?>) value);
            return;

        case SER_UNKNOWN:
            writeUnknownField(fieldName, value);
            return;
        }

        if (type < 0) { // Bean type!
            BeanPropertyWriter[] props = _typeDetector.getPropertyWriters(type);
            if (props != null) { // sanity check
                _generator.writeFieldName(fieldName);
                writeBeanValue(props, value);
                return;
            }
        }
        _badType(type, value);
    }

    protected void _writeValue(Object value, int type) throws IOException
    {
        switch (type) {

        // Structured types:
        case SER_MAP:
            writeMapValue((Map<?,?>) value);
            return;
        case SER_LIST:
            writeListValue((List<?>) value);
            return;
        case SER_COLLECTION:
            writeCollectionValue((Collection<?>) value);
            return;
        case SER_OBJECT_ARRAY:
            writeObjectArrayValue((Object[]) value);
            return;
        case SER_INT_ARRAY:
            writeIntArrayValue((int[]) value);
            return;
        case SER_LONG_ARRAY:
            writeLongArrayValue((long[]) value);
            return;
        case SER_BOOLEAN_ARRAY:
            writeBooleanArrayValue((boolean[]) value);
            return;
        case SER_TREE_NODE:
            writeTreeNodeValue((TreeNode) value);
            return;

        // Textual types, related:
        case SER_STRING:
            writeStringValue((String) value);
            return;
        case SER_CHAR_ARRAY:
            writeStringValue(new String((char[]) value));
            return;
        case SER_CHARACTER_SEQUENCE:
            writeStringValue(((CharSequence) value).toString());
            return;
        case SER_BYTE_ARRAY:
            writeBinaryValue((byte[]) value);
            return;

        // Number types:
            
        case SER_NUMBER_FLOAT: // fall through
        case SER_NUMBER_DOUBLE:
            writeDoubleValue(((Number) value).doubleValue());
            return;
        case SER_NUMBER_BYTE: // fall through
        case SER_NUMBER_SHORT: // fall through
        case SER_NUMBER_INTEGER:
            writeIntValue(((Number) value).intValue());
            return;
        case SER_NUMBER_LONG:
            writeLongValue(((Number) value).longValue());
            return;
        case SER_NUMBER_BIG_DECIMAL:
            writeBigDecimalValue((BigDecimal) value);
            return;
        case SER_NUMBER_BIG_INTEGER:
            writeBigIntegerValue((BigInteger) value);
            return;

        // Other scalar types:

        case SER_BOOLEAN:
            writeBooleanValue(((Boolean) value).booleanValue());
            return;
        case SER_CHAR:
            writeStringValue(String.valueOf(value));
            return;
        case SER_CALENDAR:
            writeDateValue(((Calendar) value).getTime());
            return;
        case SER_DATE:
            writeDateValue((Date) value);
            return;

        case SER_ENUM:
            writeEnumValue((Enum<?>) value);
            return;
        case SER_CLASS:
            writeStringLikeValue(((Class<?>) value).getName(), type);
            return;
        case SER_FILE:
            writeStringLikeValue(((File) value).getAbsolutePath(), type);
            return;
            // these type should be fine using toString()
        case SER_UUID:
        case SER_URL:
        case SER_URI:
            writeStringLikeValue(value.toString(), type);
            return;

        case SER_ITERABLE:
            writeIterableValue((Iterable<?>) value);
            return;
        case SER_UNKNOWN:
            writeUnknownValue(value);
            return;
        }

        if (type < 0) { // Bean type!
            BeanPropertyWriter[] props = _typeDetector.getPropertyWriters(type);
            if (props != null) { // sanity check
                writeBeanValue(props, value);
                return;
            }
        }
        _badType(type, value);
    }

    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, structured types
    /**********************************************************************
     */
    
    protected void writeCollectionValue(Collection<?> v) throws IOException
    {
        _generator.writeStartArray();
        for (Object ob : v) {
            writeValue(ob);
        }
        _generator.writeEndArray();
    }

    protected void writeCollectionField(String fieldName, Collection<?> v) throws IOException
    {
        _generator.writeFieldName(fieldName);
        writeCollectionValue(v);
    }
    
    protected void writeIterableValue(Iterable<?> v) throws IOException
    {
        _generator.writeStartArray();
        for (Object ob : v) {
            writeValue(ob);
        }
        _generator.writeEndArray();
    }

    protected void writeIterableField(String fieldName, Iterable<?> v) throws IOException
    {
        _generator.writeFieldName(fieldName);
        writeIterableValue(v);
    }
    
    protected void writeListValue(List<?> list) throws IOException
    {
        _generator.writeStartArray();
        for (int i = 0, len = list.size(); i < len; ++i) {
            Object value = list.get(i);
            if (value == null) {
                _generator.writeNull();
                continue;
            }
            _writeValue(value, _typeDetector.findSerializationType(value.getClass()));
        }
        _generator.writeEndArray();
    }

    protected void writeListField(String fieldName, List<?> v) throws IOException
    {
        _generator.writeFieldName(fieldName);
        writeListValue(v);
    }
    
    protected void writeMapValue(Map<?,?> v) throws IOException
    {
        _generator.writeStartObject();
        if (!v.isEmpty()) {
            for (Map.Entry<?,?> entry : v.entrySet()) {
                String key = keyToString(entry.getKey());
                Object value = entry.getValue();

                if (value == null) {
                    if (_writeNullValues) {
                        writeNullField(key);
                    }
                    continue;
                }
                Class<?> cls = value.getClass();
                int type = _typeDetector.findSerializationType(cls);
                writeField(key, value, type);
            }
        }
        _generator.writeEndObject();
    }

    protected void writeMapField(String fieldName, Map<?,?> v) throws IOException
    {
        _generator.writeFieldName(fieldName);
        writeMapValue(v);
    }

    protected void writeObjectArrayValue(Object[] v) throws IOException {
        _generator.writeStartArray();
        for (int i = 0, len = v.length; i < len; ++i) {
            writeValue(v[i]);
        }
        _generator.writeEndArray();
    }

    protected void writeObjectArrayField(String fieldName, Object[] v) throws IOException {
        _generator.writeFieldName(fieldName);
        writeObjectArrayValue(v);
    }

    protected void writeIntArrayValue(int[] v) throws IOException {
        _generator.writeStartArray();
        for (int i = 0, len = v.length; i < len; ++i) {
            _generator.writeNumber(v[i]);
        }
        _generator.writeEndArray();
    }

    protected void writeIntArrayField(String fieldName, int[] v) throws IOException {
        _generator.writeFieldName(fieldName);
        writeIntArrayValue(v);
    }
    
    protected void writeLongArrayValue(long[] v) throws IOException {
        _generator.writeStartArray();
        for (int i = 0, len = v.length; i < len; ++i) {
            _generator.writeNumber(v[i]);
        }
        _generator.writeEndArray();
    }

    protected void writeLongArrayField(String fieldName, long[] v) throws IOException {
        _generator.writeFieldName(fieldName);
        writeLongArrayValue(v);
    }
    
    protected void writeBooleanArrayValue(boolean[] v) throws IOException {
        _generator.writeStartArray();
        for (int i = 0, len = v.length; i < len; ++i) {
            _generator.writeBoolean(v[i]);
        }
        _generator.writeEndArray();
    }

    protected void writeBooleanArrayField(String fieldName, boolean[] v) throws IOException {
        _generator.writeFieldName(fieldName);
        writeBooleanArrayValue(v);
    }

    protected void writeTreeNodeValue(TreeNode v) throws IOException {
        if (_treeCodec == null) {
            throw new JSONObjectException("No TreeCodec configured: can not serializer TreeNode values");
        }
        _treeCodec.writeTree(_generator, v);
    }

    protected void writeTreeNodeField(String fieldName, TreeNode v) throws IOException
    {
        _generator.writeFieldName(fieldName);
        writeTreeNodeValue(v);
    }
    
    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, primitives
    /**********************************************************************
     */

    protected void writeBooleanValue(boolean v) throws IOException {
        _generator.writeBoolean(v);
    }

    protected void writeBooleanField(String fieldName, boolean v) throws IOException {
        _generator.writeBooleanField(fieldName, v);
    }

    protected void writeIntValue(int v) throws IOException {
        _generator.writeNumber(v);
    }

    protected void writeIntField(String fieldName, int v) throws IOException {
        _generator.writeNumberField(fieldName, v);
    }

    protected void writeLongValue(long v) throws IOException {
        _generator.writeNumber(v);
    }

    protected void writeBigIntegerValue(BigInteger v) throws IOException {
        _generator.writeNumber(v);
    }

    protected void writeBigIntegerField(String fieldName, BigInteger v) throws IOException {
        _generator.writeFieldName(fieldName);
        writeBigIntegerValue(v);
    }
    
    protected void writeLongField(String fieldName, long v) throws IOException {
        _generator.writeNumberField(fieldName, v);
    }
    
    protected void writeDoubleValue(double v) throws IOException {
        _generator.writeNumber(v);
    }

    protected void writeDoubleField(String fieldName, double v) throws IOException {
        _generator.writeNumberField(fieldName, v);
    }

    protected void writeBigDecimalValue(BigDecimal v) throws IOException {
        _generator.writeNumber(v);
    }

    protected void writeBigDecimalField(String fieldName, BigDecimal v) throws IOException {
        _generator.writeNumberField(fieldName, v);
    }

    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, textual
    /**********************************************************************
     */

    protected void writeStringValue(String v) throws IOException {
        _generator.writeString(v);
    }

    protected void writeStringField(String fieldName, String v) throws IOException {
        _generator.writeStringField(fieldName, v);
    }

    protected void writeStringLikeValue(String v, int actualType) throws IOException {
        _generator.writeString(v);
    }

    protected void writeStringLikeField(String fieldName, String v, int actualType) throws IOException {
        _generator.writeStringField(fieldName, v);
    }
    
    protected void writeBinaryValue(byte[] data) throws IOException {
        _generator.writeBinary(data);
    }

    protected void writeBinaryField(String fieldName, byte[] data) throws IOException {
        _generator.writeBinaryField(fieldName, data);
    }

    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, other
    /**********************************************************************
     */

    protected void writeNullValue() throws IOException {
        _generator.writeNull();
    }

    protected void writeNullField(String fieldName) throws IOException {
        if (_writeNullValues) {
            _generator.writeNullField(fieldName);
        }
    }

    protected void writeNullField(SerializedString fieldName) throws IOException {
        if (_writeNullValues) {
            _generator.writeFieldName(fieldName);
            _generator.writeNull();
        }
    }

    protected void writeDateValue(Date v) throws IOException {
        if (JSON.Feature.WRITE_DATES_AS_TIMESTAMP.isEnabled(_features)) {
            writeLongValue(v.getTime());
        } else {
            writeStringValue(dateToString(v));
        }
    }

    protected void writeDateField(String fieldName, Date v) throws IOException {
        if (JSON.Feature.WRITE_DATES_AS_TIMESTAMP.isEnabled(_features)) {
            writeLongField(fieldName, v.getTime());
        } else {
            writeStringField(fieldName, dateToString(v));
        }
    }

    protected void writeEnumValue(Enum<?> v) throws IOException {
        if (JSON.Feature.WRITE_ENUMS_USING_INDEX.isEnabled(_features)) {
            writeIntValue(v.ordinal());
        } else {
            writeStringValue(v.toString());
        }
    }

    protected void writeEnumField(String fieldName, Enum<?> v) throws IOException {
        if (JSON.Feature.WRITE_ENUMS_USING_INDEX.isEnabled(_features)) {
            writeIntField(fieldName, v.ordinal());
        } else {
            writeStringField(fieldName, v.toString());
        }
    }

    protected void writeBeanValue(BeanPropertyWriter[] props, Object bean) throws IOException
    {
        _generator.writeStartObject();
        for (int i = 0, end = props.length; i < end; ++i) {
            BeanPropertyWriter property = props[i];
            SerializedString name = property.name;
            Object value = property.getValueFor(bean);
            if (value == null) {
                if (_writeNullValues) {
                    writeNullField(name);
                }
                continue;
            }
            int typeId = property.typeId;
            if (typeId == 0) {
                typeId = _typeDetector.findSerializationType(value.getClass());
            }
            _generator.writeFieldName(name);
            _writeValue(value, typeId);
        }
        _generator.writeEndObject();
    }

    protected void writeUnknownValue(Object data) throws IOException {
        _checkUnknown(data);
        writeStringValue(data.toString());
    }

    protected void writeUnknownField(String fieldName, Object data) throws IOException {
        _checkUnknown(data);
        writeStringField(fieldName, data.toString());
    }

    protected void _checkUnknown(Object value) throws IOException
    {
        if (JSON.Feature.FAIL_ON_UNKNOWN_TYPE_WRITE.isEnabled(_features)) {
            throw new JSONObjectException("Unrecognized type ("+value.getClass().getName()
                    +"), don't know how to write (disable "+JSON.Feature.FAIL_ON_UNKNOWN_TYPE_WRITE
                    +" to avoid exception)");
        }
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

    /**
     * @since 2.7
     */
    protected String dateToString(Date v) {
        if (v == null) {
            return "";
        }
        // !!! 01-Dec-2015, tatu: Should really use proper DateFormat or something
        //   since this relies on system-wide defaults, and hard/impossible to
        //   change easily
        return v.toString();
    }

    /*
    /**********************************************************************
    /* Other internal methods
    /**********************************************************************
     */
    
    private void _badType(int type, Object value)
    {
        if (type < 0) {
            throw new IllegalStateException(String.format(
                    "Internal error: missing BeanDefinition for id %d (class %s)",
                    type, value.getClass().getName()));
        }
        throw new IllegalStateException(String.format(
                "Unsupported type: %s (%s)", type, value.getClass().getName()));
    }
}
