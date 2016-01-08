package com.fasterxml.jackson.jr.stree;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;

import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;

public class JrsArray extends JrsValue
{
    private final List<JrsValue> _values;

    public JrsArray() {
        _values = Collections.emptyList();
    }

    public JrsArray(List<JrsValue> v) {
        _values = v;
    }

    @Override
    public JsonToken asToken() {
        return START_ARRAY;
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
    public boolean isArray() {
        return true;
    }

    @Override
    public JrsValue get(int i) {
        return ((0 <= i) && (i < _values.size())) ? _values.get(i) : null;
    }

    @Override
    public TreeNode get(String s) {
        return null;
    }

    @Override
    public JrsValue path(int i){
        return ((0 <= i) && (i < _values.size())) ? _values.get(i) : JrsMissing.instance();
    }

    @Override
    public TreeNode path(String s) {
        return JrsMissing.instance();
    }

    @Override
    protected JrsValue _at(JsonPointer ptr) {
        // fine to return `null`; caller converts to "missing":
        return get(ptr.getMatchingIndex());
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    public Iterator<JrsValue> elements() {
        if (_values.isEmpty()) { // fine, nothing that could be removed anyway
            return _values.iterator();
        }
        // ensure caller can not modify values this way
        return Collections.unmodifiableList(_values).iterator();            
    }

    /*
    /**********************************************************************
    /* Abstract methods
    /**********************************************************************
     */

    @Override
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec) throws IOException
    {
        g.writeStartArray();
        for (int i = 0, end = _values.size(); i < end; ++i) {
            codec.writeTree(g, _values.get(i));
        }
        g.writeEndArray();
    }
}
