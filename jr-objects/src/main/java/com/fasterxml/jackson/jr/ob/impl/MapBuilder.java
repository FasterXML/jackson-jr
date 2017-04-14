package com.fasterxml.jackson.jr.ob.impl;

import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

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
    protected final int _features;
    protected final boolean _checkDups;
    
    /**
     * Optional {@link Map} implementation class, used when specific
     * implementation is desired.
     */
    protected final Class<?> _mapType;
    
    protected MapBuilder(int features, Class<?> type) {
        _features = features;
        _checkDups = JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS.isEnabled(features);
        _mapType = type;
    }

    /**
     * Factory method for getting a blueprint instance of the default
     * {@link MapBuilder} implementation.
     */
    public static MapBuilder defaultImpl() {
        return new Default(0, null);
    }

    public abstract MapBuilder newBuilder(int features);

    public abstract MapBuilder newBuilder(Class<?> mapImpl);
    
    public MapBuilder newBuilder() {
        return newBuilder(_features);
    }

    public final boolean isEnabled(JSON.Feature f) {
        return f.isEnabled(_features);
    }

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
    public Map<Object,Object> emptyMap() throws JSONObjectException {
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
    public Map<Object,Object> singletonMap(Object key, Object value) throws JSONObjectException {
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
        protected Map<Object,Object> _current;

        protected Default(int features, Class<?> type) {
            super(features, type);
        }

        @Override
        public MapBuilder newBuilder(int features) {
            return new Default(features, _mapType);
        }

        @Override
        public MapBuilder newBuilder(Class<?> mapImpl) {
            return new Default(_features, mapImpl);
        }

        @Override
        public MapBuilder start() {
            // If this builder is "busy", create a new one...
            if (_current != null) {
                return newBuilder().start();
            }
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
            if (_checkDups) {
                if (_current.containsKey(key)) {
                    // 14-Apr-2017, tatu: Note that choice of `IllegalArgumentException` is arbitrary
                    //   but not random: caller catches and re-packages it to give context
                    throw new IllegalArgumentException("Duplicate key (key '"+key+"')");
                }
            }
            _current.put(key, value);
            return this;
        }

        @Override
        public Map<Object,Object> emptyMap() {
            if ((_mapType == null) && isEnabled(Feature.READ_ONLY)) {
                return Collections.emptyMap();
            }
            return _map(4);
        }

        private final Map<Object,Object> _map(int initialSize) {
            if (_mapType != null) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<Object,Object> m = (Map<Object,Object>) _mapType.newInstance();
                    return m;
                } catch (Exception e) {
                    Throwable t = e;
                    while (t.getCause() != null) {
                        t = t.getCause();
                    }
                    throw new IllegalArgumentException("Failed to create an instance of "
                            +_mapType.getName()+" ("+t.getClass().getName()+"): "+t.getMessage());
                    
                }
            }
            final boolean sort = isEnabled(Feature.PRESERVE_FIELD_ORDERING);
            if (isEnabled(Feature.USE_DEFERRED_MAPS)) {
                return new DeferredMap(sort, initialSize);
            }
            return sort ? new LinkedHashMap<Object,Object>(initialSize)
                    : new HashMap<Object,Object>(initialSize);
        }
    }
}
