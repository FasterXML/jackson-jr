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
    /**
     * Factory method for getting a blueprint instance of the default
     * {@link ListBuilder} implementation.
     */
    public static ListBuilder defaultImpl() {
        return new Default(0);
    }
    
    public abstract ListBuilder newBuilder(int features);

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
     */
    public static class Default extends ListBuilder
    {
        protected final boolean _readOnly;

        protected List<Object> _current;
        
        protected Default(int features) {
            _readOnly = Feature.READ_ONLY.isEnabled(features);
        }
        
        @Override
        public ListBuilder newBuilder(int features) {
            return new Default(features);
        }

        @Override
        public ListBuilder start() {
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
            if (_readOnly) {
                return Collections.emptyList();
            }
            return _list(0);
        }

        private final List<Object> _list(int initialSize) {
            return new ArrayList<Object>(initialSize);
        }
    }
}
