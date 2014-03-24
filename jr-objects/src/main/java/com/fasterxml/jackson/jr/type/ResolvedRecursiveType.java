package com.fasterxml.jackson.jr.type;

public class ResolvedRecursiveType extends ResolvedType {
    protected ResolvedType _referencedType;

    public ResolvedRecursiveType(Class<?> erased, TypeBindings bindings) { super(erased, bindings); }

    void setReference(ResolvedType ref) { _referencedType = ref; }

    @Override public ResolvedType selfRefType() { return _referencedType; }
    @Override public StringBuilder appendDesc(StringBuilder sb) { return _appendClassDesc(sb); }
}
