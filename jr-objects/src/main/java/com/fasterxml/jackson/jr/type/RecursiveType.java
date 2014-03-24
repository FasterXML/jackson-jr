package com.fasterxml.jackson.jr.type;

public class RecursiveType extends ResolvedType {
    protected ResolvedType _referencedType;

    public RecursiveType(Class<?> erased, TypeBindings bindings) {
        super(T_RECURSIVE, erased, bindings, null, null);
    }

    void setReference(ResolvedType ref) { _referencedType = ref; }

    @Override public ResolvedType selfRefType() { return _referencedType; }
    @Override public StringBuilder appendDesc(StringBuilder sb) { return _appendClassDesc(sb); }
}
