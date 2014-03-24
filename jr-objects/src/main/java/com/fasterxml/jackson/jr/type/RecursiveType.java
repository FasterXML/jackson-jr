package com.fasterxml.jackson.jr.type;

public class RecursiveType extends ResolvedType {
    protected ResolvedType _referencedType;

    public RecursiveType(Class<?> erased, TypeBindings bindings) {
        super(erased, bindings);
    }

    void setReference(ResolvedType ref) { _referencedType = ref; }
    public ResolvedType selfRefType() { return _referencedType; }
}
