package com.fasterxml.jackson.jr.ob;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.jr.ob.JSON.Feature;
import com.fasterxml.jackson.jr.ob.impl.BeanDefinition;
import com.fasterxml.jackson.jr.ob.impl.BeanProperty;
import com.fasterxml.jackson.jr.ob.impl.TypeDetector;

import static com.fasterxml.jackson.jr.ob.impl.TypeDetector.*;

/**
 * Object that handles serialization of simple Objects into
 * JSON.
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
    /*
    /**********************************************************************
    /* Blueprint config
    /**********************************************************************
     */

    protected final int _features;
    
    /**
     * Object that is used to resolve types of values dynamically.
     */
    protected final TypeDetector _typeDetector;

    protected final TreeCodec _treeCodec;

    protected final boolean _requireSetter;
    
    /*
    /**********************************************************************
    /* Instance config
    /**********************************************************************
     */

    protected final JsonGenerator _generator;
    
    /*
    /**********************************************************************
    /* Blueprint construction
    /**********************************************************************
     */

    /**
     * Constructor used for creating differently configured blueprint
     * instances
     */
    protected JSONWriter(int features, TypeDetector td, TreeCodec tc)
    {
        _features = features;
        _typeDetector = td;
        _treeCodec = tc;
        _generator = null;
        _requireSetter = JSON.Feature.WRITE_READONLY_BEAN_PROPERTIES.isDisabled(features);
    }

    /**
     * Constructor for non-blueprint instances
     */
    protected JSONWriter(JSONWriter base, JsonGenerator jgen)
    {
        _features = base._features;
        _typeDetector = base._typeDetector.perOperationInstance(_features);
        _treeCodec = base._treeCodec;
        _generator = jgen;
        _requireSetter = JSON.Feature.WRITE_READONLY_BEAN_PROPERTIES.isDisabled(_features);
    }

    /*
    /**********************************************************************
    /* Mutant factories for blueprint
    /**********************************************************************
     */

    public final JSONWriter withFeatures(int features) {
        if (_features == features) {
            return this;
        }
        return _with(features, _typeDetector, _treeCodec);
    }

    public final JSONWriter with(TypeDetector td) {
        if (_typeDetector == td) {
            return this;
        }
        return _with(_features, td, _treeCodec);
    }

    public final JSONWriter with(TreeCodec tc) {
        if (_treeCodec == tc) {
            return this;
        }
        return _with(_features, _typeDetector, tc);
    }

    /**
     * Overridable method that all mutant factories call if a new instance
     * is to be constructed
     */
    protected JSONWriter _with(int features, TypeDetector td, TreeCodec tc)
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

    public JSONWriter perOperationInstance(JsonGenerator jg)
    {
        if (getClass() != JSONWriter.class) { // sanity check
            throw new IllegalStateException("Sub-classes MUST override perOperationInstance(...)");
        }
        return new JSONWriter(this, jg);
    }
    
    /*
    /**********************************************************************
    /* Public entry methods
    /**********************************************************************
     */

    public final void writeValue(Object value) throws IOException, JsonProcessingException
    {
        if (value == null) {
            writeNullValue();
            return;
        }
        _writeValue(value, _typeDetector.findFullSerializationType(value.getClass()));
    }

    public void writeField(String fieldName, Object value) throws IOException, JsonProcessingException
    {
        if (value == null) {
            if (Feature.WRITE_NULL_PROPERTIES.isEnabled(_features)) {
                writeNullField(fieldName);
            }
            return;
        }

        int type = _typeDetector.findFullSerializationType(value.getClass());
        switch (type) {

        // Textual types, similar:

        case SER_STRING:
            writeStringField(fieldName, (String) value);
            return;
        case SER_CHAR_ARRAY:
            writeStringField(fieldName, new String((char[]) value));
            return;
        case SER_CHAR:
            writeStringField(fieldName, String.valueOf(value));
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
        case SER_NUMBER_OTHER:
            writeNumberField(fieldName, (Number) value);
            return;

        // Scalar types:

        case SER_BOOLEAN:
            writeBooleanField(fieldName, ((Boolean) value).booleanValue());
            return;
        case SER_DATE:
            writeDateField(fieldName, (Date) value);
            return;
        case SER_ENUM:
            writeEnumField(fieldName, (Enum<?>) value);
            return;
            
        // Structured types:

        case SER_COLLECTION:
            writeCollectionField(fieldName, (Collection<?>) value);
            return;
        case SER_ITERABLE:
            writeIterableField(fieldName, (Iterable<?>) value);
            return;
        case SER_LIST:
            writeListField(fieldName, (List<?>) value);
            return;
        case SER_MAP:
            writeMapField(fieldName, (Map<?,?>) value);
            return;
        case SER_OBJECT_ARRAY:
            writeObjectArrayField(fieldName, (Object[]) value);
            return;
        case SER_INT_ARRAY:
            writeIntArrayField(fieldName, (int[]) value);
            return;
        case SER_TREE_NODE:
            writeTreeNodeField(fieldName, (TreeNode) value);
            return;

        case SER_UNKNOWN:
            writeUnknownField(fieldName, value);
            return;
        }

        if (type < 0) { // Bean type!
            BeanDefinition def = _typeDetector.getBeanDefinition(type);
            if (def == null) { // sanity check
                throw new IllegalStateException("Internal error: missing BeanDefinition for id "+type
                        +" (class "+value.getClass().getName()+")");
            }
            _generator.writeFieldName(fieldName);
            writeBeanValue(def, value);
        }
        
        throw new IllegalStateException("Unsupported type: "+type);
    }

    protected void _writeValue(Object value, int type) throws IOException
    {
        switch (type) {

        // Textual types, similar:

        case SER_STRING:
            writeStringValue((String) value);
            return;
        case SER_STRING_LIKE:
            writeStringValue(value.toString());
            return;
        case SER_CHAR_ARRAY:
            writeStringValue(new String((char[]) value));
            return;
        case SER_CHAR:
            writeStringValue(String.valueOf(value));
            return;
        case SER_CHARACTER_SEQUENCE:
            writeStringValue(((CharSequence) value).toString());
            return;
        case SER_BYTE_ARRAY:
            writeBinaryValue((byte[]) value);
            return;
        case SER_INT_ARRAY:
            writeIntArrayValue((int[]) value);
            return;

            // Number types:
        
        case SER_NUMBER_BIG_DECIMAL:
            writeBigDecimalValue((BigDecimal) value);
            return;
        case SER_NUMBER_BIG_INTEGER:
            writeBigIntegerValue((BigInteger) value);
            return;
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
        case SER_NUMBER_OTHER:
            writeNumberValue((Number) value);
            return;

        // Scalar types:

        case SER_BOOLEAN:
            writeBooleanValue(((Boolean) value).booleanValue());
            return;
        case SER_DATE:
            writeDateValue((Date) value);
            return;
        case SER_ENUM:
            writeEnumValue((Enum<?>) value);
            return;
            
        // Structured types:

        case SER_COLLECTION:
            writeCollectionValue((Collection<?>) value);
            return;
        case SER_ITERABLE:
            writeIterableValue((Iterable<?>) value);
            return;
        case SER_LIST:
            writeListValue((List<?>) value);
            return;
        case SER_MAP:
            writeMapValue((Map<?,?>) value);
            return;
        case SER_OBJECT_ARRAY:
            writeObjectArrayValue((Object[]) value);
            return;
        case SER_TREE_NODE:
            writeTreeNodeValue((TreeNode) value);
            return;
        case SER_UNKNOWN:
            writeUnknownValue(value);
            return;
        }
        
        if (type < 0) { // Bean type!
            BeanDefinition def = _typeDetector.getBeanDefinition(type);
            if (def == null) { // sanity check
                throw new IllegalStateException("Internal error: missing BeanDefinition for id "+type
                        +" (class "+value.getClass().getName()+")");
            }
            writeBeanValue(def, value);
            return;
        }
        throw new IllegalStateException("Unsupported type: "+type+" (class "+value.getClass().getName()+")");
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
    
    protected void writeListValue(List<?> v) throws IOException
    {
        _generator.writeStartArray();
        for (int i = 0, len = v.size(); i < len; ++i) {
            writeValue(v.get(i));
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
                writeField(keyToString(entry.getKey()), entry.getValue());
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

    protected void writeObjectArrayField(String fieldName, Object[] v) throws IOException
    {
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

    protected void writeIntArrayField(String fieldName, int[] v) throws IOException
    {
        _generator.writeFieldName(fieldName);
        writeIntArrayValue(v);
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

    protected void writeNumberValue(Number v) throws IOException {
        // Unknown type; must use fallback method
        _generator.writeNumber(v.toString());
    }

    protected void writeNumberField(String fieldName, Number v) throws IOException {
        _generator.writeFieldName(fieldName);
        writeNumberValue(v);
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
        if (Feature.WRITE_NULL_PROPERTIES.isEnabled(_features)) {
            _generator.writeNullField(fieldName);
        }
    }

    protected void writeNullField(SerializedString fieldName) throws IOException {
        if (Feature.WRITE_NULL_PROPERTIES.isEnabled(_features)) {
            _generator.writeFieldName(fieldName);
            _generator.writeNull();
        }
    }
    
    protected void writeDateValue(Date v) throws IOException {
        // TODO: maybe allow serialization using timestamp?
        writeStringValue(v.toString());
    }

    protected void writeDateField(String fieldName, Date v) throws IOException {
        writeStringField(fieldName, v.toString());
    }

    protected void writeEnumValue(Enum<?> v) throws IOException {
        // TODO: maybe allow serialization using index?
        writeStringValue(v.toString());
    }

    protected void writeEnumField(String fieldName, Enum<?> v) throws IOException {
        writeStringField(fieldName, v.toString());
    }

    protected void writeBeanValue(BeanDefinition beanDef, Object bean) throws IOException
    {
        _generator.writeStartObject();
        for (BeanProperty property : beanDef.properties()) {
            SerializedString name;
            
            if (_requireSetter) {
                name =  property.getNameIfHasSetter();
                if (name == null) {
                    continue;
                }
            } else {
                name = property.getName();
            }
            Object value = property.getValueFor(bean);
            if (value == null) {
                writeNullField(name);
                continue;
            }
            int typeId = property.getWriteTypeId();
            if (typeId == 0) {
                typeId = _typeDetector.findFullSerializationType(value.getClass());
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
        if (Feature.FAIL_ON_UNKNOWN_TYPE_WRITE.isEnabled(_features)) {
            throw new JSONObjectException("Unrecognized type ("+value.getClass().getName()
                    +"), don't know how to write (disable "+Feature.FAIL_ON_UNKNOWN_TYPE_WRITE
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
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
}
