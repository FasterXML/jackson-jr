package com.fasterxml.jackson.simple.ob;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.simple.ob.JSON.Feature;
import com.fasterxml.jackson.simple.ob.impl.SequenceComposer;

/**
 * Root-level composer object that acts as streaming "builder"
 * object, using an underlying {@link com.fasterxml.jackson.core.JsonGenerator} object.
 * This is similar to {@link com.fasterxml.jackson.simple.ob.impl.ArrayComposer}, but does not
 * have parent composer (so no <code>end()</code> method),
 * but does implement {@link java.io.Closeable}
 * 
 * @param <T> Type of result being composed.
 */
public class JSONComposer<T>
    extends SequenceComposer<JSONComposer<T>>
{
    protected final int _features;

    protected final boolean _closeGenerator;
    
    protected T _result;

    protected SegmentedStringWriter _stringWriter;
    
    protected ByteArrayBuilder _byteWriter;
    
    public JSONComposer(int features, JsonGenerator gen, boolean closeGenerator)
    {
        super(gen);
        _features = features;
        _stringWriter = null;
        _byteWriter = null;
        _closeGenerator = closeGenerator;
    }

    protected JSONComposer(int features, JsonGenerator gen, SegmentedStringWriter w)
    {
        super(gen);
        _features = features;
        _stringWriter = w;
        _byteWriter = null;
        _closeGenerator = true;
    }

    protected JSONComposer(int features, JsonGenerator gen, ByteArrayBuilder w)
    {
        super(gen);
        _features = features;
        _stringWriter = null;
        _byteWriter = w;
        _closeGenerator = true;
    }
    
    /*
    /**********************************************************************
    /* Extended API, factory methods
    /**********************************************************************
     */

    public static <RESULT> JSONComposer<RESULT> streamComposer(int features,
            JsonGenerator gen, boolean closeGenerator)
    {
        return new JSONComposer<RESULT>(features, gen, closeGenerator);
    }
    
    public static JSONComposer<String> stringComposer(int features,
            JsonGenerator gen, SegmentedStringWriter w) {
        return new JSONComposer<String>(features, gen, w);
    }

    public static JSONComposer<byte[]> bytesComposer(int features,
            JsonGenerator gen, ByteArrayBuilder w) {
        return new JSONComposer<byte[]>(features, gen, w);
    }
    
    /*
    /**********************************************************************
    /* Extended API, life-cycle
    /**********************************************************************
     */

    /**
     * Method to call to complete composition, flush any pending content,
     * and return instance of specified result type.
     */
    @SuppressWarnings("unchecked")
    public T finish() throws IOException, JsonProcessingException
    {
        if (_open) {
            _closeChild();
            _open = false;
            if (_closeGenerator) {
                _generator.close();
            } else if (Feature.FLUSH_AFTER_WRITE_VALUE.isEnabled(_features)) {
                _generator.flush();
            }
        }
        if (_result == null) {
            Object x;
            if (_stringWriter != null) {
                x = _stringWriter.getAndClear();
                _stringWriter = null;
            } else if (_byteWriter != null) {
                x = _byteWriter.toByteArray();
                _byteWriter = null;
            } else {
                x = _generator.getOutputTarget();
            }
            _result = (T) x;
        }
        return _result;
    }
    
    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    protected JSONComposer<T> _start() throws IOException, JsonProcessingException {
        // Should never be called
        throw _illegalCall();
    }

    @Override
    protected void _finish() throws IOException, JsonProcessingException {
        // Should never be called
        throw _illegalCall();
    }
}
