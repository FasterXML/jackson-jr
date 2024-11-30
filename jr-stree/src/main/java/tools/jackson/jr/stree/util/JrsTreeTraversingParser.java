package tools.jackson.jr.stree.util;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import tools.jackson.core.*;
import tools.jackson.core.base.ParserMinimalBase;
import tools.jackson.core.util.JacksonFeatureSet;

import tools.jackson.jr.stree.JrsArray;
import tools.jackson.jr.stree.JrsNumber;
import tools.jackson.jr.stree.JrsObject;
import tools.jackson.jr.stree.JrsValue;
import tools.jackson.jr.stree.PackageVersion;

/**
 * Facade over {@link JrsValue} that implements {@link JsonParser} to allow
 * accessing contents of JSON tree in alternate form (stream of tokens).
 * Useful when a streaming source is expected by code, such as data binding
 * functionality.
 */
public class JrsTreeTraversingParser extends ParserMinimalBase
{
    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Value node that this parser exposes as a token stream.
     */
    protected final JrsValue _source;

    /**
     * Traversal context within tree
     */
    protected JrsValueCursor _nodeCursor;

    /*
    /**********************************************************************
    /* State
    /**********************************************************************
     */

    /**
     * Sometimes parser needs to buffer a single look-ahead token; if so,
     * it'll be stored here. This is currently used for handling 
     */
    protected JsonToken _nextToken;

    /**
     * Flag needed to handle recursion into contents of child
     * Array/Object nodes.
     */
    protected boolean _startContainer;
    
    /**
     * Flag that indicates whether parser is closed or not. Gets
     * set when parser is either closed by explicit call
     * ({@link #close}) or when end-of-input is reached.
     */
    protected boolean _closed;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public JrsTreeTraversingParser(ObjectReadContext readCtxt, JrsValue n)
    {
        super(readCtxt);
        _source = n;
        if (n.isArray()) {
            _nextToken = JsonToken.START_ARRAY;
            _nodeCursor = new JrsValueCursor.ArrayCursor((JrsArray) n, null);
        } else if (n.isObject()) {
            _nextToken = JsonToken.START_OBJECT;
            _nodeCursor = new JrsValueCursor.ObjectCursor((JrsObject) n, null);
        } else { // value node
            _nodeCursor = new JrsValueCursor.RootCursor(n, null);
        }
    }

    /*
    /**********************************************************************
    /* Config access, capability introspection
    /**********************************************************************
     */

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public JacksonFeatureSet<StreamReadCapability> streamReadCapabilities() {
        // Defaults are fine
        return DEFAULT_READ_CAPABILITIES;
    }

    @Override
    public Object streamReadInputSource() {
        return _source;
    }

    /*
    /**********************************************************************
    /* Closeable implementation
    /**********************************************************************
     */

    @Override
    public void close()
    {
        if (!_closed) {
            _closed = true;
            _nodeCursor = null;
            _currToken = null;
        }
    }

    @Override
    protected void _closeInput() { }

    @Override
    protected void _releaseBuffers() { }

    /*
    /**********************************************************************
    /* Public API, traversal
    /**********************************************************************
     */

    @Override
    public JsonToken nextToken() throws JacksonException
    {
        if (_nextToken != null) {
            _currToken = _nextToken;
            _nextToken = null;
            return _currToken;
        }
        // are we to descend to a container child?
        if (_startContainer) {
            _startContainer = false;
            // minor optimization: empty containers can be skipped
            if (!_nodeCursor.currentHasChildren()) {
                _currToken = (_currToken == JsonToken.START_OBJECT) ?
                    JsonToken.END_OBJECT : JsonToken.END_ARRAY;
                return _currToken;
            }
            _nodeCursor = _nodeCursor.iterateChildren();
            _currToken = _nodeCursor.nextToken();
            if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
                _startContainer = true;
            }
            return _currToken;
        }
        // No more content?
        if (_nodeCursor == null) {
            _closed = true; // if not already set
            return null;
        }
        // Otherwise, next entry from current cursor
        _currToken = _nodeCursor.nextToken();
        if (_currToken != null) {
            if (_currToken == JsonToken.START_OBJECT || _currToken == JsonToken.START_ARRAY) {
                _startContainer = true;
            }
            return _currToken;
        }
        // null means no more children; need to return end marker
        _currToken = _nodeCursor.endToken();
        _nodeCursor = _nodeCursor.getParent();
        return _currToken;
    }
    
    // default works well here:
    //public JsonToken nextValue() throws JacksonException

    @Override
    public JsonParser skipChildren() throws JacksonException
    {
        if (_currToken == JsonToken.START_OBJECT) {
            _startContainer = false;
            _currToken = JsonToken.END_OBJECT;
        } else if (_currToken == JsonToken.START_ARRAY) {
            _startContainer = false;
            _currToken = JsonToken.END_ARRAY;
        }
        return this;
    }

    @Override
    public boolean isClosed() {
        return _closed;
    }

    /*
    /**********************************************************************
    /* Public API, token accessors
    /**********************************************************************
     */

    @Override
    public String currentName() {
        return (_nodeCursor == null) ? null : _nodeCursor.currentName();
    }

    @Override
    public TokenStreamContext streamReadContext() {
        return _nodeCursor;
    }

    @Override public void assignCurrentValue(Object v) { _nodeCursor.assignCurrentValue(v); }
    @Override public Object currentValue() { return _nodeCursor.currentValue(); }

    @Override
    public TokenStreamLocation currentTokenLocation() {
        return TokenStreamLocation.NA;
    }

    @Override
    public TokenStreamLocation currentLocation() {
        return TokenStreamLocation.NA;
    }

    /*
    /**********************************************************************
    /* Public API, access to textual content
    /**********************************************************************
     */

    @Override
    public String getText()
    {
        if (_closed) {
            return null;
        }
        // need to separate handling a bit...
        switch (_currToken) {
        case PROPERTY_NAME:
            return _nodeCursor.currentName();
        case VALUE_STRING:
        case VALUE_NUMBER_INT:
        case VALUE_NUMBER_FLOAT:
            return currentNode().asText();
// 28-Dec-2015, tatu: Not yet supported:
//        case VALUE_EMBEDDED_OBJECT:
        default:
        	return (_currToken == null) ? null : _currToken.asString();
        }
    }

    @Override
    public char[] getTextCharacters() throws JacksonException {
        return getText().toCharArray();
    }

    @Override
    public int getTextLength() throws JacksonException {
        return getText().length();
    }

    @Override
    public int getTextOffset() throws JacksonException {
        return 0;
    }

    @Override
    public boolean hasTextCharacters() {
        // generally we do not have efficient access as char[], hence:
        return false;
    }
    
    /*
    /**********************************************************************
    /* Public API, typed non-text access
    /**********************************************************************
     */

    //public byte getByteValue() throws JacksonException

    @Override
    public NumberType getNumberType() {
        JrsValue n = currentNumericNode();
        return (n == null) ? null : n.numberType();
    }

    @Override
    public BigInteger getBigIntegerValue() throws JacksonException
    {
        return currentNumericNode().asBigInteger();
    }

    @Override
    public BigDecimal getDecimalValue() throws JacksonException {
        return currentNumericNode().asBigDecimal();
    }

    @Override
    public double getDoubleValue() throws JacksonException {
        return currentNumericValue().doubleValue();
    }

    @Override
    public float getFloatValue() throws JacksonException {
        return (float) currentNumericValue().doubleValue();
    }

    @Override
    public long getLongValue() throws JacksonException {
        return currentNumericValue().longValue();
    }

    @Override
    public int getIntValue() throws JacksonException {
        return currentNumericValue().intValue();
    }

    @Override
    public Number getNumberValue() throws JacksonException {
        return currentNumericValue();
    }

    @Override
    public boolean isNaN() {
        return currentNumericNode().isNaN();
    }

    @Override
    public Object getEmbeddedObject() {
        // 28-Dec-2015, tatu: Embedded values ("POJO nodes") not yet supported
        return null;
    }

    /*
    /**********************************************************************
    /* Public API, typed binary (base64) access
    /**********************************************************************
     */

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws JacksonException {
        // 28-Dec-2015, tatu: Binary nodes not yet supported
        return null;
    }


    @Override
    public int readBinaryValue(Base64Variant b64variant, OutputStream out) throws JacksonException {
        // 28-Dec-2015, tatu: Binary nodes not yet supported
        return -1;
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected JrsValue currentNode() {
        if (_closed || _nodeCursor == null) {
            return null;
        }
        return _nodeCursor.currentNode();
    }

    protected JrsNumber currentNumericNode()
    {
        JrsValue n = currentNode();
        if ((n == null) || !(n instanceof JrsNumber)) {
            JsonToken t = (n == null) ? null : n.asToken();
            throw _constructReadException("Current token (%s) not numeric, can not use numeric value accessors", t);
        }
        return (JrsNumber) n;
    }

    protected Number currentNumericValue() {
        return currentNumericNode().getValue();
    }
    
    @Override
    protected void _handleEOF() {
        _throwInternal(); // should never get called
    }
}
