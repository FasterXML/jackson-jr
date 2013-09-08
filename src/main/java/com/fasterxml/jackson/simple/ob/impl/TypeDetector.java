package com.fasterxml.jackson.simple.ob.impl;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper object used for efficient detection of type information
 * relevant to our conversion needs.
 *<p>
 * Note that usage pattern is such that a single "root" instance is kept
 * by each {@link com.fasterxml.jackson.simple.ob.JSON} instance; and
 * an actual per-operation instance must be constructed by calling
 * {@link #perOperationInstance()}: reason for this is that instances
 * use simple caching to handle the common case of repeating types
 * within JSON Arrays.
 */
public class TypeDetector
{
    protected final ConcurrentHashMap<ClassKey, SimpleType> _knownTypes;

    protected final ClassKey _key = new ClassKey();
    
    protected Class<?> _prevClass;

    protected SimpleType _prevType;
    
    protected TypeDetector(ConcurrentHashMap<ClassKey, SimpleType> types) {
        _knownTypes = types;
    }

    protected TypeDetector(TypeDetector base) {
        _knownTypes = base._knownTypes;
    }
    
    public final static TypeDetector rootDetector() {
        return new TypeDetector(new ConcurrentHashMap<ClassKey, SimpleType>(50, 0.75f, 4));
    }

    public TypeDetector perOperationInstance() {
        return new TypeDetector(this);
    }

    public final SimpleType findType(Class<?> raw)
    {
        if (raw == _prevClass) {
            return _prevType;
        }
        ClassKey k = _key;
        k.reset(raw);
        SimpleType t = _knownTypes.get(k);
        if (t == null) {
            t = _find(raw);
        }
        _prevType = t;
        _prevClass = raw;
        return t;
    }

    protected SimpleType _find(Class<?> raw)
    {
        // !!! TODO
        return null;
    }
}
