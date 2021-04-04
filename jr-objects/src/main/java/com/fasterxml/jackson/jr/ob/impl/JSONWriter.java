package com.fasterxml.jackson.jr.ob.impl;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.api.ValueWriter;

import static com.fasterxml.jackson.jr.ob.impl.ValueWriterLocator.*;

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

    /*
    /**********************************************************************
    /* Instance config
    /**********************************************************************
     */

    protected final TreeCodec _treeCodec;

    /**
     * Object that is used to dynamically find Bean (and custom type) value writers
     */
    protected final ValueWriterLocator _writerLocator;

    protected final JsonGenerator _generator;

    protected final TimeZone _timezone;

    protected final int _features;

    protected final boolean _writeNullValues;
    
    /*
    /**********************************************************************
    /* Blueprint construction
    /**********************************************************************
     */

    /**
     * Constructor used for creating differently configured blueprint
     * instances
     */
    public JSONWriter()
    {
        _features = 0;
        _writeNullValues = false;
        _writerLocator = null;
        _treeCodec = null;
        _generator = null;
        _timezone = DEFAULT_TIMEZONE;
    }

    /**
     * Constructor for non-blueprint instances.
     *
     * @param base Blueprint instance to base settings of the new instance on
     * @param features Active features for the write operation
     * @param loc Helper object to use for dynamically located value writers
     * @param tc TreeCodec to use for writing tree values, if any
     * @param g Underlying streaming encoder to use
     */
    protected JSONWriter(JSONWriter base, int features,
            ValueWriterLocator loc, TreeCodec tc,
            JsonGenerator g)
    {
        _features = features;
        _writeNullValues = JSON.Feature.WRITE_NULL_PROPERTIES.isEnabled(features);
        _treeCodec = tc;
        _writerLocator = loc.perOperationInstance(this, features);
        _generator = g;
        _timezone = DEFAULT_TIMEZONE;
    }

    /*
    /**********************************************************************
    /* Mutant factories for blueprint
    /**********************************************************************
     */

    public JSONWriter withCacheCheck(int features) {
//        int diff = (features ^ _features);

        return this;
    }

    /*
    /**********************************************************************
    /* New instance creation
    /**********************************************************************
     */

    public JSONWriter perOperationInstance(int features,
            ValueWriterLocator loc, TreeCodec tc,
            JsonGenerator g)
    {
        if (getClass() != JSONWriter.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override perOperationInstance(...)");
        }
        return new JSONWriter(this, features, loc, tc, g);
    }

    /*
    /**********************************************************************
    /* Simple accessors
    /**********************************************************************
     */

    public boolean isEnabled(JSON.Feature f) {
        return f.isEnabled(_features);
    }

    /*
    /**********************************************************************
    /* Public write methods
    /**********************************************************************
     */

    /**
     * Main entry point for non-blueprint instances: called for the root value to
     * write it out.
     */
    public void writeValue(Object value) throws JacksonException
    {
        if (value == null) {
            writeNullValue();
            return;
        }
        _writeValue(value, _writerLocator.findSerializationType(value.getClass()));
    }

    public void writeProperty(String propName, Object value, int type) throws JacksonException
    {
        switch (type) {
        case SER_NULL:
            if (_writeNullValues) {
                writeNullProperty(propName);
            }
            return;
        // Structured types:
        case SER_MAP:
            writeMapProperty(propName, (Map<?,?>) value);
            return;
        case SER_LIST:
            writeListProperty(propName, (List<?>) value);
            return;
        case SER_COLLECTION:
            writeCollectionProperty(propName, (Collection<?>) value);
            return;
        case SER_OBJECT_ARRAY:
            writeObjectArrayProperty(propName, (Object[]) value);
            return;
        case SER_INT_ARRAY:
            writeIntArrayProperty(propName, (int[]) value);
            return;
        case SER_LONG_ARRAY:
            writeLongArrayProperty(propName, (long[]) value);
            return;
        case SER_BOOLEAN_ARRAY:
            writeBooleanArrayProperty(propName, (boolean[]) value);
            return;
        case SER_TREE_NODE:
            writeTreeNodeProperty(propName, (TreeNode) value);
            return;

        // Textual types, similar:

        case SER_STRING:
            writeStringProperty(propName, (String) value);
            return;
        case SER_CHAR_ARRAY:
            writeStringProperty(propName, new String((char[]) value));
            return;
        case SER_CHARACTER_SEQUENCE:
            writeStringProperty(propName, ((CharSequence) value).toString());
            return;
        case SER_BYTE_ARRAY:
            writeBinaryProperty(propName, (byte[]) value);
            return;

        // Number types:
        case SER_NUMBER_BIG_DECIMAL:
            writeBigDecimalProperty(propName, (BigDecimal) value);
            return;
        case SER_NUMBER_BIG_INTEGER:
            writeBigIntegerProperty(propName, (BigInteger) value);
            return;
        case SER_NUMBER_FLOAT: // fall through
        case SER_NUMBER_DOUBLE:
            writeDoubleProperty(propName, ((Number) value).doubleValue());
            return;
        case SER_NUMBER_BYTE: // fall through
        case SER_NUMBER_SHORT: // fall through
        case SER_NUMBER_INTEGER:
            writeIntProperty(propName, ((Number) value).intValue());
            return;
        case SER_NUMBER_LONG:
            writeLongProperty(propName, ((Number) value).longValue());
            return;

        // Scalar types:

        case SER_BOOLEAN:
            writeBooleanProperty(propName, ((Boolean) value).booleanValue());
            return;
        case SER_CHAR:
            writeStringProperty(propName, String.valueOf(value));
            return;
        case SER_CALENDAR:
            writeDateProperty(propName, ((Calendar) value).getTime());
            return;
        case SER_DATE:
            writeDateProperty(propName, (Date) value);
            return;
        case SER_ENUM:
            writeEnumProperty(propName, (Enum<?>) value);
            return;
        case SER_CLASS:
            writeStringLikeProperty(propName, ((Class<?>) value).getName(), type);
            return;
        case SER_FILE:
            writeStringLikeProperty(propName, ((File) value).getAbsolutePath(), type);
            return;
        case SER_UUID:
        case SER_URL:
        case SER_URI:
            writeStringLikeProperty(propName, value.toString(), type);
            return;

        // Others
            
        case SER_ITERABLE:
            writeIterableProperty(propName, (Iterable<?>) value);
            return;

        case SER_UNKNOWN:
            writeUnknownProperty(propName, value);
            return;
        }

        if (type < 0) { // Bean type!
            ValueWriter writer = _writerLocator.getValueWriter(type);
            if (writer != null) { // sanity check
                _generator.writeName(propName);
                writer.writeValue(this, _generator, value);
                return;
            }
        }
        _badType(type, value);
    }

    protected void _writeValue(Object value, int type) throws JacksonException
    {
        switch (type) {
        case SER_NULL:
            writeNullValue();
            return;

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

        if (type < 0) { // explicit ValueWriter
            ValueWriter writer = _writerLocator.getValueWriter(type);
            if (writer != null) { // sanity check
                writer.writeValue(this, _generator, value);
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
    
    protected void writeCollectionValue(Collection<?> v) throws JacksonException
    {
        _generator.writeStartArray();
        for (Object ob : v) {
            writeValue(ob);
        }
        _generator.writeEndArray();
    }

    protected void writeCollectionProperty(String propName, Collection<?> v) throws JacksonException
    {
        _generator.writeName(propName);
        writeCollectionValue(v);
    }
    
    protected void writeIterableValue(Iterable<?> v) throws JacksonException
    {
        _generator.writeStartArray();
        for (Object ob : v) {
            writeValue(ob);
        }
        _generator.writeEndArray();
    }

    protected void writeIterableProperty(String propName, Iterable<?> v) throws JacksonException
    {
        _generator.writeName(propName);
        writeIterableValue(v);
    }
    
    protected void writeListValue(List<?> list) throws JacksonException
    {
        final int len = list.size();
        _generator.writeStartArray(list, len);
        for (int i = 0; i < len; ++i) {
            Object value = list.get(i);
            if (value == null) {
                _generator.writeNull();
                continue;
            }
            _writeValue(value, _writerLocator.findSerializationType(value.getClass()));
        }
        _generator.writeEndArray();
    }

    protected void writeListProperty(String propName, List<?> v) throws JacksonException
    {
        _generator.writeName(propName);
        writeListValue(v);
    }
    
    protected void writeMapValue(Map<?,?> v) throws JacksonException
    {
        _generator.writeStartObject(v);
        if (!v.isEmpty()) {
            for (Map.Entry<?,?> entry : v.entrySet()) {
                String key = keyToString(entry.getKey());
                Object value = entry.getValue();
                int type;
                if (value == null) {
                    type = SER_NULL;
                } else {
                    Class<?> cls = value.getClass();
                    type = _writerLocator.findSerializationType(cls);
                }
                writeProperty(key, value, type);
            }
        }
        _generator.writeEndObject();
    }

    protected void writeMapProperty(String propName, Map<?,?> v) throws JacksonException
    {
        _generator.writeName(propName);
        writeMapValue(v);
    }

    protected void writeObjectArrayValue(Object[] v) throws JacksonException {
        final int len = v.length;
        _generator.writeStartArray(v, len);
        int i = 0;
        int left = v.length;

        if (left > 3) {
            do {
                writeValue(v[i]);
                writeValue(v[i+1]);
                writeValue(v[i+2]);
                writeValue(v[i+3]);
                i += 4;
                left -= 4;
            } while (left > 3);
        }
        switch (left) {
        case 3:
            writeValue(v[i++]);
        case 2:
            writeValue(v[i++]);
        case 1:
            writeValue(v[i++]);
        }
        _generator.writeEndArray();
    }

    protected void writeObjectArrayProperty(String propName, Object[] v) throws JacksonException {
        _generator.writeName(propName);
        writeObjectArrayValue(v);
    }

    protected void writeIntArrayValue(int[] v) throws JacksonException {
        final int len = v.length;
        _generator.writeStartArray(v, len);
        for (int i = 0; i < len; ++i) {
            _generator.writeNumber(v[i]);
        }
        _generator.writeEndArray();
    }

    protected void writeIntArrayProperty(String propName, int[] v) throws JacksonException {
        _generator.writeName(propName);
        writeIntArrayValue(v);
    }
    
    protected void writeLongArrayValue(long[] v) throws JacksonException {
        final int len = v.length;
        _generator.writeStartArray(v, len);
        for (int i = 0; i < len; ++i) {
            _generator.writeNumber(v[i]);
        }
        _generator.writeEndArray();
    }

    protected void writeLongArrayProperty(String propName, long[] v) throws JacksonException {
        _generator.writeName(propName);
        writeLongArrayValue(v);
    }
    
    protected void writeBooleanArrayValue(boolean[] v) throws JacksonException {
        final int len = v.length;
        _generator.writeStartArray(v, len);
        for (int i = 0; i < len; ++i) {
            _generator.writeBoolean(v[i]);
        }
        _generator.writeEndArray();
    }

    protected void writeBooleanArrayProperty(String propName, boolean[] v) throws JacksonException {
        _generator.writeName(propName);
        writeBooleanArrayValue(v);
    }

    protected void writeTreeNodeValue(TreeNode v) throws JacksonException {
        if (_treeCodec == null) {
            throw new JSONObjectException("No `TreeCodec` configured: can not serialize `TreeNode` values");
        }
        _treeCodec.writeTree(_generator, v);
    }

    protected void writeTreeNodeProperty(String propName, TreeNode v) throws JacksonException
    {
        _generator.writeName(propName);
        writeTreeNodeValue(v);
    }
    
    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, primitives
    /**********************************************************************
     */

    protected void writeBooleanValue(boolean v) throws JacksonException {
        _generator.writeBoolean(v);
    }

    protected void writeBooleanProperty(String propName, boolean v) throws JacksonException {
        _generator.writeBooleanProperty(propName, v);
    }

    protected void writeIntValue(int v) throws JacksonException {
        _generator.writeNumber(v);
    }

    protected void writeIntProperty(String propName, int v) throws JacksonException {
        _generator.writeNumberProperty(propName, v);
    }

    protected void writeLongValue(long v) throws JacksonException {
        _generator.writeNumber(v);
    }

    protected void writeBigIntegerValue(BigInteger v) throws JacksonException {
        _generator.writeNumber(v);
    }

    protected void writeBigIntegerProperty(String propName, BigInteger v) throws JacksonException {
        _generator.writeName(propName);
        writeBigIntegerValue(v);
    }
    
    protected void writeLongProperty(String propName, long v) throws JacksonException {
        _generator.writeNumberProperty(propName, v);
    }
    
    protected void writeDoubleValue(double v) throws JacksonException {
        _generator.writeNumber(v);
    }

    protected void writeDoubleProperty(String propName, double v) throws JacksonException {
        _generator.writeNumberProperty(propName, v);
    }

    protected void writeBigDecimalValue(BigDecimal v) throws JacksonException {
        _generator.writeNumber(v);
    }

    protected void writeBigDecimalProperty(String propName, BigDecimal v) throws JacksonException {
        _generator.writeNumberProperty(propName, v);
    }

    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, textual
    /**********************************************************************
     */

    protected void writeStringValue(String v) throws JacksonException {
        _generator.writeString(v);
    }

    protected void writeStringProperty(String propName, String v) throws JacksonException {
        _generator.writeStringProperty(propName, v);
    }

    protected void writeStringLikeValue(String v, int actualType) throws JacksonException {
        _generator.writeString(v);
    }

    protected void writeStringLikeProperty(String propName, String v, int actualType) throws JacksonException {
        _generator.writeStringProperty(propName, v);
    }
    
    protected void writeBinaryValue(byte[] data) throws JacksonException {
        _generator.writeBinary(data);
    }

    protected void writeBinaryProperty(String propName, byte[] data) throws JacksonException {
        _generator.writeBinaryProperty(propName, data);
    }

    /*
    /**********************************************************************
    /* Overridable concrete typed write methods, other
    /**********************************************************************
     */

    protected void writeNullValue() throws JacksonException {
        _generator.writeNull();
    }

    protected void writeNullProperty(String propName) throws JacksonException {
        if (_writeNullValues) {
            _generator.writeNullProperty(propName);
        }
    }

    protected void writeNullProperty(SerializedString propName) throws JacksonException {
        if (_writeNullValues) {
            _generator.writeName(propName);
            _generator.writeNull();
        }
    }

    protected void writeDateValue(Date v) throws JacksonException {
        if (JSON.Feature.WRITE_DATES_AS_TIMESTAMP.isEnabled(_features)) {
            writeLongValue(v.getTime());
        } else {
            writeStringValue(dateToString(v));
        }
    }

    protected void writeDateProperty(String propName, Date v) throws JacksonException {
        if (JSON.Feature.WRITE_DATES_AS_TIMESTAMP.isEnabled(_features)) {
            writeLongProperty(propName, v.getTime());
        } else {
            writeStringProperty(propName, dateToString(v));
        }
    }

    protected void writeEnumValue(Enum<?> v) throws JacksonException {
        if (JSON.Feature.WRITE_ENUMS_USING_INDEX.isEnabled(_features)) {
            writeIntValue(v.ordinal());
        } else {
            writeStringValue(v.toString());
        }
    }

    protected void writeEnumProperty(String propName, Enum<?> v) throws JacksonException {
        if (JSON.Feature.WRITE_ENUMS_USING_INDEX.isEnabled(_features)) {
            writeIntProperty(propName, v.ordinal());
        } else {
            writeStringProperty(propName, v.toString());
        }
    }

    public void writeBeanValue(BeanPropertyWriter[] props, Object bean) throws JacksonException
    {
        _generator.writeStartObject(bean);
        int i = 0;
        int left = props.length;

        if (left > 3) {
            int typeId;
            do {
                BeanPropertyWriter property = props[i];
                Object value = property.getValueFor(bean);
                if (value == null) {
                    if (_writeNullValues) {
                        writeNullProperty(property.name);
                    }
                } else {
                    typeId = property.typeId;
                    if (typeId == 0) {
                        typeId = _writerLocator.findSerializationType(value.getClass());
                    }
                    _generator.writeName(property.name);
                    _writeValue(value, typeId);
                }

                property = props[i+1];
                value = property.getValueFor(bean);
                if (value == null) {
                    if (_writeNullValues) {
                        writeNullProperty(property.name);
                    }
                } else {
                    typeId = property.typeId;
                    if (typeId == 0) {
                        typeId = _writerLocator.findSerializationType(value.getClass());
                    }
                    _generator.writeName(property.name);
                    _writeValue(value, typeId);
                }

                property = props[i+2];
                value = property.getValueFor(bean);
                if (value == null) {
                    if (_writeNullValues) {
                        writeNullProperty(property.name);
                    }
                } else {
                    typeId = property.typeId;
                    if (typeId == 0) {
                        typeId = _writerLocator.findSerializationType(value.getClass());
                    }
                    _generator.writeName(property.name);
                    _writeValue(value, typeId);
                }

                property = props[i+3];
                value = property.getValueFor(bean);
                if (value == null) {
                    if (_writeNullValues) {
                        writeNullProperty(property.name);
                    }
                } else {
                    typeId = property.typeId;
                    if (typeId == 0) {
                        typeId = _writerLocator.findSerializationType(value.getClass());
                    }
                    _generator.writeName(property.name);
                    _writeValue(value, typeId);
                }
                left -= 4;
                i += 4;
            } while (left > 3);
        }
        BeanPropertyWriter property;
        Object value;
        int typeId;
        switch (left) {
        case 3:
            property = props[i++];
            value = property.getValueFor(bean);
            if (value == null) {
                if (_writeNullValues) {
                    writeNullProperty(property.name);
                }
            } else {
                typeId = property.typeId;
                if (typeId == 0) {
                    typeId = _writerLocator.findSerializationType(value.getClass());
                }
                _generator.writeName(property.name);
                _writeValue(value, typeId);
            }
        case 2:
            property = props[i++];
            value = property.getValueFor(bean);
            if (value == null) {
                if (_writeNullValues) {
                    writeNullProperty(property.name);
                }
            } else {
                typeId = property.typeId;
                if (typeId == 0) {
                    typeId = _writerLocator.findSerializationType(value.getClass());
                }
                _generator.writeName(property.name);
                _writeValue(value, typeId);
            }
        case 1:
            property = props[i++];
            value = property.getValueFor(bean);
            if (value == null) {
                if (_writeNullValues) {
                    writeNullProperty(property.name);
                }
            } else {
                typeId = property.typeId;
                if (typeId == 0) {
                    typeId = _writerLocator.findSerializationType(value.getClass());
                }
                _generator.writeName(property.name);
                _writeValue(value, typeId);
            }
        }
        _generator.writeEndObject();
    }

    protected void writeUnknownValue(Object data) throws JacksonException {
        _checkUnknown(data);
        writeStringValue(data.toString());
    }

    protected void writeUnknownProperty(String propName, Object data) throws JacksonException {
        _checkUnknown(data);
        writeStringProperty(propName, data.toString());
    }

    protected void _checkUnknown(Object value) throws JacksonException
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
        String typeDesc = (value == null) ? "NULL" : value.getClass().getName();
        throw new IllegalStateException(String.format("Unsupported type: %s (%s)", type, typeDesc));
    }
}
