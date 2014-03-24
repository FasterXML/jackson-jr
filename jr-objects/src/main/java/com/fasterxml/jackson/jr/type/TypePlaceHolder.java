package com.fasterxml.jackson.jr.type;

public class TypePlaceHolder extends ResolvedType {
    protected final int _ordinal;
    protected ResolvedType _actualType;
    
    public TypePlaceHolder(int ordinal) {
        super(Object.class, null);
        _ordinal = ordinal;
    }

    public ResolvedType actualType() { return _actualType; }
    public void actualType(ResolvedType t) { _actualType = t; }

    @Override public StringBuilder appendDesc(StringBuilder sb) {
        sb.append('<').append(_ordinal).append('>');
        return sb;
    }
}
