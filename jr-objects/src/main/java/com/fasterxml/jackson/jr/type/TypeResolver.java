package com.fasterxml.jackson.jr.type;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

import com.fasterxml.jackson.jr.ob.impl.ClassKey;

/**
 * Object that is used for resolving generic type information of a class
 * so that it is accessible using simple API. Resolved types are also starting
 * point for accessing resolved (generics aware) return and argument types
 * of class members (methods, fields, constructors).
 *<p>
 * Note that resolver instances are stateful in that resolvers cache resolved
 * types for efficiency. Since this is internal state and not directly visible
 * to callers, access to state is fully synchronized so that access from
 * multiple threads is safe.
 */
@SuppressWarnings("serial")
public class TypeResolver implements Serializable
{
    private final static ResolvedType[] NO_TYPES = new ResolvedType[0];
    
    // // Pre-created instances

    private final static ResolvedObjectType sJavaLangObject =
        new ResolvedObjectType(Object.class, null, null, NO_TYPES);

    protected final static HashMap<ClassKey, ResolvedType> _primitiveTypes;
    static {
        _primitiveTypes = new HashMap<ClassKey, ResolvedType>(16);
        for (ResolvedPrimitiveType type : ResolvedPrimitiveType.all()) {
            _primitiveTypes.put(new ClassKey(type.getErasedType()), type);
        }
        // should we include "void"? might as well...
        _primitiveTypes.put(new ClassKey(Void.TYPE), ResolvedPrimitiveType.voidType());
        // and at least java.lang.Object should be added too.
        _primitiveTypes.put(new ClassKey(Object.class), sJavaLangObject);
        // but most other types can be added dynamically
    }

    // // Caching
    
    protected final ResolvedTypeCache _resolvedTypes = new ResolvedTypeCache(200);

    public TypeResolver() { }

    /**
     * Factory method for resolving specified Java {@link java.lang.reflect.Type}, given
     * {@link TypeBindings} needed to resolve any type variables.
     *<p>
     * Use of this method is discouraged (use if and only if you really know what you
     * are doing!); but if used, type bindings passed should come from {@link ResolvedType}
     * instance of declaring class (or interface).
     *<p>
     * NOTE: order of arguments was reversed for 0.8, to avoid problems with
     * overload varargs method.
     */
    public ResolvedType resolve(TypeBindings typeBindings, Type jdkType) {
        return _fromAny(null, jdkType, typeBindings);
    }

    private ResolvedType _fromAny(ClassStack context, Type mainType, TypeBindings typeBindings) {
        if (mainType instanceof Class<?>) {
            return _fromClass(context, (Class<?>) mainType, typeBindings);
        }
        if (mainType instanceof ResolvedType) {
            return (ResolvedType) mainType;
        }
        if (mainType instanceof ParameterizedType) {
            return _fromParamType(context, (ParameterizedType) mainType, typeBindings);
        }
        if (mainType instanceof GenericArrayType) {
            ResolvedType elementType = _fromAny(context, ((GenericArrayType) mainType).getGenericComponentType(), typeBindings);
            // Figuring out raw class for generic array is actually bit tricky...
            Object emptyArray = Array.newInstance(elementType.getErasedType(), 0);
            return new ResolvedArrayType(emptyArray.getClass(), typeBindings, elementType);
        }
        if (mainType instanceof TypeVariable<?>) {
            return _fromVariable(context, (TypeVariable<?>) mainType, typeBindings);
        }
        if (mainType instanceof WildcardType) {
            return _fromAny(context, ((WildcardType) mainType).getUpperBounds()[0], typeBindings);
        }
        // should never get here...
        throw new IllegalArgumentException("Unrecognized type class: "+mainType.getClass().getName());
    }

    private ResolvedType _fromClass(ClassStack context, Class<?> rawType, TypeBindings typeBindings) {
        // First: a primitive type perhaps?
        ResolvedType type = _primitiveTypes.get(new ClassKey(rawType));
        if (type != null) {
            return type;
        }
        // Second: recursive reference?
        if (context == null) {
            context = new ClassStack(rawType);
        } else {
            ClassStack prev = context.find(rawType);
            if (prev != null) {
                // Self-reference: needs special handling, then...
                ResolvedRecursiveType selfRef = new ResolvedRecursiveType(rawType, typeBindings);
                prev.addSelfReference(selfRef);
                return selfRef;
            }
            // no, can just add
            context = context.child(rawType);
        }
        
        // If not, already recently resolved?
        ResolvedType[] typeParameters = typeBindings.typeParameterArray();
        ResolvedTypeCache.Key key = _resolvedTypes.key(rawType, typeParameters);
                
        type = _resolvedTypes.find(key);
        if (type == null) {
            type = _constructType(context, rawType, typeBindings);
            _resolvedTypes.put(key, type);
        }
        context.resolveSelfReferences(type);
        return type;
    }

    private ResolvedType _constructType(ClassStack context, Class<?> rawType, TypeBindings typeBindings)
    {
        // Ok: no easy shortcut, let's figure out type of type...
        if (rawType.isArray()) {
            ResolvedType elementType = _fromAny(context, rawType.getComponentType(), typeBindings);
            return new ResolvedArrayType(rawType, typeBindings, elementType);
        }
        // For other types super interfaces are needed...
        if (rawType.isInterface()) {
            return new ResolvedInterfaceType(rawType, typeBindings,
                    _resolveSuperInterfaces(context, rawType, typeBindings));
            
        }
        return new ResolvedObjectType(rawType, typeBindings,
                _resolveSuperClass(context, rawType, typeBindings),
                _resolveSuperInterfaces(context, rawType, typeBindings));
    }

    private ResolvedType[] _resolveSuperInterfaces(ClassStack context, Class<?> rawType, TypeBindings typeBindings) {
        Type[] types = rawType.getGenericInterfaces();
        if (types == null || types.length == 0) {
            return NO_TYPES;
        }
        int len = types.length;
        ResolvedType[] resolved = new ResolvedType[len];
        for (int i = 0; i < len; ++i) {
            resolved[i] = _fromAny(context, types[i], typeBindings);
        }
        return resolved;
    }

    private ResolvedObjectType _resolveSuperClass(ClassStack context, Class<?> rawType, TypeBindings typeBindings) {
        Type parent = rawType.getGenericSuperclass();
        if (parent == null) {
            return null;
        }
        ResolvedType rt = _fromAny(context, parent, typeBindings);
        // can this ever be something other than class? (primitive, array)
        return (ResolvedObjectType) rt;
    }
    
    private ResolvedType _fromParamType(ClassStack context, ParameterizedType ptype, TypeBindings parentBindings)
    {
        Class<?> rawType = (Class<?>) ptype.getRawType();
        Type[] params = ptype.getActualTypeArguments();
        int len = params.length;
        ResolvedType[] types = new ResolvedType[len];

        for (int i = 0; i < len; ++i) {
            types[i] = _fromAny(context, params[i], parentBindings);
        }
        TypeBindings newBindings = TypeBindings.create(rawType, types);
        return _fromClass(context, rawType, newBindings);
    }

    private ResolvedType _fromVariable(ClassStack context, TypeVariable<?> variable, TypeBindings typeBindings) {
        String name = variable.getName();
        ResolvedType type = typeBindings.findBoundType(name);
        if (type != null) {
            return type;
        }
        typeBindings = typeBindings.withAdditionalBinding(name, sJavaLangObject);
        Type[] bounds = variable.getBounds();
        return _fromAny(context, bounds[0], typeBindings);
    }

    private final static class ClassStack
    {
        private final ClassStack _p;
        private final Class<?> _curr;
        
        private ArrayList<ResolvedRecursiveType> _selfRefs;
        
        public ClassStack(Class<?> rootType) {
            this(null, rootType);
        }
        
        private ClassStack(ClassStack parent, Class<?> curr) {
            _p = parent;
            _curr = curr;
        }

        public ClassStack child(Class<?> cls) {
            return new ClassStack(this, cls);
        }

        public void addSelfReference(ResolvedRecursiveType ref) {
            if (_selfRefs == null) {
                _selfRefs = new ArrayList<ResolvedRecursiveType>();
            }
            _selfRefs.add(ref);
        }

        public void resolveSelfReferences(ResolvedType resolved) {
            if (_selfRefs != null) {
                for (ResolvedRecursiveType ref : _selfRefs) {
                    ref.setReference(resolved);
                }
            }
        }
        
        public ClassStack find(Class<?> cls) {
            if (_curr == cls) return this;
            if (_p != null) {
                return _p.find(cls);
            }
            return null;
        }
    }
}
