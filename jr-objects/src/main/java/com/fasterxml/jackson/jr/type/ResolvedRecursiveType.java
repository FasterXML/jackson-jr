package com.fasterxml.jackson.jr.type;

import java.util.*;

public class ResolvedRecursiveType extends ResolvedType
{
    protected ResolvedType _referencedType;

    public ResolvedRecursiveType(Class<?> erased, TypeBindings bindings) {
        super(erased, bindings);
    }

    public void setReference(ResolvedType ref) {
        if (_referencedType != null) {
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
    }

    @Override
    public ResolvedType selfRefType() { return _referencedType; }

    @Override
    public List<ResolvedType> implInterfaces() {
        return Collections.<ResolvedType>emptyList();
    }

    @Override
    public StringBuilder appendDesc(StringBuilder sb) { return _appendClassDesc(sb); }
}
