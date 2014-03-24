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
    /* Simple property accessors
    /**********************************************************************
     */

    @Override
    public boolean isInterface() { return true; }

    @Override
    public boolean isAbstract() { return true; }

    @Override
    public boolean isArray() { return false; }

    @Override
    public boolean isPrimitive() { return false; }

    /*
    /**********************************************************************
    /* String representations
    /**********************************************************************
     */

    @Override
    public StringBuilder appendSignature(StringBuilder sb) {
        return _appendClassSignature(sb);
    }

    @Override
    public StringBuilder appendErasedSignature(StringBuilder sb) {
        return _appendErasedClassSignature(sb);
    }

    @Override
    public StringBuilder appendBriefDescription(StringBuilder sb) {
        return _appendClassDescription(sb);
    }

    @Override
    public StringBuilder appendFullDescription(StringBuilder sb)
    {
        sb = _appendClassDescription(sb);
        // interfaces 'extend' other interfaces...
        int count = _superInterfaces.length;
        if (count > 0) {
            sb.append(" extends ");
            for (int i = 0; i < count; ++i) {
                if (i > 0) {
                    sb.append(",");
                }
                sb = _superInterfaces[i].appendBriefDescription(sb);
            }
        }
        return sb;
    }
}



