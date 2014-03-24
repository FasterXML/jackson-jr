package com.fasterxml.jackson.jr.type;

public final class ResolvedArrayType extends ResolvedType {
    protected final ResolvedType _elementType;

    public ResolvedArrayType(Class<?> erased, TypeBindings bindings, ResolvedType elementType) {
        super(erased, bindings);
        _elementType = elementType;
    }

    public ResolvedType elementType() { return _elementType; }

    @Override public StringBuilder appendDesc(StringBuilder sb) {
        return _elementType.appendDesc(sb).append("[]");
    }
}
