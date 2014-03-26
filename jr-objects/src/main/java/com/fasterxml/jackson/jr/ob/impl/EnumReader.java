package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

/**
 * Reader for Enum values: needed because we need a simple {@link java.util.Map}
 * for efficient conversion from id (gotten with {@link java.lang.Enum#toString()}
 * to value.
 *<p>
 * In future we could consider alternatively allowing use of
 * {@link java.lang.Enum#name()} for id.
 */
public class EnumReader extends ValueReader
{
    protected final Object[] _byIndex;
    protected final Map<String,Object> _byName;

    public EnumReader(Object[] byIndex, Map<String,Object> byName) {
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