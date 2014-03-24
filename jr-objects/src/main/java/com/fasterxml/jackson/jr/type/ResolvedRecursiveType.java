package com.fasterxml.jackson.jr.type;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Specialized type placeholder used in cases where type definition is
 * recursive; to avoid infinite loop, reference that would be "back" in
 * hierarchy is represented by an instance of this class.
 * Underlying information is achievable (for full resolution), but
 * not exposed using super type (parent) accessors; and has special
 * handling when used for constructing descriptions.
 */
public class ResolvedRecursiveType extends ResolvedType
{
    /**
     * Actual fully resolved type; assigned once resultion is complete
     */
    protected ResolvedType _referencedType;

    /*
    /**********************************************************************
    /* Life cycle
    /**********************************************************************
     */
    
    public ResolvedRecursiveType(Class<?> erased, TypeBindings bindings)
    {
        super(erased, bindings);
    }
    
    @Override
    public boolean canCreateSubtypes() {
        return _referencedType.canCreateSubtypes();
    }
    
    public void setReference(ResolvedType ref)
    {
        // sanity check; should not be called multiple times
        if (_referencedType != null) {
            throw new IllegalStateException("Trying to re-set self reference; old value = "+_referencedType+", new = "+ref);
        }
        _referencedType = ref;
    }
    

    /*
    /**********************************************************************
    /* Accessors for related types
    /**********************************************************************
     */
    
    /**
     * To avoid infinite loops, will return null;
     */
    @Override
    public ResolvedType getParentClass() {
        return null;
    }

    @Override
    public ResolvedType getSelfReferencedType() { return _referencedType; }
    
    /**
     * To avoid infinite loops, will return empty list
     */
    @Override
    public List<ResolvedType> getImplementedInterfaces() {
        return Collections.<ResolvedType>emptyList();
    }
    
    /**
     * To avoid infinite loops, will return null type
     */
    @Override
    public ResolvedType getArrayElementType() { // interfaces are never arrays, so:
        return null;
    }

    /*
    /**********************************************************************
    /* Simple property accessors
    /**********************************************************************
     */

    @Override
    public boolean isInterface() { return _erasedType.isInterface(); }

    @Override
    public boolean isAbstract() { return Modifier.isAbstract(_erasedType.getModifiers()); }

    @Override
    public boolean isArray() { return _erasedType.isArray(); }

    @Override
    public boolean isPrimitive() { return false; }
    
    /*
    /**********************************************************************
    /* String representations
    /**********************************************************************
     */

    @Override
    public StringBuilder appendSignature(StringBuilder sb) {
        // to avoid infinite recursion, only print type erased version
        return appendErasedSignature(sb);
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
        // should never get called, but just in case, only print brief description
        return appendBriefDescription(sb);
    }
}
