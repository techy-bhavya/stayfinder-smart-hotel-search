package com.stayfinder.app.algorithm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LruCacheTest {
    @Test
    void evictsLeastRecentlyUsedEntry() {
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("Jaipur", 1);
        cache.put("Goa", 2);
        cache.get("Jaipur");
        cache.put("Mumbai", 3);

        assertThat(cache.get("Jaipur")).contains(1);
        assertThat(cache.get("Goa")).isEmpty();
        assertThat(cache.get("Mumbai")).contains(3);
    }
}
