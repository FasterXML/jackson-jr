package tools.jackson.jr.type;

import java.util.ArrayList;

final class ClassStack
{
    private final ClassStack _p;
    private final Class<?> _curr;
    
    private ArrayList<RecursiveType> _selfRefs;
    
    public ClassStack(Class<?> rootType) {
        this(null, rootType);
    }
    
    private ClassStack(ClassStack parent, Class<?> curr) {
        _p = parent;
        _curr = curr;
    }

    public ClassStack child(Class<?> cls) {
        return new ClassStack(this, cls);
    }

    public void addSelfReference(RecursiveType ref) {
        if (_selfRefs == null) {
            _selfRefs = new ArrayList<RecursiveType>();
        }
        _selfRefs.add(ref);
    }

    public void resolveSelfReferences(ResolvedType resolved) {
        if (_selfRefs != null) {
            for (RecursiveType ref : _selfRefs) {
                ref.setReference(resolved);
            }
        }
    }
    
    public ClassStack find(Class<?> cls) {
        if (_curr == cls) return this;
        if (_p != null) {
            return _p.find(cls);
        }
        return null;
    }
}