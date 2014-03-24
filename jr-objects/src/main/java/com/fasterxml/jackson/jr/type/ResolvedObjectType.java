package com.fasterxml.jackson.jr.type;

import java.util.*;

/**
 * Type implementation for classes that do not represent interfaces,
 * primitive or array types.
 */
public class ResolvedObjectType extends ResolvedType
{
    protected final ResolvedObjectType _superClass;
    protected final ResolvedType[] _superInterfaces;
    protected final int _modifiers;

    public ResolvedObjectType(Class<?> erased, TypeBindings bindings,
            ResolvedObjectType superClass, List<ResolvedType> interfaces)
    {
        this(erased, bindings, superClass,
                (interfaces == null || interfaces.isEmpty()) ? NO_TYPES :
                interfaces.toArray(new ResolvedType[interfaces.size()]));
    }

    public ResolvedObjectType(Class<?> erased, TypeBindings bindings,
            ResolvedObjectType superClass, ResolvedType[] interfaces)
    {
        super(erased, bindings);
        _superClass = superClass;
        _superInterfaces = (interfaces == null) ? NO_TYPES : interfaces;
        _modifiers = erased.getModifiers();
    }

    @Override public ResolvedObjectType parentType() { return _superClass; }

    @Override public List<ResolvedType> implInterfaces() {
        return (_superInterfaces.length == 0) ?
                Collections.<ResolvedType>emptyList() : Arrays.asList(_superInterfaces);
    }

    @Override public StringBuilder appendDesc(StringBuilder sb) { return _appendClassDesc(sb); }
}

