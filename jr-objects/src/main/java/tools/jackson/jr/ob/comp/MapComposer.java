package tools.jackson.jr.ob.comp;

import java.util.*;

import tools.jackson.core.SerializableString;

public class MapComposer<PARENT extends ComposerBase>
    extends ComposerBase
{
    protected final PARENT _parent;

    protected String _propName;

    protected Map<String,Object> _map;
    
    public MapComposer(PARENT parent) {
        super();
        _parent = parent;
    }

    public MapComposer(Map<String,Object> map) {
        super();
        _parent = null;
        _map = map;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static MapComposer<?> rootComposer(Map<String,Object> map) {
        return new MapComposer(map);
    }

    /*
    /**********************************************************************
    /* Abstract method impls
    /**********************************************************************
     */

    @Override
    public void flush() { }

    @Override
    protected MapComposer<PARENT> _start() {
        if (_map == null) {
            _map = constructMap();
        }
        return this;
    }
    
    @Override
    protected Map<String,Object> _finish()
    {
        if (_open) {
            _open = false;
        }
        return _map;
    }
    
    /*
    /**********************************************************************
    /* Compose methods, structures
    /**********************************************************************
     */
    
    public CollectionComposer<MapComposer<PARENT>,?> startArrayProperty(String propName)
    {
        _closeChild();
        _propName = propName;
        CollectionComposer<MapComposer<PARENT>,?> child = _startCollection(this);
        _map.put(propName, child._collection);
        return child;
    }
    
    public CollectionComposer<MapComposer<PARENT>,?> startArrayProperty(SerializableString propName) {
        return startArrayProperty(propName.getValue());
    }
    
    public MapComposer<MapComposer<PARENT>> startObjectProperty(String propName)
    {
        _closeChild();
        _propName = propName;
        MapComposer<MapComposer<PARENT>> child = _startMap(this);
        _map.put(propName, child._map);
        return child;
    }
    
    public MapComposer<MapComposer<PARENT>> startObjectProperty(SerializableString propName) {
        return startObjectProperty(propName.getValue());
    }
    
    public PARENT end()
    {
        _closeChild();
        if (_open) {
            _open = false;
            _parent._childClosed();
        }
        return _parent;
    }

    public Map<String,Object> finish() {
        return _finish();
    }
    
    /*
    /**********************************************************************
    /* Compose methods, scalars
    /**********************************************************************
     */
    
    public MapComposer<PARENT> put(String propName, boolean value)
    {
        _map.put(propName, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }
    
    public MapComposer<PARENT> putNull(String propName)
    {
        // could maybe just omit but...
        _map.put(propName, null);
        return this;
    }
    
    public MapComposer<PARENT> put(String propName, int value)
    {
        _map.put(propName, value);
        return this;
    }

    public MapComposer<PARENT> put(String propName, long value)
    {
        _map.put(propName, value);
        return this;
    }

    public MapComposer<PARENT> put(String propName, double value)
    {
        _map.put(propName, value);
        return this;
    }
    
    public MapComposer<PARENT> put(String propName, String value)
    {
        _map.put(propName, value);
        return this;
    }
    
    public MapComposer<PARENT> put(String propName, CharSequence value)
    {
        String str = (value == null) ? null : value.toString();
        _map.put(propName, str);
        return this;
    }

    /*
    /**********************************************************************
    /* Overridable helper methods
    /**********************************************************************
     */
    
    protected Map<String,Object> constructMap() {
        return new LinkedHashMap<String,Object>();
    }
    
    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */
    
    protected void _closeChild()
    {
        if (_child != null) {
            Object value = _child._finish();
            _map.put(_propName, value);
            _child = null;
        }
    }
}
