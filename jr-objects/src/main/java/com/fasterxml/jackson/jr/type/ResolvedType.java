package com.fasterxml.jackson.jr.type;

import java.util.*;

public abstract class ResolvedType implements java.lang.reflect.Type
{
    public final static ResolvedType[] NO_TYPES = new ResolvedType[0];
    
    protected final Class<?> _erasedType;

    /**
     * Type bindings active when resolving members (methods, fields,
     * constructors) of this type
     */
    protected final TypeBindings _typeBindings;
    
    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */
    
    protected ResolvedType(Class<?> cls, TypeBindings bindings) {
        _erasedType = cls;
        _typeBindings = (bindings == null) ? TypeBindings.emptyBindings() : bindings;
    }

    /*
    /**********************************************************************
    /* Accessors for related types
    /**********************************************************************
     */
    
    /**
     * Returns type-erased Class<?> that this resolved type has.
     */
    public Class<?> erasedType() { return _erasedType; }

    /**
     * Returns parent class of this type, if it has one; primitive types
     * and interfaces have no parent class, nor does Object type
     * {@link java.lang.Object}.
     * Also, placeholders for cyclic (recursive) types return null for
     * this method.
     */
    public ResolvedType parentType() { return null; }

    /**
     * Accessor that must be used to find out actual type in
     * case of "self-reference"; case where type refers
     * recursive to itself (like, <code>T implements Comparable&lt;T></code>).
     * For all other types returns null but for self-references "real" type.
     * Separate accessor is provided to avoid accidental infinite loops.
     */
    public ResolvedType selfRefType() { return null; }

    /**
     * Returns ordered list of interfaces (in declaration order) that this type
     * implements.
     * 
     * @return List of interfaces this type implements, if any; empty list if none
     */
    public List<ResolvedType> implInterfaces() { return Collections.emptyList(); }

    /**
     * Returns list of generic type declarations for this type, in order they
     * are declared in class description.
     */
    public List<ResolvedType> getTypeParameters() { return _typeBindings.getTypeParameters(); }

    /**
     * Method for accessing bindings of type variables to resolved types in context
     * of this type. It has same number of entries as return List of
     * {@link #getTypeParameters}, accessible using declared name to which they
     * bind; for example, {@link java.util.Map} has 2 type bindings; one for
     * key type (name "K", from Map.java) and one for value type
     * (name "V", from Map.java).
     */
    public TypeBindings getTypeBindings() { return _typeBindings; }
    
    /**
     * Method that will try to find type parameterization this type
     * has for specified super type
     * 
     * @return List of type parameters for specified supertype (which may
     *   be empty, if supertype is not a parametric type); null if specified
     *   type is not a super type of this type
     */
    public List<ResolvedType> typeParametersFor(Class<?> erasedSupertype)
    {
        ResolvedType type = findSupertype(erasedSupertype);
        if (type != null) {
            return type.getTypeParameters();
        }
        // nope; doesn't look like we extend or implement super type in question
        return null;
    }

    /**
     * Method for finding super type of this type that has specified type
     * erased signature. If supertype is an interface which is implemented
     * using multiple inheritance paths, preference is given to interfaces
     * implemented "highest up the stack" (directly implemented interfaces
     * over interfaces superclass implements).
     */
    public ResolvedType findSupertype(Class<?> erasedSupertype)
    {
        if (erasedSupertype == _erasedType) {
            return this;
        }
        // Check super interfaces first:
        if (erasedSupertype.isInterface()) {
            for (ResolvedType it : implInterfaces()) {
                ResolvedType type = it.findSupertype(erasedSupertype);
                if (type != null) {
                    return type;
                }
            }
        }
        // and if not found, super class and its supertypes
        ResolvedType pc = parentType();
        if (pc != null) {
            ResolvedType type = pc.findSupertype(erasedSupertype);
            if (type != null) {
                return type;
            }
        }
        // nope; doesn't look like we extend or implement super type in question
        return null;
    }

    /*
    /**********************************************************************
    /* String representations
    /**********************************************************************
     */

    /**
     * Human-readable brief description of type, which does not include
     * information about super types.
     */
    public String getDesc() {
        StringBuilder sb = new StringBuilder();
        return appendDesc(sb).toString();
    }

    public abstract StringBuilder appendDesc(StringBuilder sb);

    /*
    /**********************************************************************
    /* Standard methods
    /**********************************************************************
     */

    @Override public String toString() {
        return getDesc();
    }

    @Override public int hashCode() {
        return _erasedType.getName().hashCode() + _typeBindings.hashCode();
    }

    @Override public boolean equals(Object o)
    {
        if (o == this) return true;
        // sub-types must be same:
        if (o == null || o.getClass() != getClass()) return false;
        // Should be possible to actually implement here...
        ResolvedType other = (ResolvedType) o;
        if (other._erasedType != _erasedType) {
            return false;
        }
        // and type bindings must match as well
        return _typeBindings.equals(other._typeBindings);
    }
    
    /*
    /**********************************************************************
    /* Helper methods for sub-classes; string construction
    /**********************************************************************
     */

    protected StringBuilder _appendClassDesc(StringBuilder sb) {
        sb.append(_erasedType.getName());
        int count = _typeBindings.size();
        if (count > 0) {
            sb.append('<');
            for (int i = 0; i < count; ++i) {
                if (i > 0) {
                    sb.append(',');
                }
                sb = _typeBindings.getBoundType(i).appendDesc(sb);
            }
            sb.append('>');
        }
        return sb;
    }
}
