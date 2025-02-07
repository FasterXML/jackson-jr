package tools.jackson.jr.stree;

import java.util.*;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonPointer;
import tools.jackson.core.JsonToken;
import tools.jackson.core.tree.ArrayTreeNode;

public class JrsArray
    extends JrsValue
    implements ArrayTreeNode
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
        return JsonToken.START_ARRAY;
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
    public boolean isContainer() {
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
    public JrsValue get(String s) {
        return null;
    }

    @Override
    public JrsValue path(int i){
        return ((0 <= i) && (i < _values.size())) ? _values.get(i) : JrsMissing.instance();
    }

    @Override
    public JrsValue path(String s) {
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
    protected void write(JsonGenerator g, JacksonJrsTreeCodec codec)
        throws JacksonException
    {
        g.writeStartArray();
        for (int i = 0, end = _values.size(); i < end; ++i) {
            codec.writeTree(g, _values.get(i));
        }
        g.writeEndArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JrsArray jrsArray = (JrsArray) o;

        return _values != null ? _values.equals(jrsArray._values) : jrsArray._values == null;
    }

    @Override
    public int hashCode() {
        return _values != null ? _values.hashCode() : 0;
    }
}
