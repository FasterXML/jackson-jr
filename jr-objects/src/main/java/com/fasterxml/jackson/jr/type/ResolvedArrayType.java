package com.fasterxml.jackson.jr.type;

import java.util.*;

public final class ResolvedArrayType extends ResolvedType
{
    protected final ResolvedType _elementType;
    
    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */

    public ResolvedArrayType(Class<?> erased, TypeBindings bindings,
            ResolvedType elementType)
    {
        super(erased, bindings);
        _elementType = elementType;
    }

    /*
    /**********************************************************************
    /* Accessors for related types
    /**********************************************************************
     */

    @Override
    public List<ResolvedType> getImplementedInterfaces() {
        return Collections.emptyList();
    }
    
    /*
    /**********************************************************************
    /* Simple property accessors
    /**********************************************************************
     */

    @Override
    public ResolvedType getArrayElementType() { return _elementType; }

    /*
    /**********************************************************************
    /* String representations
    /**********************************************************************
     */

    @Override
    public StringBuilder appendBriefDescription(StringBuilder sb)
    {
        sb = _elementType.appendBriefDescription(sb);
        sb.append("[]");
        return sb;
    }
}
