package com.fasterxml.jackson.jr.type;

import java.util.*;

import com.fasterxml.jackson.jr.ob.impl.ClassKey;

/**
 * Simple LRU cache used for storing up to specified number of most recently accessed
 * {@link ResolvedType} instances.
 * Since usage pattern is such that caller needs synchronization, cache access methods
 * are fully synchronized so that caller need not do explicit synchronization.
 * Note too that instead of LRU (via {@link java.util.LinkedHashMap} sub-classing)
 * we will instead simply clear out cache once it fills up.
 */
public class ResolvedTypeCache
{
    protected final int _max;

    protected final Map<ClassKey, ResolvedType> _map;
    
    public ResolvedTypeCache(int max) {
        _map = new HashMap<ClassKey, ResolvedType>(max);
        _max = max;
    }

    public synchronized ResolvedType find(ClassKey key) {
        return _map.get(key);
    }

    public synchronized void put(ClassKey key, ResolvedType type) {
        if (_map.size() >= _max) {
            _map.clear();
        }
        _map.put(key, type);
    }
}
