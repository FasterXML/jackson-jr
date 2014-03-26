package com.fasterxml.jackson.jr.ob.impl;

import static com.fasterxml.jackson.jr.ob.impl.TypeDetector.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * {@link ValueReader} used for simple scalar types and related.
 */
public class SimpleValueReader extends ValueReader
{
    protected final int _typeId;
    protected final Class<?> _rawType;

    public SimpleValueReader(int typeId, Class<?> raw) {
        _typeId = typeId;
        _rawType = raw;
    }

    @Override
    public Object read(JSONReader reader, JsonParser parser) throws IOException
    {
        switch (_typeId) {

        case SER_MAP:
        case SER_LIST:
        case SER_COLLECTION:
        case SER_OBJECT_ARRAY:
            // should never get here: we have dedicated readers
            break;

        case SER_INT_ARRAY:
            return _readIntArray(parser);

        case SER_TREE_NODE:
            return reader._treeCodec().readTree(parser);

        // Textual types, related:
        case SER_STRING:
        case SER_CHARACTER_SEQUENCE:
            return parser.getValueAsString();
        case SER_CHAR_ARRAY:
            return parser.getValueAsString().toCharArray();
        case SER_BYTE_ARRAY:
            return _readBinary(parser);

        // Number types:
            
        case SER_NUMBER_FLOAT: // fall through
            return Float.valueOf((float) parser.getValueAsDouble());
        case SER_NUMBER_DOUBLE:
            return parser.getValueAsDouble();

        case SER_NUMBER_BYTE: // fall through
            return (byte) parser.getValueAsInt();
            
        case SER_NUMBER_SHORT: // fall through
            return (short) parser.getValueAsInt();
        case SER_NUMBER_INTEGER:
            return parser.getValueAsInt();
        case SER_NUMBER_LONG:
            return parser.getValueAsLong();

        case SER_NUMBER_BIG_DECIMAL:
            return parser.getDecimalValue();

        case SER_NUMBER_BIG_INTEGER:
            return parser.getBigIntegerValue();

        // Other scalar types:

        case SER_BOOLEAN:
            return parser.getValueAsBoolean();
            
        case SER_CHAR:
            {
                String str = parser.getValueAsString();
                return (str == null || str.isEmpty()) ? ' ' : str.charAt(0);
            }
            
        case SER_CALENDAR:
            {
                long l = _fetchLong(parser);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(l);
                return cal;
            }

        case SER_DATE:
            return new Date(_fetchLong(parser));

        case SER_CLASS:
        {
            String v = parser.getValueAsString();
            try {
                return Class.forName(v);
            } catch (Exception e) {
                throw new JSONObjectException("Failed to bind java.lang.Class from value '"+v+"'");
            }
        }
        case SER_FILE:
            return new File(parser.getValueAsString());
        case SER_UUID:
            return UUID.fromString(parser.getValueAsString());
        case SER_URL:
            return new URL(parser.getValueAsString());
        case SER_URI:
        
            return URI.create(parser.getValueAsString());

        default: // types that shouldn't get here
        //case SER_ENUM:
        }
        
        throw JSONObjectException.from(parser,
                "Can not create a "+_rawType.getName()+" instance out of "+_tokenDesc(parser));
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
                +_rawType.getName()+") from "+_tokenDesc(p, t));
    }
}
