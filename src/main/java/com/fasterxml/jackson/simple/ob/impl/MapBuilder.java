package com.fasterxml.jackson.simple.ob.impl;

import java.util.*;

import com.fasterxml.jackson.simple.ob.Feature;

/**
 * Helper class that is used for constructing {@link java.util.Map}s
 * to map JSON Object values in.
 *<p>
 * Objects server both as "factories" for creating new builders (blueprint
 * style), and as actual builders. For each distinct read operation,
 * {@link #newBuilder} will be called at least once; this instance
 * may be used and reused multiple times, as calling {@link #start}
 * will reset the state so that more {@link List}s may be built.
 */
public abstract class MapBuilder
{
    /**
     * Factory method for getting a blueprint instance of the default
     * {@link MapBuilder} implementation.
     */
    public static MapBuilder defaultImpl() {
        return new Default(0);
    }
    
    public abstract MapBuilder newBuilder(int features);

    public abstract MapBuilder start();

    public abstract MapBuilder put(Object key, Object value);

    public abstract Map<Object,Object> build();
    
    /**
     * Specialized method that is called when an empty list needs to
     * be constructed; this may be a new list, or an immutable shared
     * List, depending on implementation.
     *<p>
     * Default implementation simply calls:
     *<pre>
     *  start().build();
     *</pre>
     * which assumes that a builder has been constructed with {@link #newBuilder}
     */
    public Map<Object,Object> emptyMap() {
        return start().build();
    }

    /**
     * Specialized method that is called when an empty list needs to
     * be constructed; this may be a new list, or an immutable shared
     * List, depending on implementation.
     *<p>
     * Default implementation simply calls:
     *<pre>
     *  start().put(key, value).build();
     *</pre>
     */
    public Map<Object,Object> singletonMap(Object key, Object value) {
        return start().put(key, value).build();
    }

    /*
    /**********************************************************
    /* Default implementation
    /**********************************************************
     */

    /**
     * Default {@link MapBuilder} implementation which builds
     * either {@link HashMap} or {@link LinkedHashMap} instances
     * (depending on {@link Feature#PRESERVE_FIELD_ORDERING}).
     * It may also, with alternate configuration,
     * use {@link Collections#emptyMap()} for empty Maps,
     * if allowed with {@link Feature#READ_ONLY}.
     */
    public static class Default extends MapBuilder
    {
        protected final boolean _readOnly;

        protected final boolean _ordered;

        protected Map<Object,Object> _current;
        
        protected Default(int features) {
            _readOnly = Feature.READ_ONLY.isEnabled(features);
            _ordered = Feature.PRESERVE_FIELD_ORDERING.isEnabled(features);
        }
        
        @Override
        public MapBuilder newBuilder(int features) {
            return new Default(features);
        }

        @Override
        public MapBuilder start() {
            _current = _map(12);
            return this;
        }
        
        @Override
        public Map<Object,Object> build() {
            Map<Object,Object> result = _current;
            _current = null;
            return result;
        }

        @Override
        public MapBuilder put(Object key, Object value) {
            _current.put(key, value);
            return this;
        }
        
        @Override
        public Map<Object,Object> emptyMap() {
            if (_readOnly) {
                return Collections.emptyMap();
            }
            return _map(4);
        }

        private final HashMap<Object,Object> _map(int initialSize) {
            return _ordered ? new LinkedHashMap<Object,Object>(initialSize)
                    : new HashMap<Object,Object>(initialSize);
        }
    }
}
