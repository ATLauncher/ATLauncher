package com.atlauncher.collection;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Caching{
    public static final int MAX_SIZE = Integer.valueOf(System.getProperty("com.atlauncher.collection.Caching.cacheSize", "127"));

    private Caching(){}

    public static <K, V> LRUCache<K, V> newLRU(){
        return new LRUCache<>(MAX_SIZE);
    }


    public static final class LRUCache<K, V>
    extends LinkedHashMap<K, V>{
        private final int cap;

        private LRUCache(int cap){
            super(cap + 1, 1.0F, true);
            this.cap= cap;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return this.size() > this.cap;
        }
    }
}
