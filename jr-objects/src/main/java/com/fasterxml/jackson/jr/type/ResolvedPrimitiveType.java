package com.fasterxml.jackson.jr.type;

public final class ResolvedPrimitiveType extends ResolvedType {
    protected final String _desc;

    protected ResolvedPrimitiveType(Class<?> erased, String desc) {
        super(erased, TypeBindings.emptyBindings());
        _desc = desc;
    }

    @Override public StringBuilder appendDesc(StringBuilder sb) {
        return sb.append(_desc);
    }
}
