package com.fasterxml.jackson.jr.annotationsupport;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.impl.BeanConstructors;
import com.fasterxml.jackson.jr.ob.impl.JSONReader;
import com.fasterxml.jackson.jr.ob.impl.JSONWriter;
import com.fasterxml.jackson.jr.ob.impl.POJODefinition;

/**
 *
 * @since 2.11
 */
public class AnnotationBasedIntrospector
{
    // // // Configuration

    protected final Class<?> _type;

    protected final boolean _forSerialization;

    /**
     * Visibility settings to use for auto-detecting accessors.
     */
    protected final JsonAutoDetect.Value _visibility;

    // // // State (collected properties, related)

    protected final Map<String, APropBuilder> _props = new HashMap<String, APropBuilder>();

    // // // State only for deserialization:

    protected Set<String> _ignorableNames;
    protected int _features;

    protected AnnotationBasedIntrospector(Class<?> type, boolean serialization,
                                          JsonAutoDetect.Value visibility, int features) {
        _type = type;
        _forSerialization = serialization;
        _ignorableNames = serialization ? null : new HashSet<String>();
        _features = features;

        // First things first: find possible `@JsonAutoDetect` to override
        // default visibility settings
        JsonAutoDetect ann = _find(type, JsonAutoDetect.class); // bad form but...
        if (ann == null) {
            _visibility = visibility;
        } else {
            _visibility = visibility.withOverrides(JsonAutoDetect.Value.from(ann));
        }
    }

    public static POJODefinition pojoDefinitionForDeserialization(JSONReader r,
            Class<?> pojoType, JsonAutoDetect.Value visibility) {
        return new AnnotationBasedIntrospector(pojoType, false, visibility, r.features())
                .introspectDefinition();
    }

    public static POJODefinition pojoDefinitionForSerialization(JSONWriter w,
            Class<?> pojoType, JsonAutoDetect.Value visibility) {
        return new AnnotationBasedIntrospector(pojoType, true, visibility, w.features())
                .introspectDefinition();
    }

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    protected POJODefinition introspectDefinition()
    {
        _findFields();
        _findMethods();

        BeanConstructors constructors = null;

        // A few things only matter during deserialization: constructors,
        // secondary ignoral information:
        if (_forSerialization) {
            constructors = null;
        } else {
            constructors = new BeanConstructors(_type);
            for (Constructor<?> ctor : _type.getDeclaredConstructors()) {
                Class<?>[] argTypes = ctor.getParameterTypes();
                if (argTypes.length == 0) {
                    constructors.addNoArgsConstructor(ctor);
                } else if (argTypes.length == 1) {
                    Class<?> argType = argTypes[0];
                    if (argType == String.class) {
                        constructors.addStringConstructor(ctor);
                    } else if (argType == Long.class || argType == Long.TYPE) {
                        constructors.addLongConstructor(ctor);
                    }
                }
            }
        }

        POJODefinition def = new POJODefinition(_type,
                _pruneProperties(_forSerialization), constructors);
        if (_ignorableNames != null) {
            def = def.withIgnorals(_ignorableNames);
        }
        return def;
    }

    /*
    /**********************************************************************
    /* Internal methods, main introspection
    /**********************************************************************
     */

    protected POJODefinition.Prop[] _pruneProperties(boolean sortProperties)
    {
        // First round: entry removal, collections of things to rename
        List<APropBuilder> renamed = null;
        Iterator<APropBuilder> it = _props.values().iterator();
        while (it.hasNext()) {
            final APropBuilder prop = it.next();

            // Start with ignorals, since those can be used as marker for otherwise
            // unknown properties
            if (prop.anyIgnorals()) {
                // if one or more ignorals, and no explicit markers, remove the whole thing
                if (!prop.anyExplicit()) {
                    it.remove();
                    _addIgnoral(prop.name);
                } else {
                    // otherwise just remove ones marked to be ignored
                    prop.removeIgnored();
                    if (!prop.couldDeserialize()) {
                        _addIgnoral(prop.name);
                    }
                }
                continue;
            }
            // but even without ignorals, something has to be visible; if not, remove prop
            if (!prop.anyVisible()) { // if nothing visible, just remove altogether
                it.remove();
                continue;
            }
            // plus then remove non-visible accessors
            prop.removeNonVisible();

            // and finally, see if renaming (due to explicit name override) needed:
            String explName = prop.findPrimaryExplicitName(_forSerialization);
            if (explName != null) {
                it.remove();
                if (renamed == null) {
                    renamed = new LinkedList<APropBuilder>();
                }
                renamed.add(prop.withName(explName));
            }
        }

        // If (but only if) renamings needed, re-process
        if (renamed != null) {
            for (APropBuilder prop : renamed) {
                APropBuilder orig = _props.get(prop.name);
                if (orig == null) { // Straight rename, no merge
                    _props.put(prop.name, prop);
                    continue;
                }
                APropBuilder merged = APropBuilder.merge(orig, prop);
                _props.put(prop.name, merged);
            }
        }

        // Next step: removal by `@JsonIgnoreProperties`:
        final Collection<String> ignorableNames = _findIgnorableNames();
        if (!ignorableNames.isEmpty()) {
            if (_ignorableNames != null) { // may be needed for deserialization
                _ignorableNames.addAll(ignorableNames);
            }
            // but needs to be removed from set of known properties for both
            for (String ignorableName : ignorableNames) {
                _findAndRemoveByName(ignorableName);
            }
        }

        final int propCount = _props.size();
        final POJODefinition.Prop[] result = new POJODefinition.Prop[propCount];
        int i = 0;
        final boolean collectAliases = !_forSerialization;

        if (sortProperties) { // sorting? (yes for serialization, no for deser)
            // anything to sort by?
            List<String> nameOrder = _findNameSortOrder();
            if (!nameOrder.isEmpty()) {
                for (String name : nameOrder) {
                    APropBuilder prop = _findAndRemoveByName(name);
                    if (prop != null) {
                        result[i++] = prop.asProperty(collectAliases);
                    }
                }
            }

            // and anything remaining, add alphabetically
            TreeMap<String, APropBuilder> sorted = new TreeMap<String, APropBuilder>(_props);

            // For now, order alphabetically (natural order by name)
            for (APropBuilder prop : sorted.values()) {
                result[i++] = prop.asProperty(collectAliases);
            }
        } else {
            for (APropBuilder prop : _props.values()) {
                result[i++] = prop.asProperty(collectAliases);
            }
        }
        return result;
    }

    protected void _findFields() {
        _findFields(_type);
    }

    protected void _findFields(final Class<?> currType)
    {
        if (currType == null || currType == Object.class) {
            return;
        }
        // [jackson-jr#76]: Was not doing recursive field detection
        // Start with base type fields (so overrides work)
        _findFields(currType.getSuperclass());

        // then get fields from within class itself
        for (Field f : currType.getDeclaredFields()) {
            // skip static fields and synthetic fields except for enum constants
            if ((JSON.Feature.INCLUDE_STATIC_FIELDS.isDisabled(_features) && Modifier.isStatic(f.getModifiers())
                    && !f.isEnumConstant()) || f.isSynthetic()) {
                continue;
            }
            // otherwise, first things first; explicit ignoral?
            final String implName = f.getName();
            APropAccessor<Field> acc;

            if (Boolean.TRUE.equals(_hasIgnoreMarker(f))) {
                acc = APropAccessor.createIgnorable(implName, f);
            } else {
                final String explName = _findExplicitName(f);
                // Otherwise, do have explicit inclusion marker?
                if (explName != null) {
                    // ... with actual name?
                    if (explName.isEmpty()) { // `@JsonProperty("")`
                        acc = APropAccessor.createVisible(implName, f);
                    } else {
                        acc = APropAccessor.createExplicit(explName, f);
                    }
                } else {
                    // Otherwise may be visible
                    acc = APropAccessor.createImplicit(explName, f,
                            _isFieldVisible(f));
                }
            }
            _propBuilder(implName).field = acc;

        }
    }

    protected void _findMethods() {
        _findMethods(_type);
    }

    protected void _findMethods(final Class<?> currType)
    {
        if (currType == null || currType == Object.class || currType == Enum.class) {
            return;
        }
        // Start with base type methods (so overrides work)
        _findMethods(currType.getSuperclass());

        // then get methods from within this class
        for (Method m : currType.getDeclaredMethods()) {
            final int flags = m.getModifiers();
            // 13-Jun-2015, tatu: Skip synthetic, bridge methods altogether, for now
            //    at least (add more complex handling only if absolutely necessary)
            if (Modifier.isStatic(flags)
                    || m.isSynthetic() || m.isBridge()) {
                continue;
            }
            int argCount = m.getParameterCount();
            if (argCount == 0) { // getters (including 'any getter')
                _checkGetterMethod(m);
            } else if (argCount == 1) { // setters
                _checkSetterMethod(m);
            }
        }
    }

    protected void _checkGetterMethod(Method m)
    {
        Class<?> resultType = m.getReturnType();
        if (resultType == Void.class) {
            return;
        }
        final String name0 = m.getName();
        String implName = null;
        boolean isIsGetter = false;

        if (name0.startsWith("get")) {
            if (name0.length() > 3) {
                implName = _decap(name0.substring(3));
            }
        } else if (name0.startsWith("is")) {
            if (name0.length() > 2) {
                // May or may not be used, but collect for now all the same:
                implName = _decap(name0.substring(2));
                isIsGetter = true;
            }
        }

        APropAccessor<Method> acc;
        if (implName == null) { // does not follow naming convention; needs explicit
            final String explName = _findExplicitName(m);
            if (explName == null) {
                return;
            }
            implName = name0;

            // But let's first see if there is ignoral
            if (Boolean.TRUE.equals(_hasIgnoreMarker(m))) {
                // could just bail out as is, at this point? But there is explicit marker
                acc = APropAccessor.createIgnorable(implName, m);
            } else {
                if (explName.isEmpty()) {
                    acc = APropAccessor.createVisible(implName, m);
                } else {
                    acc = APropAccessor.createExplicit(explName, m);
                }
            }
        } else { // implicit name already, but ignoral/explicit?
            if (Boolean.TRUE.equals(_hasIgnoreMarker(m))) {
                acc = APropAccessor.createIgnorable(implName, m);
            } else {
                final String explName = _findExplicitName(m);
                if (explName == null) {
                    acc = APropAccessor.createImplicit(implName, m,
                            _isGetterVisible(m, isIsGetter));
                } else if (explName.isEmpty()) {
                    acc = APropAccessor.createVisible(implName, m);
                } else {
                    acc = APropAccessor.createExplicit(explName, m);
                }
            }
        }
        _propBuilder(implName).getter = acc;
    }

    protected void _checkSetterMethod(Method m)
    {
        final String name0 = m.getName();
        String implName;

        if (name0.startsWith("set") && (name0.length() > 3)) {
            implName = _decap(name0.substring(3));
        } else {
            implName = null;
        }

        // Pretty much the same as with getters (just calls to couple of diff methods)
        APropAccessor<Method> acc;
        if (implName == null) {
            final String explName = _findExplicitName(m);
            if (explName == null) {
                return;
            }
            implName = name0;

            if (Boolean.TRUE.equals(_hasIgnoreMarker(m))) {
                acc = APropAccessor.createIgnorable(implName, m);
            } else {
                if (explName.isEmpty()) {
                    acc = APropAccessor.createVisible(implName, m);
                } else {
                    acc = APropAccessor.createExplicit(explName, m);
                }
            }
        } else {
            if (Boolean.TRUE.equals(_hasIgnoreMarker(m))) {
                acc = APropAccessor.createIgnorable(implName, m);
            } else {
                final String explName = _findExplicitName(m);
                if (explName == null) {
                    acc = APropAccessor.createImplicit(implName, m,
                            _isSetterVisible(m));
                } else if (explName.isEmpty()) {
                    acc = APropAccessor.createVisible(implName, m);
                } else {
                    acc = APropAccessor.createExplicit(explName, m);
                }
            }
        }
        _propBuilder(implName).setter = acc;
    }

    /*
    /**********************************************************************
    /* Internal methods, visibility
    /**********************************************************************
     */

    protected boolean _isFieldVisible(Field f) {
        // Consider transient and static-final to be non-visible
        // TODO: (maybe?) final
        return !(Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) && !f.isEnumConstant())
                && !Modifier.isTransient(f.getModifiers())
                && _visibility.getFieldVisibility().isVisible(f);
    }

    protected boolean _isGetterVisible(Method m, boolean isIsGetter) {
        if (isIsGetter) {
            return _visibility.getIsGetterVisibility().isVisible(m);
        }
        return _visibility.getGetterVisibility().isVisible(m);
    }

    protected boolean _isSetterVisible(Method m) {
        return _visibility.getSetterVisibility().isVisible(m);
    }

    /*
    /**********************************************************************
    /* Internal methods, annotation introspection
    /**********************************************************************
     */

    // wrapper type just in case in future we want to detect existence of disables
    // ignoral marker for some reason
    protected Boolean _hasIgnoreMarker(AnnotatedElement m) {
        JsonIgnore ann = _find(m, JsonIgnore.class);
        return (ann != null) && ann.value();
    }

    protected String _findExplicitName(AnnotatedElement m) {
        JsonProperty ann = _find(m, JsonProperty.class);
        return (ann == null) ? null : ann.value();
    }

    /**
     * Lookup method for finding possible annotated order of property names
     * for the type this introspector is to introspect
     *
     * @return List of property names that defines order (possibly partial); if
     *   none, empty List (but never null)
     */
    protected List<String> _findNameSortOrder() {
        JsonPropertyOrder ann = _find(_type, JsonPropertyOrder.class);
        if (ann == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(ann.value());
    }

    /**
     * Lookup method for finding a set of property names
     * for the type this introspector is to introspect that should be ignored
     * (both for serialization and deserialization).
     *
     * @return List of property names that defines order (possibly partial); if
     *   none, empty List (but never null)
     */
    protected Collection<String> _findIgnorableNames() {
        JsonIgnoreProperties ann = _find(_type, JsonIgnoreProperties.class);
        if (ann == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(ann.value());
    }

    // Overridable accessor method
    protected <ANN extends Annotation> ANN _find(AnnotatedElement elem, Class<ANN> annotationType) {
        return elem.getAnnotation(annotationType);
    }

    /*
    /**********************************************************************
    /* Internal methods, other
    /**********************************************************************
     */

    protected APropBuilder _propBuilder(String name) {
        return _props.computeIfAbsent(name,APropBuilder::new);
    }

    protected void _addIgnoral(String name) {
        if (_ignorableNames != null) {
            _ignorableNames.add(name);
        }
    }

    /**
     * Helper method for locating a property (builder) identified by given name
     * (either primary, or secondary), and if found, removing from main properties
     * Map, returning.
     *
     * @param name Name of property to find (either primary [checked first] or secondary)
     *
     * @return Property (builder) if found; {@code null} if none
     */
    protected APropBuilder _findAndRemoveByName(String name) {
        APropBuilder prop = _props.remove(name);
        if (prop == null) {
            // Not located by primary, check secondary ('original' or 'internal' name)
            for (APropBuilder p2 : _props.values()) {
                prop = _props.remove(p2.origName);
                if (prop != null) {
                    break;
                }
            }
        }
        return prop;
    }

    protected static String _decap(String name) {
        char c = name.charAt(0);
        char lowerC = Character.toLowerCase(c);

        if (c != lowerC) {
            // First: do NOT lower case if more than one leading upper case letters:
            if ((name.length() == 1)
                    || !Character.isUpperCase(name.charAt(1))) {
                char chars[] = name.toCharArray();
                chars[0] = lowerC;
                return new String(chars);
            }
        }
        return name;
    }

    /*
    /**********************************************************************
    /* Helper classes
    /**********************************************************************
     */

    protected static class APropBuilder
        implements Comparable<APropBuilder>
    {
        /**
         * Initial name from accessor ("implicit" or "internal" name); not
         * changed with renames
         */
        public final String origName;

        public final String name;

        protected APropAccessor<Field> field;
        protected APropAccessor<Method> getter;
        protected APropAccessor<Method> setter;

        public APropBuilder(String n) {
            origName = n;
            name = n;
        }

        protected APropBuilder(APropBuilder base, String n) {
            origName = base.origName;
            name = n;
        }

        public POJODefinition.Prop asProperty(boolean collectAliases) {
            Set<String> aliases = collectAliases ? collectAliases() : null;
            return new POJODefinition.Prop(name,
                    (field == null) ? null : field.accessor,
                    (setter == null) ? null : setter.accessor,
                    (getter == null) ? null : getter.accessor,
                    /* isGetter */ null,
                    aliases);
        }

        public static APropBuilder merge(APropBuilder b1, APropBuilder b2) {
            APropBuilder newB = new APropBuilder(b1.name);
            newB.field = _merge(b1.field, b2.field);
            newB.getter = _merge(b1.getter, b2.getter);
            newB.setter = _merge(b1.setter, b2.setter);
            return newB;
        }

        private static <A extends AccessibleObject> APropAccessor<A> _merge(APropAccessor<A> a1, APropAccessor<A> a2)
        {
            if (a1 == null) {
                return a2;
            }
            if (a2 == null) {
                return a1;
            }

            if (a1.isNameExplicit) {
                return a1;
            }
            if (a2.isNameExplicit) {
                return a2;
            }
            if (a1.isExplicit) {
                return a1;
            }
            if (a2.isExplicit) {
                return a2;
            }
            // Could try other things too (visibility, place in hierarchy) but... for now
            // should be fine to take first one
            return a1;
        }

        public APropBuilder withName(String newName) {
            APropBuilder newB = new APropBuilder(this, newName);
            newB.field = field;
            newB.getter = getter;
            newB.setter = setter;
            return newB;
        }

        public void removeIgnored() {
            if ((field != null) && field.isToIgnore) {
                field = null;
            }
            if ((getter != null) && getter.isToIgnore) {
                getter = null;
            }
            if ((setter != null) && setter.isToIgnore) {
                setter = null;
            }
        }

        public void removeNonVisible() {
            if ((field != null) && !field.isVisible) {
                field = null;
            }
            if ((getter != null) && !getter.isVisible) {
                getter = null;
            }
            if ((setter != null) && !setter.isVisible) {
                setter = null;
            }
        }

        public Set<String> collectAliases() {
            Set<String> collectedAliases = null;
            // although aliases only relevant for deserialization, collect from
            // all accessors nonetheless
            collectedAliases = _collectAliases(field, collectedAliases);
            collectedAliases = _collectAliases(getter, collectedAliases);
            collectedAliases = _collectAliases(setter, collectedAliases);
            return collectedAliases;
        }

        private static Set<String> _collectAliases(APropAccessor<?> acc, Set<String> collectedAliases) {
            if (acc != null) {
                AnnotatedElement accOb = acc.accessor;
                if (accOb != null) {
                    JsonAlias ann = accOb.getAnnotation(JsonAlias.class);
                    if (ann != null) {
                        final String[] names = ann.value();
                        if (collectedAliases == null) {
                            collectedAliases = new HashSet<String>();
                        }
                        for (String alias : names) {
                            collectedAliases.add(alias);
                        }
                    }
                }
            }
            return collectedAliases;
        }

        private String _firstExplicit(APropAccessor<?> acc1,
                APropAccessor<?> acc2,
                APropAccessor<?> acc3) {
            if (acc1 != null && acc1.isNameExplicit) {
                return acc1.name;
            }
            if (acc2 != null && acc2.isNameExplicit) {
                return acc2.name;
            }
            if (acc3 != null && acc3.isNameExplicit) {
                return acc3.name;
            }
            return null;
        }

        public String findPrimaryExplicitName(boolean forSer) {
            if (forSer) {
                return _firstExplicit(getter, setter, field);
            }
            return _firstExplicit(setter, getter, field);
        }

        public boolean anyVisible() {
            return ((field != null) && field.isVisible)
                    || ((getter != null) && getter.isVisible)
                    || ((setter != null) && setter.isVisible);
        }

        public boolean anyExplicit() {
            return ((field != null) && field.isExplicit)
                    || ((getter != null) && getter.isExplicit)
                    || ((setter != null) && setter.isExplicit);
        }

        public boolean anyIgnorals() {
            return ((field != null) && field.isToIgnore)
                    || ((getter != null) && getter.isToIgnore)
                    || ((setter != null) && setter.isToIgnore);
        }

        public boolean couldDeserialize() {
            return (field != null) || (setter != null);
        }

        @Override
        public int compareTo(APropBuilder o) {
            return name.compareTo(o.name);
        }
    }

    protected static class APropAccessor<ACC extends AccessibleObject> {
        public final String name;
        public final ACC accessor;

        public final boolean isExplicit, isNameExplicit;
        public final boolean isToIgnore, isVisible;

        protected APropAccessor(String n, ACC acc,
                boolean expl, boolean nameExpl,
                boolean ignore, boolean visible)
        {
            name = n;
            accessor = acc;
            isExplicit = expl;
            isNameExplicit = nameExpl;
            isToIgnore = ignore;
            isVisible = visible;
        }

        // We saw `@JsonIgnore` and that's all we need
        public static <T extends AccessibleObject> APropAccessor<T> createIgnorable(String name, T accessor) {
            return new APropAccessor<T>(name, accessor,
                    false, false, true, false);
        }

        // We didn't saw any relevant annotation
        public static <T extends AccessibleObject> APropAccessor<T> createImplicit(String name, T accessor,
                boolean visible) {
            return new APropAccessor<T>(name, accessor,
                    false, false, false, visible);
        }

        // We only saw "empty" `@JsonProperty` (or similar marker)
        public static <T extends AccessibleObject> APropAccessor<T> createVisible(String name, T accessor) {
            return new APropAccessor<T>(name, accessor,
                    true, false, false, true);
        }

        // We saw `@JsonProperty` with non-empty name
        public static <T extends AccessibleObject> APropAccessor<T> createExplicit(String name, T accessor) {
            return new APropAccessor<T>(name, accessor,
                    true, true, false, true);
        }
    }
}
