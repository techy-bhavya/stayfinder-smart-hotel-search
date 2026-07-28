package com.stayfinder.app.algorithm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Thread-safe bounded cache. LinkedHashMap maintains access order,
 * giving average O(1) get/put and automatic least-recently-used eviction.
 */
public class LruCache<K, V> {
    private final Map<K, V> cache;

    public LruCache(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Cache capacity must be positive");
        }
        this.cache = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        };
    }

    public synchronized Optional<V> get(K key) {
        return Optional.ofNullable(cache.get(key));
    }

    public synchronized void put(K key, V value) {
        cache.put(key, value);
    }

    public synchronized void clear() {
        cache.clear();
    }

    public synchronized int size() {
        return cache.size();
    }
}
