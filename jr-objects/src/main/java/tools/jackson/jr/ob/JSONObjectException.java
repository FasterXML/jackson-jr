package tools.jackson.jr.ob;

import java.io.Serializable;
import java.util.*;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonLocation;
import tools.jackson.core.JsonParser;

/**
 * Standard exception exposed by this package; equivalent of
 * {@code com.fasterxml.jackson.databind.DatabindException}
 * (and, in fact, much of implementation came from that class, but
 * had to be cut-n-pasted since we do not depend on databind package).
 */
public class JSONObjectException
    extends JacksonException
{
    private static final long serialVersionUID = 1L;


    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    public JSONObjectException(String msg) {
        super(msg);
    }

    public JSONObjectException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }

    public JSONObjectException(String msg, JsonLocation loc) {
        super(msg, loc, null);
    }

    public JSONObjectException(String msg, JsonLocation loc, Throwable rootCause) {
        super(msg, loc, rootCause);
    }

    public static JSONObjectException from(JsonParser p, String msg) {
        return new JSONObjectException(msg, ((p == null) ? null : p.currentTokenLocation()))
                .with(p);
    }

    public static JSONObjectException from(JsonParser p, String msg, Object... args) {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        return new JSONObjectException(msg, ((p == null) ? null : p.currentTokenLocation()))
                .with(p);
    }

    public static JSONObjectException from(JsonParser p, Throwable problem,
            String msg, Object... args)
    {
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        return new JSONObjectException(msg, ((p == null) ? null : p.currentTokenLocation()), problem)
                .with(p);
    }

    protected JSONObjectException with(JsonParser p) {
        _processor = p;
        return this;
    }

    /*
    /**********************************************************************
    /* Overridden methods
    /**********************************************************************
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
}
