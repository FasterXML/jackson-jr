package com.fasterxml.jackson.jr.annotationsupport;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Map used when we need to maintain insertion index.
 */
@SuppressWarnings("NullableProblems")
class IndexedMap<K, V> extends AbstractMap<K, V> {

    private final ArrayList<Map.Entry<K, V>> entries = new ArrayList<>();
    private final Map.Entry<K, V> nullValue = new SimpleEntry<>(null, null);

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean containsKey(Object key) {
        return entries.stream().anyMatch(e -> e.getKey().equals(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return entries.stream().anyMatch(e -> e.getValue().equals(value));
    }

    @Override
    public V get(Object key) {
        return entries.stream().filter(e -> e.getKey().equals(key))
                      .findFirst()
                      .orElse(nullValue)
                      .getValue();
    }

    @Override
    public V put(K key, V value) {
        V old = remove(key);
        entries.add(new SimpleEntry<>(key, value));
        return old;
    }

    @Override
    public V remove(Object key) {
        V old = null;
        for (int i = 0; i < entries.size(); i++) {
            Entry<K, V> kvEntry = entries.get(i);
            if (kvEntry.getKey().equals(key)) {
                old = kvEntry.getValue();
                entries.remove(i);
                break;
            }
        }
        return old;
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public Set<K> keySet() {
        return entries.stream()
                .map(Entry::getKey)
                .collect(toSet());
    }

    @Override
    public Collection<V> values() {
        return new Values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new LinkedHashSet<>(entries);
    }

    /**
     * Replaces entry at the index of {@code oldKey} with a new entry: {@code newKey, value}.
     */
    public void replaceAtIndexOf(K oldKey, K newKey, V value) {
        boolean stop = false;
        for (int index = 0; index < entries.size() && !stop; index++) {
            Entry<K, V> kvEntry = entries.get(index);
            if (kvEntry.getKey().equals(oldKey)) {
                entries.set(index, new SimpleEntry<>(newKey, value));
                stop = true;
            }
        }
    }

    class Values extends AbstractList<V> {
        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public boolean contains(Object o) {
            for (Entry<K, V> entry : entries) {
                if (entry.getValue().equals(o)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Iterator<V> iterator() {
            return new Iterator<V>() {
                final Iterator<Map.Entry<K, V>> it = entries.iterator();
                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public V next() {
                    return it.next().getValue();
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }

        @Override
        public Object[] toArray() {
            Object[] result = new Object[size()];
            for (int i = 0; i < entries.size(); i++) {
                result[i] = entries.get(i).getValue();
            }
            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            T[] result = (T[]) new Object[size()];
            for (int i = 0; i < entries.size(); i++) {
                result[i] = (T) entries.get(i).getValue();
            }
            return result;
        }

        @Override
        public boolean remove(Object o) {
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getValue().equals(o)) {
                    entries.remove(i);
                    return true;
                }
            }
            return false;
        }

        @Override
        public V get(int index) {
            return entries.get(index).getValue();
        }

        @Override
        public void clear() {
            entries.clear();
        }
    }
}
