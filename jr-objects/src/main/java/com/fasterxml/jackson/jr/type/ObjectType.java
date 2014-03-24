package com.fasterxml.jackson.jr.type;

import java.util.*;

/**
 * Type implementation for classes that do not represent interfaces,
 * primitive or array types.
 */
public class ObjectType extends ResolvedType
{
    protected final ObjectType _superClass;
    protected final int _modifiers;

    public ObjectType(Class<?> erased, TypeBindings bindings,
            ObjectType superClass, List<ResolvedType> interfaces)
    {
        this(erased, bindings, superClass,
                (interfaces == null || interfaces.isEmpty()) ? NO_TYPES :
                interfaces.toArray(new ResolvedType[interfaces.size()]));
    }

    public ObjectType(Class<?> erased, TypeBindings bindings,
            ObjectType superClass, ResolvedType[] interfaces)
    {
        super(T_REGULAR, erased, bindings, interfaces, null);
        _superClass = superClass;
        _modifiers = erased.getModifiers();
    }

    @Override public ObjectType parentType() { return _superClass; }
    @Override public StringBuilder appendDesc(StringBuilder sb) { return _appendClassDesc(sb); }
}

