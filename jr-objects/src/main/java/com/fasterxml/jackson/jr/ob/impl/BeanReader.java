package com.fasterxml.jackson.jr.ob.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.api.ValueReader;

/**
 * Class that contains information about dynamically introspected
 * Bean types, to be able to deserialize (read) JSON into a POJO
 * and serialize (write) POJO as JSON.
 */
public class BeanReader
    extends ValueReader // so we can chain calls for Collections, arrays
{
    protected final Map<String, BeanPropertyReader> _propsByName;

    /**
     * Mapping of aliased names to primary names (direct linkage unfortunately
     * impractical due to implementation limits).
     *
     * @since 2.11
     */
    protected final Map<String, String> _aliasMapping;

    // @since 2.11
    protected final Set<String> _ignorableNames;

    /**
     * @since 2.17
     */
    protected final BeanConstructors _constructors;

    protected final boolean _isRecordType;

    /**
     * Constructors used for deserialization use case
     *
     * @since 2.17
     */
    public BeanReader(Class<?> type, Map<String, BeanPropertyReader> props,
            BeanConstructors constructors,
            Set<String> ignorableNames, Map<String, String> aliasMapping)
    {
        super(type);
        _propsByName = props;
        _constructors = constructors;
        if (ignorableNames == null) {
            ignorableNames = Collections.<String>emptySet();
        }
        _ignorableNames = ignorableNames;
        if (aliasMapping == null) {
            aliasMapping = Collections.emptyMap();
        }
        _aliasMapping = aliasMapping;
        _isRecordType = RecordsHelpers.isRecordType(type);
    }

    @Deprecated // since 2.17
    public BeanReader(Class<?> type, Map<String, BeanPropertyReader> props,
            Constructor<?> defaultCtor, Constructor<?> stringCtor, Constructor<?> longCtor,
            Set<String> ignorableNames, Map<String, String> aliasMapping)
    {
        this(type, props, new BeanConstructors(type).addNoArgsConstructor(defaultCtor)
                .addStringConstructor(stringCtor)
                .addLongConstructor(longCtor),
                ignorableNames, aliasMapping);
    }

    public Map<String,BeanPropertyReader> propertiesByName() { return _propsByName; }

    public BeanPropertyReader findProperty(String name) {
        BeanPropertyReader prop = _propsByName.get(name);
        if (prop == null) {
            return _findAlias(name);
        }
        return prop;
    }

    private final BeanPropertyReader _findAlias(String name) {
        String primaryName = _aliasMapping.get(name);
        return (primaryName == null) ? null : _propsByName.get(primaryName);
    }

    @Override
    public Object readNext(JSONReader r, JsonParser p) throws IOException
    {
        JsonToken t = p.nextToken();
        try {
            switch (t) {
            case VALUE_NULL:
                return null;
            case VALUE_STRING:
                return _constructors.create(p.getText());
            case VALUE_NUMBER_INT:
                return _constructors.create(p.getLongValue());
            case START_OBJECT:
                {
                    if (_isRecordType) {
                        return readRecord(r, p);
                    }
                    Object bean = _constructors.create();
                    final Object[] valueBuf = r._setterBuffer;
                    String propName;

                    for (; (propName = p.nextFieldName()) != null; ) {
                        BeanPropertyReader prop = findProperty(propName);
                        if (prop == null) {
                            handleUnknown(r, p, propName);
                            continue;
                        }
                        valueBuf[0] = prop.getReader().readNext(r, p);
                        prop.setValueFor(bean, valueBuf);
                    }
                    // also verify we are not confused...
                    if (!p.hasToken(JsonToken.END_OBJECT)) {
                        throw _reportProblem(p);
                    }
                    return bean;
                }
            default:
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw JSONObjectException.from(p, "Failed to create an instance of "
                    +_valueType.getName()+" due to ("+e.getClass().getName()+"): "+e.getMessage(),
                    e);
        }
        throw JSONObjectException.from(p,
                "Can not create a "+_valueType.getName()+" instance out of "+_tokenDesc(p));
    }

    private Object readRecord(JSONReader r, JsonParser p) throws Exception {
        final Object[] values = new Object[_propsByName.size() + _constructors.getRecordCtorAliasesCount()];

        String propName;
        for (; (propName = p.nextFieldName()) != null;) {
            BeanConstructors.RecordAlias recAlias;
            if ((recAlias = _constructors.getRecordCtorAliasValueReader(propName)) != null) {
                values[recAlias.pos()] = recAlias.reader().readNext(r, p);

                continue;
            }

            BeanPropertyReader prop = findProperty(propName);
            if (prop == null) {
                handleUnknown(r, p, propName);
                continue;
            }
            Object value = prop.getReader().readNext(r, p);
            values[prop.getIndex()] = value;
        }
        return _constructors.createRecord(values);
    }

    /**
     * Method used for deserialization; will read an instance of the bean
     * type using given parser.
     */
    @Override
    public Object read(JSONReader r, JsonParser p) throws IOException
    {
        JsonToken t = p.currentToken();

        try {
            switch (t) {
            case VALUE_NULL:
                return null;
            case VALUE_STRING:
                return _constructors.create(p.getText());
            case VALUE_NUMBER_INT:
                return _constructors.create(p.getLongValue());
            case START_OBJECT:
                {
                    if (_isRecordType) {
                        return readRecord(r, p);
                    }
                    Object bean = _constructors.create();
                    String propName;
                    final Object[] valueBuf = r._setterBuffer;

                    for (; (propName = p.nextFieldName()) != null; ) {
                        BeanPropertyReader prop = findProperty(propName);
                        if (prop == null) {
                            handleUnknown(r, p, propName);
                            continue;
                        }
                        valueBuf[0] = prop.getReader().readNext(r, p);
                        prop.setValueFor(bean, valueBuf);
                    }
                    // also verify we are not confused...
                    if (!p.hasToken(JsonToken.END_OBJECT)) {
                        throw _reportProblem(p);
                    }
                    return bean;
                }
            default:
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw JSONObjectException.from(p, e,
                    "Failed to create an instance of %s due to (%s): %s",
                    _valueType.getName(), e.getClass().getName(), e.getMessage());
        }
        throw JSONObjectException.from(p, "Can not create a %s instance out of %s",
                _valueType.getName(), _tokenDesc(p));
    }

    protected void handleUnknown(JSONReader reader, JsonParser parser, String fieldName) throws IOException {
        if (JSON.Feature.FAIL_ON_UNKNOWN_BEAN_PROPERTY.isEnabled(reader._features)) {
            // 20-Jan-2020, tatu: With optional annotation support, may have "known ignorable"
            //    that usually should behave as if safely ignorable
            if (!_ignorableNames.contains(fieldName)) {
                final StringBuilder sb = new StringBuilder(60);
                Iterator<String> it = new TreeSet<String>(_propsByName.keySet()).iterator();
                if (it.hasNext()) {
                    sb.append('"').append(it.next()).append('"');
                    while (it.hasNext()) {
                        sb.append(", \"").append(it.next()).append('"');
                    }
                }
                throw JSONObjectException.from(parser,
"Unrecognized JSON property \"%s\" for Bean type `%s` (known properties: [%s])",
                        fieldName, _valueType.getName(), sb.toString());
            }
        }
        parser.nextToken();
        parser.skipChildren();
    }

    protected IOException _reportProblem(JsonParser p) {
        return JSONObjectException.from(p, "Unexpected token "+p.currentToken()+"; should get FIELD_NAME or END_OBJECT");
    }
}
