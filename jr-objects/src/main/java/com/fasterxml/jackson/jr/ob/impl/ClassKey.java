package com.fasterxml.jackson.jr.ob.impl;

/**
 * Key class, used as an efficient and accurate key
 * for locating per-class values from {@link java.util.Map}s.
 *<p>
 * The reason for having a separate key class instead of
 * directly using {@link Class} as key is mostly
 * to allow for redefining <code>hashCode</code> method --
 * for some strange reason, {@link Class} does not
 * redefine {@link Object#hashCode} and thus uses identity
 * hash, which is pretty slow. This makes key access using
 * {@link Class} unnecessarily slow.
 *<p>
 * Note: since class is not strictly immutable, caller must
 * know what it is doing, if changing field values.
 */
public final class ClassKey
{
    private String _className;

    private Class<?> _class;

    /**
     * Additional discriminator flags that may be used to allow
     * variations for same class.
     *
     * @since 2.8
     */
    private int _flags;

    /**
     * Let's cache hash code straight away, since we are
     * almost certain to need it.
     */
    private int _hashCode;

    public ClassKey() 
    {
        _class = null;
        _className = null;
        _flags = _hashCode = 0;
    }

    public ClassKey(Class<?> clz, int flags)
    {
        _class = clz;
        _flags = flags;
        _className = clz.getName();
        _hashCode = _className.hashCode() + flags;
    }

    public ClassKey with(Class<?> clz, int flags)
    {
        _class = clz;
        _className = clz.getName();
        _hashCode = _className.hashCode() + flags;
        _flags = flags;
        return this;
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        ClassKey other = (ClassKey) o;

        /* Is it possible to have different Class object for same name + class loader combo?
         * Let's assume answer is no: if this is wrong, will need to uncomment following functionality
         */
        /*
        return (other._flags == _flags)
            && (other._className.equals(_className))
            && (other._class.getClassLoader() == _class.getClassLoader());
        */
        return (other._flags == _flags) && (other._class == _class);
    }

    @Override public int hashCode() { return _hashCode; }

    @Override public String toString() { return _className; }
}
