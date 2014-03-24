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
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isAbstract() { return false; }

    @Override
    public ResolvedType getArrayElementType() { return _elementType; }

    @Override
    public boolean isArray() { return true; }

    @Override
    public boolean isPrimitive() { return false; }

    /*
    /**********************************************************************
    /* Accessors for raw (minimally procesed) members
    /**********************************************************************
     */

    // defaults are fine (nothing to access)
    
    /*
    /**********************************************************************
    /* String representations
    /**********************************************************************
     */

    @Override
    public StringBuilder appendSignature(StringBuilder sb) {
        sb.append('[');
        return _elementType.appendSignature(sb);
    }

    @Override
    public StringBuilder appendErasedSignature(StringBuilder sb) {
        sb.append('[');
        return _elementType.appendErasedSignature(sb);
    }

    @Override
    public StringBuilder appendBriefDescription(StringBuilder sb)
    {
        sb = _elementType.appendBriefDescription(sb);
        sb.append("[]");
        return sb;
    }

    @Override
    public StringBuilder appendFullDescription(StringBuilder sb) {
        return appendBriefDescription(sb);
    }
}
