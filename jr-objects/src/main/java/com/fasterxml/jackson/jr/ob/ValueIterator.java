package com.fasterxml.jackson.jr.ob;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;

/**
 * Iterator exposed by {@link JSON} when binding sequence of
 * objects. Extension is done to allow more convenient exposing of
 * {@link IOException} (which basic {@link Iterator} does not expose).
 *<p>
 * NOTE: adapted from `jackson-databind` {@code MappingIterator}
 */
public class ValueIterator<T> implements Iterator<T>, Closeable
{
    /**
     * Mode in which values are read as POJOs/Beans.
     */
    protected final static int MODE_BEAN = 1;

    /**
     * Mode in which values are read as "Simple" content,
     * {@link java.util.Map}s, {@link java.util.List}s, {@link String}s,
     * {@link Number}s and {@link Boolean}s.
     */
    protected final static int MODE_ANY = 2;

    /**
     * Mode in which values are read as "Tree" values, as bound
     * by registered {@link TreeCodec}.
     */
    protected final static int MODE_TREE = 3;
    
    protected final static ValueIterator<?> EMPTY_ITERATOR =
            new ValueIterator<Object>(MODE_BEAN, null, null, null, null, false);
    
    /*
    /**********************************************************************
    /* State constants
    /**********************************************************************
     */

    /**
     * State in which iterator is closed
     */
    protected final static int STATE_CLOSED = 0;
    
    /**
     * State in which value read failed
     */
    protected final static int STATE_NEED_RESYNC = 1;
    
    /**
     * State in which no recovery is needed, but "hasNextValue()" needs
     * to be called first
     */
    protected final static int STATE_MAY_HAVE_VALUE = 2;

    /**
     * State in which "hasNextValue()" has been successfully called
     * and deserializer can be called to fetch value
     */
    protected final static int STATE_HAS_VALUE = 3;

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Mode: kind of values we are iterating over
     */
    protected final int _mode;
    
    /**
     * Type to bind individual elements to.
     */
    protected final Class<?> _type;

    /**
     * Context for deserialization, needed to pass through to deserializer
     */
    protected final JSONReader _reader;

    /**
     * If "Tree" values are read, codec we need to use for binding
     */
    protected final TreeCodec _treeCodec;
    
    /**
     * Underlying parser used for reading content to bind. Initialized
     * as not <code>null</code> but set as <code>null</code> when
     * iterator is closed, to denote closing.
     */
    protected final JsonParser _parser;

    /**
     * Context to resynchronize to, in case an exception is encountered
     * but caller wants to try to read more elements.
     */
    protected final JsonStreamContext _seqContext;

    /**
     * Flag that indicates whether input {@link JsonParser} should be closed
     * when we are done or not; generally only called when caller did not
     * pass JsonParser.
     */
    protected final boolean _closeParser;

    /*
    /**********************************************************************
    /* Parsing state
    /**********************************************************************
     */
    
    /**
     * State of the iterator
     */
    protected int _state;

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */
    
    /**
     * @param managedParser Whether we "own" the {@link JsonParser} passed or not:
     *   if true, it was created by {@code Json} and code here needs to
     *   close it; if false, it was passed by calling code and should not be
     *   closed by iterator.
     */
    protected ValueIterator(int mode, Class<?> type, JsonParser p, JSONReader reader,
            TreeCodec treeCodec, boolean managedParser)
    {
        _mode = mode;
        _type = type;
        _parser = p;
        _reader = reader;
        _treeCodec = treeCodec;
        _closeParser = managedParser;

        /* Ok: one more thing; we may have to skip START_ARRAY, assuming
         * "wrapped" sequence; but this is ONLY done for 'managed' parsers
         * and never if JsonParser was directly passed by caller (if it
         * was, caller must have either positioned it over first token of
         * the first element, or cleared the START_ARRAY token explicitly).
         * Note, however, that we do not try to guess whether this could be
         * an unwrapped sequence of arrays/Lists: we just assume it is wrapped;
         * and if not, caller needs to hand us JsonParser instead, pointing to
         * the first token of the first element.
         */
        if (p == null) { // can this occur?
            _seqContext = null;
            _state = STATE_CLOSED;
        } else {
            JsonStreamContext sctxt = p.getParsingContext();
            if (managedParser && p.isExpectedStartArrayToken()) {
                // If pointing to START_ARRAY, context should be that ARRAY
                p.clearCurrentToken();
            } else {
                // regardless, recovery context should be whatever context we have now, with
                // sole exception of pointing to a start marker, in which case it's the parent
                JsonToken t = p.getCurrentToken();
                if ((t == JsonToken.START_OBJECT) || (t == JsonToken.START_ARRAY)) {
                    sctxt = sctxt.getParent();
                }
            }
            _seqContext = sctxt;
            _state = STATE_MAY_HAVE_VALUE;
        }
    }

    @SuppressWarnings("unchecked")
    protected static <T> ValueIterator<T> emptyIterator() {
        return (ValueIterator<T>) EMPTY_ITERATOR;
    }

    /*
    /**********************************************************************
    /* Basic iterator impl
    /**********************************************************************
     */

    @Override
    public boolean hasNext()
    {
        try {
            return hasNextValue();
        } catch (JSONObjectException e) {
            return (Boolean) _handleMappingException(e);
        } catch (IOException e) {
            return (Boolean) _handleIOException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public T next()
    {
        try {
            return nextValue();
        } catch (JSONObjectException e) {
            return (T) _handleMappingException(e);
        } catch (IOException e) {
            return (T) _handleIOException(e);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void close() throws IOException {
        if (_state != STATE_CLOSED) {
            _state = STATE_CLOSED;
            if (_parser != null) {
                _parser.close();
            }
        }
    }

    /*
    /**********************************************************************
    /* Extended API, iteration
    /**********************************************************************
     */

    /**
     * Equivalent of {@link #next} but one that may throw checked
     * exceptions from Jackson due to invalid input.
     */
    public boolean hasNextValue() throws IOException
    {
        switch (_state) {
        case STATE_CLOSED:
            return false;
        case STATE_NEED_RESYNC:
            _resync();
            // fall-through
        case STATE_MAY_HAVE_VALUE:
            JsonToken t = _parser.getCurrentToken();
            if (t == null) { // un-initialized or cleared; find next
                t = _parser.nextToken();
                // If EOF, no more, or if we hit END_ARRAY (although we don't clear the token).
                if (t == null || t == JsonToken.END_ARRAY) {
                    _state = STATE_CLOSED;
                    if (_closeParser && (_parser != null)) {
                        _parser.close();
                    }
                    return false;
                }
            }
            _state = STATE_HAS_VALUE;
            return true;
        case STATE_HAS_VALUE:
            // fall through
        }
        return true;
    }

    public T nextValue() throws IOException
    {
        switch (_state) {
        case STATE_CLOSED:
            return _throwNoSuchElement();
        case STATE_NEED_RESYNC: // fall-through, will do re-sync
        case STATE_MAY_HAVE_VALUE:
            if (!hasNextValue()) {
                return _throwNoSuchElement();
            }
            break;
        case STATE_HAS_VALUE:
            break;
        }

        int nextState = STATE_NEED_RESYNC;
        try {
            Object value;

            switch (_mode) {
            case MODE_BEAN:
                value = _reader.readBean(_type);
                break;
            case MODE_ANY:
                value = _reader.readValue();
                break;
            case MODE_TREE:
                value = _treeCodec.readTree(_parser);
                break;
            default:
                throw new IllegalStateException("Invalid mode: "+_mode);
            }
            nextState = STATE_MAY_HAVE_VALUE;
            {
                @SuppressWarnings("unchecked")
                T result = (T) value;
                return result;
            }
        } finally {
            _state = nextState;
            // Need to mark token consumed no matter what, to avoid infinite loop for certain
            // failure cases.
            _parser.clearCurrentToken();
        }
    }

    /**
     * Convenience method for reading all entries accessible via
     * this iterator; resulting container will be a {@link java.util.ArrayList}.
     * 
     * @return List of entries read
     */
    public List<T> readAll() throws IOException {
        return readAll(new ArrayList<T>());
    }

    /**
     * Convenience method for reading all entries accessible via
     * this iterator
     * 
     * @return List of entries read (same as passed-in argument)
     */
    public <L extends List<? super T>> L readAll(L resultList) throws IOException
    {
        while (hasNextValue()) {
            resultList.add(nextValue());
        }
        return resultList;
    }

    /**
     * Convenience method for reading all entries accessible via this iterator
     */
    public <C extends Collection<? super T>> C readAll(C results) throws IOException
    {
        while (hasNextValue()) {
            results.add(nextValue());
        }
        return results;
    }

    /*
    /**********************************************************************
    /* Extended API, accessors
    /**********************************************************************
     */

    /**
     * Accessor for getting underlying parser this iterator uses.
     */
    public JsonParser getParser() {
        return _parser;
    }

    /**
     * Convenience method, functionally equivalent to:
     *<code>
     *   iterator.getParser().getCurrentLocation()
     *</code>
     * 
     * @return Location of the input stream of the underlying parser
     */
    public JsonLocation getCurrentLocation() {
        return _parser.getCurrentLocation();
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    protected void _resync() throws IOException
    {
        final JsonParser p = _parser;
        // First, a quick check to see if we might have been lucky and no re-sync needed
        if (p.getParsingContext() == _seqContext) {
            return;
        }

        while (true) {
            JsonToken t = p.nextToken();
            if ((t == JsonToken.END_ARRAY) || (t == JsonToken.END_OBJECT)) {
                if (p.getParsingContext() == _seqContext) {
                    p.clearCurrentToken();
                    return;
                }
            } else if ((t == JsonToken.START_ARRAY) || (t == JsonToken.START_OBJECT)) {
                p.skipChildren();
            } else if (t == null) {
                return;
            }
        }
    }

    protected <R> R _throwNoSuchElement() {
        throw new NoSuchElementException();
    }
    
    protected <R> R _handleMappingException(JSONObjectException e) {
        // Only with JDK8:
//        throw new UncheckedIOException(e.getMessage(), e);
        throw new RuntimeException(e.getMessage(), e);
    }

    protected <R> R _handleIOException(IOException e) {
        // Only with JDK8:
//        throw new UncheckedIOException(e.getMessage(), e);
        throw new RuntimeException(e.getMessage(), e);
    }
}
