package com.fasterxml.jackson.simple.ob;

import com.fasterxml.jackson.core.JsonParser;

/**
 * Context object used on per-call basis, to carry along read configuration
 * and state that is needed to handle JSON-to-Objects conversions.
 */
public final class JSONReadContext
{
    public final JsonParser parser;

    public JSONReadContext(JsonParser p)
    {
        parser = p;
    }
}
