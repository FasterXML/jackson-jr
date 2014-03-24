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
    
    public ResolvedRecursiveType(Class<?> erased, TypeBindings bindings) {
        super(erased, bindings);
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

    @Override
    public ResolvedType getSelfReferencedType() { return _referencedType; }
    
    /**
     * To avoid infinite loops, will return empty list
     */
    @Override
    public List<ResolvedType> getImplementedInterfaces() {
        return Collections.<ResolvedType>emptyList();
    }

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
