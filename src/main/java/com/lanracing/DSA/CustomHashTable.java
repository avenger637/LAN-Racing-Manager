package com.lanracing.DSA;

import java.util.HashMap;
import java.util.Map;

public class CustomHashTable<K, V> {
    private final Map<K, V> delegate = new HashMap<>();

    public void put(K key, V value) {
        delegate.put(key, value);
    }

    public V get(K key) {
        return delegate.get(key);
    }

    public V remove(K key) {
        return delegate.remove(key);
    }

    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    public int size() {
        return delegate.size();
    }
}
