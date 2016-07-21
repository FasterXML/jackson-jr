package com.fasterxml.jackson.jr.type;

public class RecursiveType extends ResolvedType
{
    private static final long serialVersionUID = 1L;

    protected ResolvedType _referencedType;

    public RecursiveType(Class<?> erased, TypeBindings bindings) {
        super(erased, bindings);
    }

    void setReference(ResolvedType ref) { _referencedType = ref; }
    public ResolvedType selfRefType() { return _referencedType; }

    @Override public boolean equals(Object o) {
        // shouldn't really ever match, even if resolved to same thing, should it?
        return (this == o);
    }
}
