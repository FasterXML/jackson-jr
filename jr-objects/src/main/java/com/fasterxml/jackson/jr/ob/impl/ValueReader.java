package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.JSONReader;

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
    
    public static ValueReader arrayReader(Class<?> enumType) {
        // !!! TBI
        return null;
    }

    public static ValueReader collectionReader(Class<?> enumType) {
        // !!! TBI
        return null;
    }

    public static ValueReader enumReader(Class<?> enumType) {
        Object[] enums = enumType.getEnumConstants();
        Map<String,Object> byName = new HashMap<String,Object>();
        for (Object e : enums) {
            byName.put(e.toString(), e);
        }
        return new EnumR(enums, byName);
    }

    public static ValueReader mapReader(Class<?> enumType) {
        // !!! TBI
        return null;
    }

    public abstract Object read(JSONReader reader, JsonParser p) throws IOException;

    public static class EnumR extends ValueReader
    {
        protected final Object[] _byIndex;
        protected final Map<String,Object> _byName;

        public EnumR(Object[] byIndex, Map<String,Object> byName) {
            _byIndex = byIndex;
            _byName = byName;
        }

        private String desc() {
            return _byIndex[0].getClass().getName();
        }
        
        @Override
        public Object read(JSONReader reader, JsonParser p) throws IOException {
            JsonToken t = p.getCurrentToken();
            if (t == JsonToken.VALUE_NUMBER_INT) {
                int ix = p.getIntValue();
                if (ix < 0 || ix >= _byIndex.length) {
                    throw new JSONObjectException("Failed to bind Enum "+desc()+" with index "+ix
                            +" (has "+_byIndex.length+" values)");
                }
                return _byIndex[ix];
            }
            String id = p.getValueAsString().trim();
            Object e = _byName.get(id);
            if (e == null) {
                throw new JSONObjectException("Failed to find Enum of type "+desc()+" for value '"+id+"'");
            }
            return e;
        }
    }
}
