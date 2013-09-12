package com.fasterxml.jackson.simple.ob.impl;

import java.util.*;

import com.fasterxml.jackson.simple.ob.Feature;

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
    protected final int _features;

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

    public final boolean isEnabled(Feature f) {
        return f.isEnabled(_features);
    }
    
    public abstract ListBuilder start();

    public abstract ListBuilder add(Object value);

    public abstract List<Object> build();

    /**
     * Specialized method that is called when an empty list needs to
     * be constructed; this may be a new list, or an immutable shared
     * List, depending on implementation.
     *<p>
     * Default implementation simply calls:
     *<pre>
     *  start().build();
     *</pre>
     */
    public List<Object> emptyList() {
        return start().build();
    }

    /**
     * Specialized method that is called when an empty list needs to
     * be constructed; this may be a new list, or an immutable shared
     * List, depending on implementation.
     *<p>
     * Default implementation simply calls:
     *<pre>
     *  start().add(value).build();
     *</pre>
     */
    public List<Object> singletonList(Object value) {
        return start().add(value).build();
    }
    
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
        public List<Object> build() {
            List<Object> result = _current;
            _current = null;
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
