package com.fasterxml.jackson.jr.stree.util;

import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.jr.stree.JrsArray;
import com.fasterxml.jackson.jr.stree.JrsObject;
import com.fasterxml.jackson.jr.stree.JrsValue;

/**
 * Helper class used by {@link JrsTreeTraversingParser} to keep track
 * of current location within traversed JSON tree.
 */
abstract class JrsValueCursor
    extends TokenStreamContext
{
    /**
     * Parent cursor of this cursor, if any; null for root
     * cursors.
     */
    protected final JrsValueCursor _parent;

    /**
     * Current field name
     */
    protected String _currentName;

    /**
     * @since 2.5
     */
    protected java.lang.Object _currentValue;
    
    public JrsValueCursor(int contextType, JrsValueCursor p)
    {
        super();
        _type = contextType;
        _index = -1;
        _parent = p;
    }

    /*
    /**********************************************************************
    /* JsonStreamContext impl
    /**********************************************************************
     */

    // note: co-variant return type
    @Override
    public final JrsValueCursor getParent() { return _parent; }

    @Override
    public final String currentName() {
        return _currentName;
    }

    @Override
    public java.lang.Object currentValue() {
        return _currentValue;
    }

    @Override
    public void assignCurrentValue(java.lang.Object v) {
        _currentValue = v;
    }
    
    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    public abstract JsonToken nextToken();
    public abstract JsonToken nextValue();
    public abstract JsonToken endToken();

    public abstract JrsValue currentNode();
    public abstract boolean currentHasChildren();
    
    /**
     * Method called to create a new context for iterating all
     * contents of the current structured value (JSON array or object)
     */
    public final JrsValueCursor iterateChildren() {
        JrsValue n = currentNode();
        if (n == null) throw new IllegalStateException("No current node");
        if (n.isArray()) { // false since we have already returned START_ARRAY
            return new ArrayCursor((JrsArray) n, this);
        }
        if (n.isObject()) {
            return new ObjectCursor((JrsObject) n, this);
        }
        throw new IllegalStateException("Current node of type "+n.getClass().getName());
    }

    /*
    /**********************************************************
    /* Concrete implementations
    /**********************************************************
     */

    /**
     * Context matching root-level value nodes (i.e. anything other
     * than JSON Object and Array).
     * Note that context is NOT created for leaf values.
     */
    protected final static class RootCursor
        extends JrsValueCursor
    {
        protected JrsValue _node;

        protected boolean _done = false;

        public RootCursor(JrsValue n, JrsValueCursor p) {
            super(TokenStreamContext.TYPE_ROOT, p);
            _node = n;
        }

        @Override
        public JsonToken nextToken() {
            if (!_done) {
                _done = true;
                return _node.asToken();
            }
            _node = null;
            return null;
        }
        
        @Override
        public JsonToken nextValue() { return nextToken(); }
        @Override
        public JsonToken endToken() { return null; }
        @Override
        public JrsValue currentNode() { return _node; }
        @Override
        public boolean currentHasChildren() { return false; }
    }

    /**
     * Cursor used for traversing non-empty JSON Array nodes
     */
    protected final static class ArrayCursor
        extends JrsValueCursor
    {
        protected Iterator<JrsValue> _contents;

        protected JrsValue _currentNode;

        public ArrayCursor(JrsArray n, JrsValueCursor p) {
            super(TokenStreamContext.TYPE_ARRAY, p);
            _contents = n.elements();
        }

        @Override
        public JsonToken nextToken()
        {
            if (!_contents.hasNext()) {
                _currentNode = null;
                return null;
            }
            _currentNode = _contents.next();
            return _currentNode.asToken();
        }

        @Override
        public JsonToken nextValue() { return nextToken(); }
        @Override
        public JsonToken endToken() { return JsonToken.END_ARRAY; }

        @Override
        public JrsValue currentNode() { return _currentNode; }
        @Override
        public boolean currentHasChildren() {
            // note: ONLY to be called for container nodes
            return currentNode().size() > 0;
        }
    }

    /**
     * Cursor used for traversing non-empty JSON Object nodes
     */
    protected final static class ObjectCursor
        extends JrsValueCursor
    {
        protected Iterator<Map.Entry<String, JrsValue>> _contents;
        protected Map.Entry<String, JrsValue> _current;

        protected boolean _needEntry;
        
        public ObjectCursor(JrsValue n, JrsValueCursor p)
        {
            super(TokenStreamContext.TYPE_OBJECT, p);
            _contents = ((JrsObject) n).fields();
            _needEntry = true;
        }

        @Override
        public JsonToken nextToken()
        {
            // Need a new entry?
            if (_needEntry) {
                if (!_contents.hasNext()) {
                    _currentName = null;
                    _current = null;
                    return null;
                }
                _needEntry = false;
                _current = _contents.next();
                _currentName = (_current == null) ? null : _current.getKey();
                return JsonToken.FIELD_NAME;
            }
            _needEntry = true;
            return _current.getValue().asToken();
        }

        @Override
        public JsonToken nextValue()
        {
            JsonToken t = nextToken();
            if (t == JsonToken.FIELD_NAME) {
                t = nextToken();
            }
            return t;
        }

        @Override
        public JsonToken endToken() { return JsonToken.END_OBJECT; }

        @Override
        public JrsValue currentNode() {
            return (_current == null) ? null : _current.getValue();
        }
        @Override
        public boolean currentHasChildren() {
            // note: ONLY to be called for container nodes
            return currentNode().size() > 0;
        }
    }
}
