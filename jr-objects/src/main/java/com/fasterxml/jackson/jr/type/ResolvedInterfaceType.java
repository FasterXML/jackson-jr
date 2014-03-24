package com.fasterxml.jackson.jr.type;

import java.util.*;

public class ResolvedInterfaceType extends ResolvedType
{

    /**
     * List of interfaces this type implements; may be empty but never null
     */
    protected final ResolvedType[] _superInterfaces;

    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */

    public ResolvedInterfaceType(Class<?> erased, TypeBindings bindings,
            ResolvedType[] superInterfaces)
    {
        super(erased, bindings);
        _superInterfaces = (superInterfaces == null ? NO_TYPES : superInterfaces);
    }

    /*
    /**********************************************************************
    /* Accessors for related types
    /**********************************************************************
     */

    @Override
    public List<ResolvedType> getImplementedInterfaces() {
        return (_superInterfaces.length == 0) ?
                Collections.<ResolvedType>emptyList() : Arrays.asList(_superInterfaces);
    }

    /*
    /**********************************************************************
    /* String representations
    /**********************************************************************
     */

    @Override
    public StringBuilder appendBriefDescription(StringBuilder sb) {
        return _appendClassDescription(sb);
    }
}



