package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

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

    public EnumReader(Class<?> enumType,
            Object[] byIndex, Map<String,Object> byName) {
        super(enumType);
        _byIndex = byIndex;
        _byName = byName;
    }

    private String desc() {
        return _byIndex[0].getClass().getName();
    }

    @Override
    public Object readNext(JSONReader reader, JsonParser p) throws IOException {
        String name = p.nextTextValue();
        if (name != null) {
            return _enum(name);
        }
        return read(reader, p);
    }
    
    @Override
    public Object read(JSONReader reader, JsonParser p) throws IOException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            int ix = p.getIntValue();
            if (ix < 0 || ix >= _byIndex.length) {
                throw new JSONObjectException("Failed to bind Enum "+desc()+" with index "+ix
                        +" (has "+_byIndex.length+" values)");
            }
            return _byIndex[ix];
        }
        return _enum(p.getValueAsString().trim());
    }
    
    private Object _enum(String id) throws IOException
    {
        Object e = _byName.get(id);
        if (e == null) {
            throw new JSONObjectException("Failed to find Enum of type "+desc()+" for value '"+id+"'");
        }
        return e;
    }
}