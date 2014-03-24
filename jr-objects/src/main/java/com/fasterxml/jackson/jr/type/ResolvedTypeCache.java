package com.fasterxml.jackson.jr.type;

import java.util.*;

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

    protected final Map<ResolvedTypeCache.Key, ResolvedType> _map;
    
    public ResolvedTypeCache(int max) {
        _map = new HashMap<ResolvedTypeCache.Key, ResolvedType>(max);
        _max = max;
    }

    /**
     * Helper method for constructing reusable cache keys
     */
    public Key key(Class<?> simpleType) {
        return new Key(simpleType);
    }

    /**
     * Helper method for constructing reusable cache keys
     */
    public Key key(Class<?> simpleType, ResolvedType[] tp) {
        return new Key(simpleType, tp);
    }
    
    public synchronized ResolvedType find(Key key) {
        return _map.get(key);
    }

    public synchronized int size() {
        return _map.size();
    }
    
    public synchronized void put(Key key, ResolvedType type) {
        if (_map.size() >= _max) {
            _map.clear();
        }
        _map.put(key, type);
    }

    public static class Key
    {
        private final Class<?> _erasedType;
        
        private final ResolvedType[] _typeParameters;
        
        private final int _hashCode;
        
        public Key(Class<?> simpleType) {
            this(simpleType, null);
        }
        
        public Key(Class<?> erasedType, ResolvedType[] tp)
        {
            // let's not hold on type empty arrays
            if (tp != null && tp.length == 0) {
                tp = null;
            }
            _erasedType = erasedType;
            _typeParameters = tp;
            int h = erasedType.getName().hashCode();
            if (tp != null) {
                h += tp.length;
            }
            _hashCode = h;
        }
        
        @Override
        public int hashCode() { return _hashCode; }

        @Override
        public boolean equals(Object o)
        {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            Key other = (Key) o;
            if (other._erasedType != _erasedType) return false;
            ResolvedType[] otherTP = other._typeParameters;
            if (_typeParameters == null) {
                return (otherTP == null);
            }
            if (otherTP == null || otherTP.length != _typeParameters.length) {
                return false;
            }
            for (int i = 0, len = _typeParameters.length; i < len; ++i) {
                if (!_typeParameters[i].equals(otherTP[i])) {
                    return false;
                }
            }
            return true;
        }
    }
}
