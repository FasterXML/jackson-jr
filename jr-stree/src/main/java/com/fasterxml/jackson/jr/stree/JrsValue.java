package com.fasterxml.jackson.jr.stree;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.jr.stree.util.JrsTreeTraversingParser;

/**
 * Shared base class for all "simple" node types of Jackson Jr
 * "simple tree" package; implements {@link TreeNode} and is usable
 * via matching {@link com.fasterxml.jackson.core.TreeCodec}
 * implementation (see {@link JacksonJrsTreeCodec}).
 */
public abstract class JrsValue implements TreeNode
{
    @Override
    public JsonParser.NumberType numberType() {
        return null;
    }

    @Override
    public boolean isMissingNode() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isNull() {
        return false;
    }
    
    @Override
    public boolean isEmbeddedValue() {
        return false;
    }
    
    @Override
    public Iterator<String> fieldNames() {
        return null;
    }

    @Override
    public JrsValue at(JsonPointer ptr)
    {
        if (ptr.matches()) {
            return this;
        }
        JrsValue n = _at(ptr);
        while (true) {
            if (n == null) {
                return JrsMissing.instance();
            }
            ptr = ptr.tail();
            if (ptr.matches()) {
                return n;
            }
            n = n._at(ptr);
        }
    }

    @Override
    public JrsValue at(String s) {
        return at(JsonPointer.compile(s));
    }
    
    @Override
    public JsonParser traverse(ObjectReadContext ctxt) {
        return new JrsTreeTraversingParser(ctxt, this);
    }

    /*
    /**********************************************************************
    /* Extended API
    /**********************************************************************
     */

    /**
     * Method that allows checking whether this value is a JSON number
     * (integer or floating-point).
     */
    public boolean isNumber() {
        return false;
    }

    /**
     * Method that may be called on scalar value nodes to get a textual
     * representation of contents. Returns `null` for structured values
     * (Arrays, Objects), textual representation for other types.
     */
    public String asText() {
        return null;
    }

    /*
    /**********************************************************************
    /* Abstract methods for sub-classes
    /**********************************************************************
     */

    @Override
    public abstract JrsValue get(String s);

    @Override
    public abstract JrsValue get(int i);

    @Override
    public abstract JrsValue path(String s);

    @Override
    public abstract JrsValue path(int i);

    protected abstract JrsValue _at(JsonPointer ptr);

    protected abstract void write(JsonGenerator g, JacksonJrsTreeCodec codec) throws IOException;

    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */
    
    /**
     * Intermediate base class for non-structured types, other than
     * {@link JrsMissing}.
     */
    public static abstract class Scalar extends JrsValue
    {
        @Override
        public final boolean isValueNode() {
            return true;
        }

        @Override
        public final boolean isContainerNode() {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public JrsValue get(String s) {
            return null;
        }

        @Override
        public JrsValue get(int i) {
            return null;
        }

        @Override
        public JrsValue path(String s) {
            return JrsMissing.instance();
        }

        @Override
        public JrsValue path(int i) {
            return JrsMissing.instance();
        }
        
        @Override
        protected JrsValue _at(JsonPointer ptr) {
            // will only allow direct matches, but no traversal through
            // (base class checks for direct match)
            return null;
        }
    }
}
