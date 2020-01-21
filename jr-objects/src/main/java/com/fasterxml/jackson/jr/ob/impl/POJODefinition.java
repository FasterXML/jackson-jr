package com.fasterxml.jackson.jr.ob.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Definition of a single Bean-style Java class, without assumptions
 * on usage for serialization or deserialization, used as input
 * for constructing bean readers and writers.
 *
 * @since 2.8
 */
public class POJODefinition
{
    protected final Class<?> _type;

    protected final Prop[] _properties;

    /**
     * Possible per-class definition of names that may be ignored safely
     * during deserialization.
     *
     * @since 2.11
     */
    protected final Set<String> _ignorableNames;

    public final Constructor<?> defaultCtor;
    public final Constructor<?> stringCtor;
    public final Constructor<?> longCtor;

    public POJODefinition(Class<?> type, Prop[] props,
            Constructor<?> defaultCtor0, Constructor<?> stringCtor0, Constructor<?> longCtor0)
    {
        _type = type;
        _properties = props;
        defaultCtor = defaultCtor0;
        stringCtor = stringCtor0;
        longCtor = longCtor0;
        _ignorableNames = null;
    }

    protected POJODefinition(POJODefinition base,
            Prop[] props, Set<String> ignorableN)
    {
        _type = base._type;
        _properties = props;
        defaultCtor = base.defaultCtor;
        stringCtor = base.stringCtor;
        longCtor = base.longCtor;
        _ignorableNames = ignorableN;
    }

    public POJODefinition withProperties(Collection<Prop> props) {
        return new POJODefinition(this, props.toArray(new Prop[0]), _ignorableNames);
    }

    // @since 2.11
    public POJODefinition withIgnorals(Set<String> ignorableN) {
        return new POJODefinition(this, _properties, ignorableN);
    }

    /*
    /**********************************************************************
    /* Public API
    /**********************************************************************
     */

    public List<Prop> getProperties() {
        return Arrays.asList(_properties);
    }

    @Deprecated // in 2.11, will be removed soon
    public Prop[] properties() {
        return _properties;
    }

    public Set<String> getIgnorableNames() {
        if (_ignorableNames == null) {
            return Collections.emptySet();
        }
        return _ignorableNames;
    }

    /*
    /**********************************************************************
    /* Helper class for containing property definitions
    /**********************************************************************
     */

    public static final class Prop
    {
        public final String name;

        public final Field field;
        public final Method setter, getter, isGetter;

        public Prop(String n, Field f,
                Method setter0, Method getter0, Method isGetter0) {
            name = n;
            field = f;
            setter = setter0;
            getter = getter0;
            isGetter = isGetter0;
        }

        public static PropBuilder builder(String name) {
            return new PropBuilder(name);
        }

        public boolean hasSetter() {
            return (setter != null) || (field != null);
        }
    }

    static final class PropBuilder {
        private final String _name;

        private Field _field;
        private Method _setter, _getter, _isGetter;
        
        public PropBuilder(String name) {
            _name = name;
        }

        public Prop build() {
            return new Prop(_name, _field, _setter, _getter, _isGetter);
        }

        public PropBuilder withField(Field f) {
            _field = f;
            return this;
        }

        public PropBuilder withSetter(Method m) {
            _setter = m;
            return this;
        }

        public PropBuilder withGetter(Method m) {
            _getter = m;
            return this;
        }

        public PropBuilder withIsGetter(Method m) {
            _isGetter = m;
            return this;
        }
    }
}

