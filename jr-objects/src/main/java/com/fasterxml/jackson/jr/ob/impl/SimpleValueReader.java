package com.fasterxml.jackson.jr.ob.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

import static com.fasterxml.jackson.jr.ob.impl.ValueWriterLocator.*;

/**
 * Default {@link ValueReader} used for simple scalar types and related,
 * not including POJO-, {@link java.util.Map} and {@link java.util.Collection}
 * types.
 */
public class SimpleValueReader extends ValueReader
{
    private final static int[] NO_INTS = new int[0];

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
            {
                String str = p.nextTextValue();
                if (str != null) {
                    return str;
                }
                return read(reader, p);
            }

        case SER_CHAR_ARRAY:
            {
                String str = p.nextTextValue();
                if (str != null) {
                    return str.toCharArray();
                }
                return read(reader, p);
            }

        // Number types:

        // Let's only optimize common ones, int/Integer, long/Long;
        // and only when `nextXxx()` correctly returns value. In all other
        // cases default to "standard" handling which does range checks etc

        case SER_NUMBER_INTEGER:
        case SER_NUMBER_INTEGER_WRAPPER:
            {
                int i = p.nextIntValue(-2);
                if (i != -2) {
                    return i;
                }
                return read(reader, p);
            }

        case SER_NUMBER_LONG:
        case SER_NUMBER_LONG_WRAPPER:
            {
                long l = p.nextLongValue(-2L);
                if (l != -2L) {
                    return l;
                }
                return read(reader, p);
            }

        // Other scalar types:

        case SER_BOOLEAN:
        case SER_BOOLEAN_WRAPPER:
            {
                Boolean b = p.nextBooleanValue();
                if (b != null) {
                    return b;
                }
                return read(reader, p);
            }
        }

        p.nextToken();
        return read(reader, p);
    }    
    
    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException
    {
        switch (_typeId) {

        case SER_INT_ARRAY:
            return _readIntArray(p);

        case SER_TREE_NODE:
            return reader.readTree();

        // Textual types, related:
        case SER_STRING:
        case SER_CHARACTER_SEQUENCE:
            return p.getValueAsString();
        case SER_CHAR_ARRAY:
            return p.getValueAsString().toCharArray();
        case SER_BYTE_ARRAY:
            return _readBinary(p);

        // Number types:
            
        case SER_NUMBER_FLOAT_WRAPPER:
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
        case SER_NUMBER_FLOAT:
            return Float.valueOf((float) p.getValueAsDouble());
        case SER_NUMBER_DOUBLE_WRAPPER:
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
        case SER_NUMBER_DOUBLE:
            return p.getValueAsDouble();

        case SER_NUMBER_BYTE:
            return (byte) p.getValueAsInt();
            
        case SER_NUMBER_SHORT:
            return (short) p.getValueAsInt();
        case SER_NUMBER_INTEGER_WRAPPER:
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
        case SER_NUMBER_INTEGER:
            return p.getValueAsInt();
        case SER_NUMBER_LONG_WRAPPER:
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
        case SER_NUMBER_LONG:
            return p.getValueAsLong();

        case SER_NUMBER_BIG_DECIMAL:
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            return p.getDecimalValue();

        case SER_NUMBER_BIG_INTEGER:
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            return p.getBigIntegerValue();

        // Other scalar types:

        case SER_BOOLEAN:
        case SER_BOOLEAN_WRAPPER:
            switch (p.currentTokenId()) {
            case JsonTokenId.ID_TRUE:
                return Boolean.TRUE;
            case JsonTokenId.ID_FALSE:
                return Boolean.FALSE;
            case JsonTokenId.ID_NULL:
                if (_typeId == SER_BOOLEAN_WRAPPER) {
                    return null;
                }
                return Boolean.FALSE;

            case JsonTokenId.ID_STRING:
                // 07-Jul-2020, tatu: Allow coercion for backwards compatibility (with 2.11)
                return p.getValueAsBoolean();
            }
            // 07-Jul-2020, tatu: leave out more esoteric coercions with 2.12
            break;

        case SER_CHAR:
            {
                String str = p.getValueAsString();
                return (str == null || str.isEmpty()) ? ' ' : str.charAt(0);
            }

        case SER_CALENDAR:
            // [jackson-jr#73]: should allow null
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            {
                long l = _fetchLong(p);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(l);
                return cal;
            }

        case SER_DATE:
            // [jackson-jr#73]: should allow null
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            return new Date(_fetchLong(p));

        case SER_CLASS:
        {
            // [jackson-jr#73]: should allow null
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            String v = p.getValueAsString();
            try {
                return Class.forName(v);
            } catch (Exception e) {
                throw new JSONObjectException("Failed to bind `java.lang.Class` from value '"+v+"'");
            }
        }
        case SER_FILE:
            // [jackson-jr#73]: should allow null
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            return new File(p.getValueAsString());
        case SER_UUID:
            // [jackson-jr#73]: should allow null
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            return UUID.fromString(p.getValueAsString());
        case SER_URL:
            // [jackson-jr#73]: should allow null
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            return new URL(p.getValueAsString());
        case SER_URI:
            // [jackson-jr#73]: should allow null
            if (p.hasToken(JsonToken.VALUE_NULL)) {
                return null;
            }
            return URI.create(p.getValueAsString());
        case SER_PATH:
            return _readPath(p);

//        case SER_MAP:
//        case SER_LIST:
//        case SER_COLLECTION:
//        case SER_OBJECT_ARRAY:
            // should never get here: we have dedicated readers
        default: // types that shouldn't get here
        //case SER_ENUM:
        }

        throw JSONObjectException.from(p,
                "Can not create a `"+_valueType.getName()+"` instance out of "+_tokenDesc(p));
    }    

    /*
    /**********************************************************************
    /* Read methods for scalars
    /**********************************************************************
     */

    protected byte[] _readBinary(JsonParser p) throws IOException {
        // [jackson-jr#107]: should allow null
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        return p.getBinaryValue();
    }

    // @since 2.17
    protected Path _readPath(JsonParser p) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        String v = p.getValueAsString();
        try {
            return Paths.get(v);
        } catch (Exception e) {
            throw new JSONObjectException("Failed to bind `java.nio.file.Path` from value '"+v+"'");
        }
    }

    protected int[] _readIntArray(JsonParser p) throws IOException {
        if (JsonToken.START_ARRAY.equals(p.currentToken())) {
            p.nextToken();
        }

        final IntStream.Builder builder = IntStream.builder();
        int t = p.currentTokenId();

        // Tiny optimization
        if (t == JsonTokenId.ID_END_ARRAY) {
            return NO_INTS;
        }
        
        main_loop:
        while (true) {
            switch (t) {
            case JsonTokenId.ID_NUMBER_FLOAT:
            case JsonTokenId.ID_NUMBER_INT:
            case JsonTokenId.ID_NULL:
                builder.add(p.getValueAsInt());
                break;
            case JsonTokenId.ID_END_ARRAY:
                break main_loop;
            default:
                throw new JSONObjectException("Failed to bind `int` element if `int[]` from value: "+
                        _tokenDesc(p));
            }
            p.nextToken();
            t = p.currentTokenId();
        }
        return builder.build().toArray();
    }

    protected long _fetchLong(JsonParser p) throws IOException
    {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return p.getLongValue();
        }
        throw JSONObjectException.from(p, "Can not get long numeric value from JSON (to construct "
                +_valueType.getName()+") from "+_tokenDesc(p, t));
    }
}
