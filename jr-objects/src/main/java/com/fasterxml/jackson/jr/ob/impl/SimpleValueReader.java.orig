package com.fasterxml.jackson.jr.ob.impl;

import static com.fasterxml.jackson.jr.ob.impl.ValueWriterLocator.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

/**
 * Default {@link ValueReader} used for simple scalar types and related,
 * not including POJO-, {@link java.util.Map} and {@link java.util.Collection}
 * types.
 */
public class SimpleValueReader extends ValueReader
{
    protected final int _typeId;

    public SimpleValueReader(Class<?> raw, int typeId) {
        super(raw);
        _typeId = typeId;
    }
    
    @Override
    public Object readNext(JSONReader reader, JsonParser p) throws IOException
    {
        // NOTE: only cases where we can optimize
        switch (_typeId) {
        // Textual types, related:
        case SER_STRING:
        case SER_CHARACTER_SEQUENCE:
            return _nextString(p);

        case SER_CHAR_ARRAY:
            String str = _nextString(p);
            return (str == null) ? null : str.toCharArray();

        // Number types:

        case SER_NUMBER_SHORT: // fall through
            return Short.valueOf((short) _nextInt(p));

        case SER_NUMBER_INTEGER:
            return Integer.valueOf(_nextInt(p));

        case SER_NUMBER_LONG:
            return Long.valueOf(_nextLong(p));

        // Other scalar types:

        case SER_BOOLEAN:
            {
                Boolean b = p.nextBooleanValue();
                if (b != null) {
                    return b;
                }
                return p.getValueAsBoolean();
            }
        }

        p.nextToken();
        return read(reader, p);
    }    
    
    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException
    {
        switch (_typeId) {

        case SER_MAP:
        case SER_LIST:
        case SER_COLLECTION:
        case SER_OBJECT_ARRAY:
            // should never get here: we have dedicated readers
            break;

        case SER_INT_ARRAY:
            return _readIntArray(p);

        case SER_TREE_NODE:
            return reader._treeCodec().readTree(p);

        // Textual types, related:
        case SER_STRING:
        case SER_CHARACTER_SEQUENCE:
            return p.getValueAsString();
        case SER_CHAR_ARRAY:
            return p.getValueAsString().toCharArray();
        case SER_BYTE_ARRAY:
            return _readBinary(p);

        // Number types:
            
        case SER_NUMBER_FLOAT: // fall through
            return Float.valueOf((float) p.getValueAsDouble());
        case SER_NUMBER_DOUBLE:
            return p.getValueAsDouble();

        case SER_NUMBER_BYTE: // fall through
            return (byte) p.getValueAsInt();
            
        case SER_NUMBER_SHORT: // fall through
            return (short) p.getValueAsInt();
        case SER_NUMBER_INTEGER:
            return p.getValueAsInt();
        case SER_NUMBER_LONG:
            return p.getValueAsLong();

        case SER_NUMBER_BIG_DECIMAL:
            return p.getDecimalValue();

        case SER_NUMBER_BIG_INTEGER:
            return p.getBigIntegerValue();

        // Other scalar types:

        case SER_BOOLEAN:
            return p.getValueAsBoolean();
            
        case SER_CHAR:
            {
                String str = p.getValueAsString();
                return (str == null || str.isEmpty()) ? ' ' : str.charAt(0);
            }
            
        case SER_CALENDAR:
            {
                long l = _fetchLong(p);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(l);
                return cal;
            }

        case SER_DATE:
            return new Date(_fetchLong(p));

        case SER_CLASS:
        {
            String v = p.getValueAsString();
            try {
                return Class.forName(v);
            } catch (Exception e) {
                throw new JSONObjectException("Failed to bind java.lang.Class from value '"+v+"'");
            }
        }
        case SER_FILE:
            return new File(p.getValueAsString());
        case SER_UUID:
            return UUID.fromString(p.getValueAsString());
        case SER_URL:
            return new URL(p.getValueAsString());
        case SER_URI:
        
            return URI.create(p.getValueAsString());

        default: // types that shouldn't get here
        //case SER_ENUM:
        }
        
        throw JSONObjectException.from(p,
                "Can not create a "+_valueType.getName()+" instance out of "+_tokenDesc(p));
    }    

    /*
    /**********************************************************************
    /* Read methods for scalars
    /**********************************************************************
     */

    protected byte[] _readBinary(JsonParser p) throws IOException {
        return p.getBinaryValue();
    }
    
    protected int[] _readIntArray(JsonParser p) throws IOException
    {
        // !!! TODO
        throw new JSONObjectException("Reading of int[] not yet implemented");
    }

    protected long _fetchLong(JsonParser p) throws IOException
    {
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return p.getLongValue();
        }
        throw JSONObjectException.from(p, "Can not get long numeric value from JSON (to construct "
                +_valueType.getName()+") from "+_tokenDesc(p, t));
    }

    private final String _nextString(JsonParser p) throws IOException {
        String str = p.nextTextValue();
        return (str == null) ? p.getValueAsString() : str;
    }

    private final int _nextInt(JsonParser p) throws IOException {
        int i = p.nextIntValue(-2);
        if (i != -2) {
            return i;
        }
        return p.getValueAsInt();
    }

    private final long _nextLong(JsonParser p) throws IOException {
        long l = p.nextLongValue(-2L);
        if (l != -2L) {
            return l;
        }
        return p.getValueAsLong();
    }
}
