package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Array;
import java.util.*;

import com.fasterxml.jackson.jr.ob.JSON.Feature;

/**
 * Helper class that is used for constructing {@link java.util.Collection}s
 * to map JSON Array values in.
 *<p>
 * Objects server both as "factories" for creating new builders (blueprint
 * style), and as actual builders. For each distinct read operation,
 * {@link #newBuilder} will be called at least once; this instance
 * may be used and reused multiple times, as calling {@link #start}
 * will reset the state so that more {@link Collection}s may be built.
 */
public abstract class CollectionBuilder
{
    protected final static Object[] EMPTY_ARRAY = new Object[0];
    
    protected final int _features;

    /**
     * Optional {@link Collection} implementation class, used when specific
     * implementation is desired.
     */
    protected final Class<?> _collectionType;

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    protected CollectionBuilder(int features, Class<?> collImpl) {
        _features = features;
        _collectionType = collImpl;
    }

    /**
     * Factory method for getting a blueprint instance of the default
     * {@link CollectionBuilder} implementation.
     */
    public static CollectionBuilder defaultImpl() {
        return new Default(0, null);
    }

    public abstract CollectionBuilder newBuilder(int features);

    public abstract CollectionBuilder newBuilder(Class<?> collImpl);
    
    public CollectionBuilder newBuilder() {
        return newBuilder(_features);
    }

    /*
    /**********************************************************************
    /* Simple accessors
    /**********************************************************************
     */
    
    public final boolean isEnabled(Feature f) {
        return f.isEnabled(_features);
    }

    /*
    /**********************************************************************
    /* Actual building
    /**********************************************************************
     */
    
    public abstract CollectionBuilder start();

    public abstract CollectionBuilder add(Object value);

    /**
     * The usual build method to use for constructing {@link Collection}
     */
    public abstract Collection<Object> buildCollection();

    /**
     * Alternative build method used when desired result type is
     * <code>Object[]</code>
     */
    public Object[] buildArray() {
        // sub-optimal, but defined for convenience
        Collection<Object> l = buildCollection();
        return l.toArray(new Object[l.size()]);
    }

    @SuppressWarnings("unchecked")
    public <T> T[] buildArray(Class<T> type) {
        // as above, sub-optimal etc, but works
        Collection<Object> l = buildCollection();
        Object[] a = (Object[]) Array.newInstance(type,  l.size());
        return (T[]) l.toArray(a);
    }

    /*
    /**********************************************************************
    /* More specialized build methods
    /**********************************************************************
     */
    
    /**
     * Specialized method that is called when an empty Collection needs to
     * be constructed; this may be a new Collection, or an immutable shared
     * one, depending on implementation.
     *<p>
     * Default implementation simply calls:
     *<pre>
     *  start().buildCollection();
     *</pre>
     */
    public Collection<Object> emptyCollection() {
        return start().buildCollection();
    }

    /**
     * Specialized method that is called when an empty <code>Object[]</code> needs to
     * be returned.
     *<p>
     * Default implementation simply returns a shared empty array instance.
     */
    public Object[] emptyArray() {
        return EMPTY_ARRAY;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] emptyArray(Class<T> type) {
        if (type == Object.class) {
            return (T[]) EMPTY_ARRAY;
        }
        return (T[]) Array.newInstance(type, 0);
    }

    /**
     * Specialized method that is called when a single-entry Collection needs to
     * be constructed.
     *<p>
     * Default implementation simply calls:
     *<pre>
     *  start().add(value).buildCollection();
     *</pre>
     */
    public Collection<Object> singletonCollection(Object value) {
        return start().add(value).buildCollection();
    }

    /**
     * Specialized method that is called when a single-entry array needs to
     * be constructed.
     *<p>
     * Default implementation simply returns equivalent of:
     *<pre>
     *   new Object[] { value }
     *</pre>
     */
    public Object[] singletonArray(Object value) {
        Object[] result = new Object[1];
        result[0] = value;
        return result;
    }

    public <T> T[] singletonArray(Class<?> type, T value) {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) Array.newInstance(type, 1);
        result[0] = value;
        return result;
    }
    
    /*
    /**********************************************************************
    /* Default implementations
    /**********************************************************************
     */
    
    /**
     * Default {@link CollectionBuilder} implementation, which uses {@link ArrayList}
     * as the type of {@link java.util.List} to build, unless instructed otherwise.
     *<p>
     * When sub-classing to use different underlying mutable {@link java.util.List}
     * type, you need to sub-class following methods:
     *<ul>
     * <li>{@link #newBuilder}: factory method for constructing new builder instance
     *  </li>
     * <li>{@link #_list}: factory method for constructing {@link java.util.List} to build
     *  </li>
     *</ul>
     *<p>
     * If constructing builders that use different approaches (like, say, produce
     * immutable Guava Lists), you may need to override more methods; or perhaps
     * just extend basic {@link CollectionBuilder}.
     */
    public static class Default extends CollectionBuilder
    {
        protected Collection<Object> _current;
        
        protected Default(int features, Class<?> collImpl) {
            super(features, collImpl);
        }

        @Override
        public CollectionBuilder newBuilder(int features) {
            return new Default(features, null);
        }

        @Override
        public CollectionBuilder newBuilder(Class<?> collType) {
            return new Default(_features, collType);
        }
        
        @Override
        public CollectionBuilder start() {
            // If this builder is "busy", create a new one...
            if (_current != null) {
                return newBuilder().start();
            }
            _current = _list(12);
            return this;
        }
        
        @Override
        public Collection<Object> buildCollection() {
            Collection<Object> result = _current;
            _current = null;
            return result;
        }

        @Override
        public Object[] buildArray() {
            Collection<Object> l = _current;
            _current = null;
            final int len = l.size();
            Object[] result = new Object[len];
            l.toArray(result);
            return result;
        }
        
        @Override
        public CollectionBuilder add(Object value) {
            _current.add(value);
            return this;
        }
        
        @Override
        public Collection<Object> emptyCollection() {
            if ((_collectionType == null) && isEnabled(Feature.READ_ONLY)) {
                return Collections.emptyList();
            }
            return _list(0);
        }

        /**
         * Overridable factory method for constructing underlying List.
         */
        protected Collection<Object> _list(int initialSize) {
            return new ArrayList<Object>(initialSize);
        }
    }
}
