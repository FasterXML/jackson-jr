package com.fasterxml.jackson.jr.type;

import java.util.*;

public class TypePlaceHolder extends ResolvedType
{
    protected final int _ordinal;
    protected ResolvedType _actualType;
    
    public TypePlaceHolder(int ordinal) {
        super(Object.class, TypeBindings.emptyBindings());
        _ordinal = ordinal;
    }

    public ResolvedType actualType() { return _actualType; }
    public void actualType(ResolvedType t) { _actualType = t; }

    @Override public List<ResolvedType> implInterfaces() { return Collections.<ResolvedType>emptyList(); }

    @Override public StringBuilder appendDesc(StringBuilder sb) {
        sb.append('<').append(_ordinal).append('>');
        return sb;
    }
}
