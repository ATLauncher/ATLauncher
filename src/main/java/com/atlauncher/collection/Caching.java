/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.collection;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Caching {
    public static final int MAX_SIZE = Integer
            .parseInt(System.getProperty("com.atlauncher.collection.Caching.cacheSize", "127"));

    private Caching() {
    }

    public interface Cache<K, V> extends Iterable<Map.Entry<K, V>> {
        V get(K key);

        V put(K key, V value);

        int size();
    }

    public static <K, V> Cache<K, V> newLRU() {
        return new LRUCache<>(MAX_SIZE);
    }

    public static <K, V> Cache<K, V> newLRU(int size) {
        return new LRUCache<>(size);
    }

    @SuppressWarnings("serial")
    private static final class LRUCache<K, V> extends LinkedHashMap<K, V> implements Cache<K, V> {
        private final int cap;

        private LRUCache(int cap) {
            super(cap + 1, 0.75F, true);
            this.cap = cap;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return this.size() > this.cap;
        }

        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return this.entrySet().iterator();
        }
    }
}
