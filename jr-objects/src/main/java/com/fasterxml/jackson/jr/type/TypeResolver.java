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

    private final static ResolvedType TYPE_OBJECT =
        new ResolvedType(Object.class, /* no super-class*/ null,
                null, null);

    protected final static HashMap<ClassKey, ResolvedType> _primitives;
    static {
        _primitives = new HashMap<ClassKey, ResolvedType>(16);

        ResolvedType[] all = new ResolvedType[] {
                new ResolvedType(Boolean.TYPE),
                new ResolvedType(Byte.TYPE),
                new ResolvedType(Short.TYPE),
                new ResolvedType(Character.TYPE),
                new ResolvedType(Integer.TYPE),
                new ResolvedType(Long.TYPE),
                new ResolvedType(Float.TYPE),
                new ResolvedType(Double.TYPE)
        };
        for (ResolvedType type : all) {
            _primitives.put(new ClassKey(type.erasedType(), 0), type);
        }
        _primitives.put(new ClassKey(Void.TYPE, 0), new ResolvedType(Void.TYPE));
        _primitives.put(new ClassKey(Object.class, 0), TYPE_OBJECT);
    }

    // // Caching
    
    protected final Map<ClassKey,ResolvedType> _cache = new HashMap<ClassKey,ResolvedType>(16, 0.8f);

    public TypeResolver() { }

    /**
     * Factory method for resolving specified Java {@link java.lang.reflect.Type}, given
     * {@link TypeBindings} needed to resolve any type variables.
     *<p>
     * Use of this method is discouraged (use if and only if you really know what you
     * are doing!); but if used, type bindings passed should come from {@link ResolvedType}
     * instance of declaring class (or interface).
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
            Object emptyArray = Array.newInstance(elementType.erasedType(), 0);
            return new ResolvedType(emptyArray.getClass(), typeBindings, elementType);
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
        final ClassKey key = new ClassKey(rawType, 0);
        if (rawType.isPrimitive()) {
            ResolvedType type = _primitives.get(key);
            if (type != null) {
                return type;
            }
        }
        if (context == null) {
            context = new ClassStack(rawType);
        } else {
            ClassStack prev = context.find(rawType);
            if (prev != null) {
                // Self-reference: needs special handling, then...
                RecursiveType selfRef = new RecursiveType(rawType, typeBindings);
                prev.addSelfReference(selfRef);
                return selfRef;
            }
            // no, can just add
            context = context.child(rawType);
        }

        ResolvedType type;
        if (typeBindings.isEmpty()) {
            synchronized (_cache) {
                type = _cache.get(key);
                if (type != null) {
                    return type;
                }
            }
            type = _constructType(context, rawType, typeBindings);
            synchronized (_cache) {
                if (_cache.size() >= 100) { // so hash table max 128 entries
                    _cache.clear();
                }
                _cache.put(key, type);
            }
        } else {
            type = _constructType(context, rawType, typeBindings);
        }
        context.resolveSelfReferences(type);
        return type;
    }

    private ResolvedType _constructType(ClassStack context, Class<?> rawType, TypeBindings typeBindings)
    {
        // Ok: no easy shortcut, let's figure out type of type...
        if (rawType.isArray()) {
            ResolvedType elementType = _fromAny(context, rawType.getComponentType(), typeBindings);
            return new ResolvedType(rawType, typeBindings, elementType);
        }
        // For other types super interfaces are needed...
        if (rawType.isInterface()) {
            return new ResolvedType(rawType, typeBindings,
                    _resolveSuperInterfaces(context, rawType, typeBindings));
            
        }
        return new ResolvedType(rawType, _resolveSuperClass(context, rawType, typeBindings),
                typeBindings,
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

    private ResolvedType _resolveSuperClass(ClassStack context, Class<?> rawType, TypeBindings typeBindings) {
        Type parent = rawType.getGenericSuperclass();
        if (parent == null) {
            return null;
        }
        return _fromAny(context, parent, typeBindings);
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
        if (typeBindings.hasUnbound(name)) {
            return TYPE_OBJECT;
        }

        typeBindings = typeBindings.withUnboundVariable(name);
        Type[] bounds = variable.getBounds();
        return _fromAny(context, bounds[0], typeBindings);
    }
}
