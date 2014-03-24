package com.fasterxml.jackson.jr.type;

import java.util.*;

public abstract class ResolvedType implements java.lang.reflect.Type
{
    public final static ResolvedType[] NO_TYPES = new ResolvedType[0];
    
    protected final Class<?> _erasedType;
    protected final TypeBindings _bindings;

    protected ResolvedType(Class<?> cls, TypeBindings bindings) {
        _erasedType = cls;
        _bindings = (bindings == null) ? TypeBindings.emptyBindings() : bindings;
    }

    public Class<?> erasedType() { return _erasedType; }
    public ResolvedType parentType() { return null; }
    public ResolvedType selfRefType() { return null; }

    public List<ResolvedType> implInterfaces() { return Collections.emptyList(); }

    public List<ResolvedType> typeParams() { return _bindings.getTypeParameters(); }

    public TypeBindings getTypeBindings() { return _bindings; }
    
    /**
     * Method that will try to find type parameterization this type
     * has for specified super type
     * 
     * @return List of type parameters for specified supertype (which may
     *   be empty, if supertype is not a parametric type); null if specified
     *   type is not a super type of this type
     */
    public List<ResolvedType> typeParametersFor(Class<?> erasedSupertype) {
        ResolvedType type = findSupertype(erasedSupertype);
        if (type != null) {
            return type.typeParams();
        }
        return null;
    }

    /**
     * Method for finding super type of this type that has specified type
     * erased signature. If supertype is an interface which is implemented
     * using multiple inheritance paths, preference is given to interfaces
     * implemented "highest up the stack" (directly implemented interfaces
     * over interfaces superclass implements).
     */
    public ResolvedType findSupertype(Class<?> erasedSupertype) {
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

    public String getDesc() {
        StringBuilder sb = new StringBuilder();
        return appendDesc(sb).toString();
    }

    public abstract StringBuilder appendDesc(StringBuilder sb);

    @Override public String toString() { return getDesc(); }

    @Override public int hashCode() {
        return _erasedType.getName().hashCode() + _bindings.hashCode();
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
        return _bindings.equals(other._bindings);
    }

    protected StringBuilder _appendClassDesc(StringBuilder sb) {
        sb.append(_erasedType.getName());
        int count = _bindings.size();
        if (count > 0) {
            sb.append('<');
            for (int i = 0; i < count; ++i) {
                if (i > 0) {
                    sb.append(',');
                }
                sb = _bindings.getBoundType(i).appendDesc(sb);
            }
            sb.append('>');
        }
        return sb;
    }
}
