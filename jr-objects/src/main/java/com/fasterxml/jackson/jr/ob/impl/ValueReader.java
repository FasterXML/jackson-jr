package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Helper class used when reading values of complex types other
 * than Beans.
 *<p>
 * Note that ugly "chameleon" style operation here is used to avoid
 * creating multiple separate classes, which in turn is done to minimize
 * size of resulting jars.
 */
public abstract class ValueReader {
    protected final static int T_ARRAY = 0;
    protected final static int T_COLLECTION = 1;
    protected final static int T_ENUM = 2;
    protected final static int T_MAP = 3;

    /*
    /**********************************************************************
    /* Factory methods
    /**********************************************************************
     */
    
    public static ValueReader arrayReader(Class<?> enumType) {
        // !!! TBI
        return null;
    }

    public static ValueReader collectionReader(Class<?> enumType) {
        // !!! TBI
        return null;
    }

    public static ValueReader mapReader(Class<?> enumType) {
        // !!! TBI
        return null;
    }

    /*
    /**********************************************************************
    /* Basic API
    /**********************************************************************
     */
    
    public abstract Object read(JSONReader reader, JsonParser p) throws IOException;

    /*
    /**********************************************************************
    /* Helper methods for sub-classes
    /**********************************************************************
     */

    protected String _tokenDesc(JsonParser p) throws IOException {
        return _tokenDesc(p, p.getCurrentToken());
    }
    
    protected static String _tokenDesc(JsonParser p, JsonToken t) throws IOException {
        if (t == null) {
            return "NULL";
        }
        switch (t) {
        case FIELD_NAME:
            return "JSON Field name '"+p.getCurrentName()+"'";
        case START_ARRAY:
            return "JSON Array";
        case START_OBJECT:
            return "JSON Object";
        case VALUE_FALSE:
            return "'false'";
        case VALUE_NULL:
            return "'null'";
        case VALUE_NUMBER_FLOAT:
        case VALUE_NUMBER_INT:
            return "JSON Number";
        case VALUE_STRING:
            return "JSON String";
        case VALUE_TRUE:
            return "'true'";
        default:
            return t.toString();
        }
    }
}
