package com.fasterxml.jackson.jr.type;

import java.util.*;

public class ResolvedInterfaceType extends ResolvedType
{
    protected final ResolvedType[] _superInterfaces;

    public ResolvedInterfaceType(Class<?> erased, TypeBindings bindings, ResolvedType[] superInterfaces) {
        super(erased, bindings);
        _superInterfaces = (superInterfaces == null ? NO_TYPES : superInterfaces);
    }

    @Override
    public List<ResolvedType> implInterfaces() {
        return (_superInterfaces.length == 0) ?
                Collections.<ResolvedType>emptyList() : Arrays.asList(_superInterfaces);
    }

    @Override public StringBuilder appendDesc(StringBuilder sb) {
        return _appendClassDesc(sb);
    }
}



