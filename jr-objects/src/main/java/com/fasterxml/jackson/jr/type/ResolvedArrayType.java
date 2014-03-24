package com.fasterxml.jackson.jr.type;

import java.util.*;

public final class ResolvedArrayType extends ResolvedType
{
    protected final ResolvedType _elementType;

    public ResolvedArrayType(Class<?> erased, TypeBindings bindings, ResolvedType elementType) {
        super(erased, bindings);
        _elementType = elementType;
    }

    @Override public List<ResolvedType> implInterfaces() { return Collections.emptyList(); }
    @Override public ResolvedType elementType() { return _elementType; }

    @Override public StringBuilder appendDesc(StringBuilder sb) {
        sb = _elementType.appendDesc(sb);
        sb.append("[]");
        return sb;
    }
}
