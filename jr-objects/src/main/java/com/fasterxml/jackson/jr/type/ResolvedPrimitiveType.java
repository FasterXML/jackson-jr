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
    private final static ResolvedPrimitiveType VOID = new ResolvedPrimitiveType(Void.TYPE, 'V', "void");
    
    /**
     * Primitive types have single-character Signature, easy and efficient
     * to just store here
     */
    protected final String _signature;

    /**
     * Human-readable description should be simple as well
     */
    protected final String _description;
    
    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */

    protected ResolvedPrimitiveType(Class<?> erased, char sig, String desc)
    {
        super(erased, TypeBindings.emptyBindings());
        _signature = String.valueOf(sig);
        _description = desc;
    }
    
    public static List<ResolvedPrimitiveType> all()
    {
        ArrayList<ResolvedPrimitiveType> all = new ArrayList<ResolvedPrimitiveType>();
        all.add(new ResolvedPrimitiveType(Boolean.TYPE, 'Z', "boolean"));
        all.add(new ResolvedPrimitiveType(Byte.TYPE, 'B', "byte"));
        all.add(new ResolvedPrimitiveType(Short.TYPE, 'S', "short"));
        all.add(new ResolvedPrimitiveType(Character.TYPE, 'C', "char"));
        all.add(new ResolvedPrimitiveType(Integer.TYPE, 'I', "int"));
        all.add(new ResolvedPrimitiveType(Long.TYPE, 'J', "long"));
        all.add(new ResolvedPrimitiveType(Float.TYPE, 'F', "float"));
        all.add(new ResolvedPrimitiveType(Double.TYPE, 'D', "double"));
        return all;
    }

    public static ResolvedPrimitiveType voidType() {
        return VOID;
    }

    /*
    /**********************************************************************
    /* Accessors for related types
    /**********************************************************************
     */

    @Override
    public ResolvedType getSelfReferencedType() { return null; }
    
    @Override
    public ResolvedType getParentClass() { return null; }
    
    /*
    /**********************************************************************
    /* Simple property accessors
    /**********************************************************************
     */

    @Override
    public boolean isInterface() { return false; }
    
    @Override
    public boolean isAbstract() { return false; }

    @Override
    public ResolvedType getArrayElementType() { return null; }

    @Override
    public boolean isArray() { return false; }

    @Override
    public boolean isPrimitive() { return true; }

    @Override
    public List<ResolvedType> getImplementedInterfaces() {
        return Collections.emptyList();
    }

    /*
    /**********************************************************************
    /* Accessors for raw (minimally procesed) members
    /**********************************************************************
     */
    
    // Primitive types are simple; no fields, no methods, no constructors
    
    /*
    /**********************************************************************
    /* String representations
    /**********************************************************************
     */

    @Override
    public String getSignature() {
        return _signature;
    }

    @Override
    public String getErasedSignature() {
        return _signature;
    }

    @Override
    public String getFullDescription() {
        return _description;
    }

    @Override
    public StringBuilder appendSignature(StringBuilder sb) {
        sb.append(_signature);
        return sb;
    }

    @Override
    public StringBuilder appendErasedSignature(StringBuilder sb) {
        sb.append(_signature);
        return sb;
    }
    
    @Override
    public StringBuilder appendFullDescription(StringBuilder sb) {
        sb.append(_description);
        return sb;
    }

    @Override
    public StringBuilder appendBriefDescription(StringBuilder sb) {
        sb.append(_description);
        return sb;
    }
}
