package com.fasterxml.jackson.simple.ob.impl;

import java.util.*;

import com.fasterxml.jackson.simple.ob.JSON.Feature;

/**
 * Helper class that is used for constructing {@link java.util.List}s
 * to map JSON Array values in.
 *<p>
 * Objects server both as "factories" for creating new builders (blueprint
 * style), and as actual builders. For each distinct read operation,
 * {@link #newBuilder} will be called at least once; this instance
 * may be used and reused multiple times, as calling {@link #start}
 * will reset the state so that more {@link List}s may be built.
 */
public abstract class ListBuilder
{
    protected final static Object[] EMPTY_ARRAY = new Object[0];
    
    protected final int _features;

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */
    
    protected ListBuilder(int features) {
        _features = features;
    }

    /**
     * Factory method for getting a blueprint instance of the default
     * {@link ListBuilder} implementation.
     */
    public static ListBuilder defaultImpl() {
        return new Default(0);
    }

    public abstract ListBuilder newBuilder(int features);

    public ListBuilder newBuilder() {
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
    
    public abstract ListBuilder start();

    public abstract ListBuilder add(Object value);

    /**
     * The usual build method to use for constructing {@link List}
     */
    public abstract List<Object> buildList();

    /**
     * Alternative build method used when desired result type is
     * <code>Object[]</code>
     */
    public Object[] buildArray() {
        // sub-optimal, but defined for convenience
        List<Object> l = buildList();
        return l.toArray(new Object[l.size()]);
    }

    /*
    /**********************************************************************
    /* More specialized build methods
    /**********************************************************************
     */
    
    /**
     * Specialized method that is called when an empty list needs to
     * be constructed; this may be a new list, or an immutable shared
     * List, depending on implementation.
     *<p>
     * Default implementation simply calls:
     *<pre>
     *  start().buildList();
     *</pre>
     */
    public List<Object> emptyList() {
        return start().buildList();
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

    /**
     * Specialized method that is called when an empty list needs to
     * be constructed; this may be a new list, or an immutable shared
     * List, depending on implementation.
     *<p>
     * Default implementation simply calls:
     *<pre>
     *  start().add(value).buildList();
     *</pre>
     */
    public List<Object> singletonList(Object value) {
        return start().add(value).buildList();
    }

    /**
     * Specialized method that is called when an empty list needs to
     * be constructed; this may be a new list, or an immutable shared
     * List, depending on implementation.
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
    
    /*
    /**********************************************************************
    /* Default implementations
    /**********************************************************************
     */
    
    /**
     * Default {@link ListBuilder} implementation, which uses {@link ArrayList}
     * as the type of {@link java.util.List} to build.
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
     * just extend basic {@link ListBuilder}.
     */
    public static class Default extends ListBuilder
    {
        protected List<Object> _current;
        
        protected Default(int features) {
            super(features);
        }
        
        @Override
        public ListBuilder newBuilder(int features) {
            return new Default(features);
        }
        
        @Override
        public ListBuilder start() {
            // If this builder is "busy", create a new one...
            if (_current != null) {
                return newBuilder().start();
            }
            _current = _list(12);
            return this;
        }
        
        @Override
        public List<Object> buildList() {
            List<Object> result = _current;
            _current = null;
            return result;
        }

        @Override
        public Object[] buildArray() {
            List<Object> l = _current;
            _current = null;
            final int len = l.size();
            Object[] result = new Object[len];
            l.toArray(result);
            return result;
        }
        
        @Override
        public ListBuilder add(Object value) {
            _current.add(value);
            return this;
        }
        
        @Override
        public List<Object> emptyList() {
            if (isEnabled(Feature.READ_ONLY)) {
                return Collections.emptyList();
            }
            return _list(0);
        }

        /**
         * Overridable factory method for constructing underlying List.
         */
        protected List<Object> _list(int initialSize) {
            return new ArrayList<Object>(initialSize);
        }
    }
}
