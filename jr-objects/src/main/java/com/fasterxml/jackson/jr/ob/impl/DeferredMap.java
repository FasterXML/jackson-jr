package com.fasterxml.jackson.jr.ob.impl;

import java.util.*;

/**
 * A specialized {@link java.util.Map} implementation that will collect
 * entries during building, but only materialize full lookup structure
 * when needed; that is, actual building of hash lookup is deferred.
 *<p>
 * Inspired by lazily initialized Map used by Boon library.
 */
public class DeferredMap extends AbstractMap<Object, Object>
{
    private Map<Object, Object> _map;
    private Object[] _entries;
    private int _end;
    private final boolean _ordered;

    public DeferredMap(boolean ordered) {
        this(ordered, 4);
    }

    public DeferredMap(boolean ordered, int initialSize) {
        _ordered = ordered;
    }

    @Override
    public Object put(Object key, Object value)
    {
        if (_map == null) {
            if (_entries == null) {
                _entries = new Object[8];
            } else if (_end == _entries.length) {
                final int newSize = _newSize(_end);
                _entries = Arrays.copyOf(_entries, newSize);
            }
            _entries[_end] = key;
            _entries[++_end] = value;
            ++_end;
            // here's assuming no dups are added
            return null;
        }
        return _map.put(key, value);
    }
    
    @Override
    public Set<Entry<Object, Object>> entrySet() {
        buildIfNeeded();
        return _map.entrySet();
    }

    @Override
    public int size() {
        // assuming no dups; otherwise could overestimate
        return (_map == null) ? (_end >> 1) : _map.size();
    }

    @Override
    public boolean isEmpty() {
        return (_map == null) ? (_end == 0) : _map.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        buildIfNeeded();
        return _map.containsValue(value);
    }

    @Override
    public boolean containsKey(Object key) {
        buildIfNeeded();
        return _map.containsKey(key);
    }

    @Override
    public Object get(Object key) {
        buildIfNeeded();
        return _map.get( key );
    }

    @Override
    public Object remove(Object key) {
        buildIfNeeded();
        return _map.remove( key );
    }

    @Override
    public void clear() {
        if (_map != null ) {
            _map.clear();
        } else {
            _end = 0;
        }
    }

    @Override
    public Set<Object> keySet() {
        buildIfNeeded();
        return _map.keySet();
    }

    @Override
    public Collection<Object> values() {
        buildIfNeeded();
        return _map.values();
    }

    @Override
    public boolean equals(Object other) {
       buildIfNeeded();
       return _map.equals(other);
    }

    @Override
    public int hashCode() {
       buildIfNeeded();
       return _map.hashCode();
    }

    @Override
    public String toString() {
       buildIfNeeded();
       return _map.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        buildIfNeeded();
        if (_map instanceof HashMap)  {
            return ((HashMap<?,?>)_map).clone();
        }
        return new HashMap<Object,Object>(_map);
    }

    protected void buildIfNeeded() {
        if (_map == null) {
            // translate from entry count (which is 2 * size) bit down; trying to avoid
            // having to resize... i.e. use 3/4 of entry count
            _map = _buildMap(_end >> 2);
            for (int i = 0; i < _end; i += 2) {
                _map.put((String) _entries[i], _entries[i+1]);
            }
            _entries = null;
        }
    }

    private final int _newSize(int size)
    {
        if (size < 200) {
            return size+size;
        }
        // note: MUST ensure it's divisible by two (that is, last bit is 0), because
        // always adding values in pairs, but checking size before add
        if (size < 2000) {
            return size + ((size>>1) & ~1);
        }
        return size + ((size>>2) & ~1);
    }
    
    protected Map<Object,Object> _buildMap(int expSize)
    {
        int size;
        if (expSize < 4) {
            size = 4;
        } else {
            // should add ~1/3 as size is rounded up to power of 3
            size = expSize + (3 * (expSize >> 3));
        }
        if (_ordered) {
            return new LinkedHashMap<Object,Object>(size);
        }
        return new HashMap<Object,Object>(size);
    }
}
