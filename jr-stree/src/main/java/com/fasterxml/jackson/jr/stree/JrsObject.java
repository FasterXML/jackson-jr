package com.fasterxml.jackson.jr.stree;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.tree.ObjectTreeNode;

public class JrsObject
    extends JrsValue
    implements ObjectTreeNode
{
    private final Map<String, JrsValue> _values;

    public JrsObject() {
        this(Collections.<String, JrsValue>emptyMap());
    }

    public JrsObject(Map<String, JrsValue> values) {
        _values = values;
    }

    @Override
    public JsonToken asToken() {
        return JsonToken.START_OBJECT;
    }

    @Override
    public int size() {
        return _values.size();
    }

    @Override
    public boolean isValueNode() {
        return false;
    }

    @Override
    public boolean isContainerNode() {
        return true;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public Iterator<String> propertyNames() {
        return _values.keySet().iterator();
    }

    @Override
    public JrsValue get(int i) {
        return null;
    }

    @Override
    public JrsValue get(String name) {
        return _values.get(name);
    }

    @Override
    public JrsValue path(int i) {
        return JrsMissing.instance();
    }

    @Override
    public JrsValue path(String name) {
        JrsValue v = _values.get(name);
        return (v == null) ? JrsMissing.instance() : v;
    }

    @Override
    protected JrsValue _at(JsonPointer ptr) {
        String prop = ptr.getMatchingProperty();
        // fine to return `null`; caller converts to "missing":
        return get(prop);
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    public Iterator<Map.Entry<String, JrsValue>> fields() {
        if (_values.isEmpty()) {
            return _values.entrySet().iterator();
        }
        return _values.entrySet().iterator();
    }

    /*
    /**********************************************************************
    /* Abstract methods
    /**********************************************************************
     */

    @Override
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec) throws JacksonException
    {
        g.writeStartObject();
        if (!_values.isEmpty()) {
            for (Map.Entry<String,JrsValue> entry : _values.entrySet()) {
                g.writeName(entry.getKey());
                codec.writeTree(g, entry.getValue());
            }
        }
        g.writeEndObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JrsObject jrsObject = (JrsObject) o;

        return _values != null ? _values.equals(jrsObject._values) : jrsObject._values == null;
    }

    @Override
    public int hashCode() {
        return _values != null ? _values.hashCode() : 0;
    }
}
