package com.fasterxml.jackson.jr.ob;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Standard exception exposed by this package; equivalent of
 * <code>com.fasterxml.jackson.databind.JsonMappingException</code>
 * (and, in fact, much of implementation came from that class, but
 * had to be cut-n-pasted since we do not depend on databind package).
 */
public class JSONObjectException
    extends JsonProcessingException
{
    private static final long serialVersionUID = 1L;

    /**
     * Let's limit length of reference chain, to limit damage in cases
     * of infinite recursion.
     */
    final static int MAX_REFS_TO_LIST = 250;

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Simple bean class used to contain references. References
     * can be added to indicate execution/reference path that
     * lead to the problem that caused this exception to be
     * thrown.
     */
    public static class Reference implements Serializable
    {
        private static final long serialVersionUID = 1L;

        /**
         * Object through which reference was resolved. Can be either
         * actual instance (usually the case for serialization), or
         * Class (usually the case for deserialization).
         */
        protected Object _from;

        /**
         * Name of field (for beans) or key (for Maps) that is part
         * of the reference. May be null for Collection types (which
         * generally have {@link #_index} defined), or when resolving
         * Map classes without (yet) having an instance to operate on.
         */
        protected String _fieldName;

        /**
         * Index within a {@link Collection} instance that contained
         * the reference; used if index is relevant and available.
         * If either not applicable, or not available, -1 is used to
         * denote "not known".
         */
        protected int _index = -1;

        /**
         * Default constructor for deserialization/sub-classing purposes
         */
        protected Reference() { }

        public Reference(Object from) { _from = from; }

        public Reference(Object from, String fieldName) {
            _from = from;
            if (fieldName == null) {
                throw new NullPointerException("Can not pass null fieldName");
            }
            _fieldName = fieldName;
        }

        public Reference(Object from, int index) {
            _from = from;
            _index = index;
        }

        public void setFrom(Object o) { _from = o; }
        public void setFieldName(String n) { _fieldName = n; }
        public void setIndex(int ix) { _index = ix; }

        public Object getFrom() { return _from; }
        public String getFieldName() { return _fieldName; }
        public int getIndex() { return _index; }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder();
            Class<?> cls = (_from instanceof Class<?>) ?
                ((Class<?>)_from) : _from.getClass();
            /* Hmmh. Although Class.getName() is mostly ok, it does look
             * butt-ugly for arrays. So let's use getSimpleName() instead;
             * but have to prepend package name too.
             */
            Package pkg = cls.getPackage();
            if (pkg != null) {
                sb.append(pkg.getName());
                sb.append('.');
            }
            sb.append(cls.getSimpleName());
            sb.append('[');
            if (_fieldName != null) {
                sb.append('"');
                sb.append(_fieldName);
                sb.append('"');
            } else if (_index >= 0) {
                sb.append(_index);
            } else {
                sb.append('?');
            }
            sb.append(']');
            return sb.toString();
        }
    }

    /*
    /**********************************************************
    /* State/configuration
    /**********************************************************
     */

    /**
     * Path through which problem that triggering throwing of
     * this exception was reached.
     */
    protected LinkedList<Reference> _path;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public JSONObjectException(String msg) {
        super(msg);
    }

    public JSONObjectException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }

    public JSONObjectException(String msg, JsonLocation loc) {
        super(msg, loc);
    }

    public JSONObjectException(String msg, JsonLocation loc, Throwable rootCause) {
        super(msg, loc, rootCause);
    }

    public static JSONObjectException from(JsonParser p, String msg) {
        return new JSONObjectException(msg, ((p == null) ? null : p.getTokenLocation()));
    }

    public static JSONObjectException from(JsonParser p, String msg, Object... args) {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        return new JSONObjectException(msg, ((p == null) ? null : p.getTokenLocation()));
    }

    public static JSONObjectException from(JsonParser p, Throwable problem,
            String msg, Object... args)
    {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        return new JSONObjectException(msg, ((p == null) ? null : p.getTokenLocation()), problem);
    }
    
    /**
     * Factory method used when "upgrading" an {@link IOException} into
     * {@link JSONObjectException}: usually only needed to comply with
     * a signature.
     */
    public static JSONObjectException fromUnexpectedIOE(IOException src)
    {
        return new JSONObjectException("Unexpected IOException (of type "
                +src.getClass().getName()+"): "+src.getMessage(), (JsonLocation)null, src);
    }
    
    /**
     * Method that can be called to either create a new JsonMappingException
     * (if underlying exception is not a JsonMappingException), or augment
     * given exception with given path/reference information.
     *
     * This version of method is called when the reference is through a
     * non-indexed object, such as a Map or POJO/bean.
     */
    public static JSONObjectException wrapWithPath(Throwable src, Object refFrom,
                                                    String refFieldName)
    {
        return wrapWithPath(src, new Reference(refFrom, refFieldName));
    }

    /**
     * Method that can be called to either create a new JsonMappingException
     * (if underlying exception is not a JsonMappingException), or augment
     * given exception with given path/reference information.
     *
     * This version of method is called when the reference is through an
     * index, which happens with arrays and Collections.
     */
    public static JSONObjectException wrapWithPath(Throwable src, Object refFrom,
                                                    int index)
    {
        return wrapWithPath(src, new Reference(refFrom, index));
    }

    /**
     * Method that can be called to either create a new JsonMappingException
     * (if underlying exception is not a JsonMappingException), or augment
     * given exception with given path/reference information.
     */
    public static JSONObjectException wrapWithPath(Throwable src, Reference ref)
    {
        JSONObjectException jme;
        if (src instanceof JSONObjectException) {
            jme = (JSONObjectException) src;
        } else {
            String msg = src.getMessage();
            if (msg == null || msg.length() == 0) {
                msg = "(was "+src.getClass().getName()+")";
            }
            jme = new JSONObjectException(msg, null, src);
        }
        jme.prependPath(ref);
        return jme;
    }
    
    /*
    /**********************************************************
    /* Accessors/mutators
    /**********************************************************
     */

    /**
     * Method for accessing full structural path within type hierarchy
     * down to problematic property.
     */
    public List<Reference> getPath()
    {
        if (_path == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(_path);
    }

    /**
     * Method for accesing description of path that lead to the
     * problem that triggered this exception
     */
    public String getPathReference()
    {
        return getPathReference(new StringBuilder()).toString();
    }

    public StringBuilder getPathReference(StringBuilder sb)
    {
        _appendPathDesc(sb);
        return sb;
    }
    
    /**
     * Method called to prepend a reference information in front of
     * current path
     */
    public void prependPath(Object referrer, String fieldName)
    {
        Reference ref = new Reference(referrer, fieldName);
        prependPath(ref);
    }
    /**
     * Method called to prepend a reference information in front of
     * current path
     */
    public void prependPath(Object referrer, int index)
    {
        Reference ref = new Reference(referrer, index);
        prependPath(ref);
    }

    public void prependPath(Reference r)
    {
        if (_path == null) {
            _path = new LinkedList<Reference>();
        }
        /* Also: let's not increase without bounds. Could choose either
         * head or tail; tail is easier (no need to ever remove), as
         * well as potentially more useful so let's use it:
         */
        if (_path.size() < MAX_REFS_TO_LIST) {
            _path.addFirst(r);
        }
    }
    
    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    @Override
    public String getLocalizedMessage() {
        return _buildMessage();
    }
    
    /**
     * Method is overridden so that we can properly inject description
     * of problem path, if such is defined.
     */
    @Override
    public String getMessage() {
        return _buildMessage();
    }

    protected String _buildMessage()
    {
        String msg = super.getMessage();
        if (_path == null) {
            return msg;
        }
        StringBuilder sb = (msg == null) ? new StringBuilder() : new StringBuilder(msg);
        sb.append(" (through reference chain: ");
        sb = getPathReference(sb);
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return getClass().getName()+": "+getMessage();
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    protected void _appendPathDesc(StringBuilder sb)
    {
        if (_path == null) {
            return;
        }
        Iterator<Reference> it = _path.iterator();
        while (it.hasNext()) {
            sb.append(it.next().toString());
            if (it.hasNext()) {
                sb.append("->");
            }
        }
    }
}
