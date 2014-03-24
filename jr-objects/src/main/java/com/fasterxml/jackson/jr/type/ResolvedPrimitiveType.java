package com.fasterxml.jackson.jr.type;

import java.util.*;

/**
 * Type used for Java primitive types (which does not include arrays here).
 *<p>
 * Since set of primitive types is bounded, constructor is defined as protected,
 * and class final; that is, new primitive types are not to be constructed
 * by calling applications.
 */
public final class ResolvedPrimitiveType extends ResolvedType
{
    private final static ResolvedPrimitiveType VOID = new ResolvedPrimitiveType(Void.TYPE, "void");

    /**
     * Human-readable description should be simple as well
     */
    protected final String _description;
    
    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */

    protected ResolvedPrimitiveType(Class<?> erased, String desc)
    {
        super(erased, TypeBindings.emptyBindings());
        _description = desc;
    }
    
    public static List<ResolvedPrimitiveType> all()
    {
        ArrayList<ResolvedPrimitiveType> all = new ArrayList<ResolvedPrimitiveType>();
        all.add(new ResolvedPrimitiveType(Boolean.TYPE, "boolean"));
        all.add(new ResolvedPrimitiveType(Byte.TYPE, "byte"));
        all.add(new ResolvedPrimitiveType(Short.TYPE, "short"));
        all.add(new ResolvedPrimitiveType(Character.TYPE, "char"));
        all.add(new ResolvedPrimitiveType(Integer.TYPE, "int"));
        all.add(new ResolvedPrimitiveType(Long.TYPE, "long"));
        all.add(new ResolvedPrimitiveType(Float.TYPE, "float"));
        all.add(new ResolvedPrimitiveType(Double.TYPE, "double"));
        return all;
    }

    public static ResolvedPrimitiveType voidType() {
        return VOID;
    }

    /*
    /**********************************************************************
    /* Simple property accessors
    /**********************************************************************
     */

    @Override
    public List<ResolvedType> getImplementedInterfaces() {
        return Collections.emptyList();
    }

    /*
    /**********************************************************************
    /* String representations
    /**********************************************************************
     */
    @Override
    public StringBuilder appendBriefDescription(StringBuilder sb) {
        sb.append(_description);
        return sb;
    }
}
