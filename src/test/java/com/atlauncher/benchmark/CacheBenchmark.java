/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.benchmark;

import com.atlauncher.collection.Caching;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class CacheBenchmark {
    private Cache<String, Integer> guavaCache;
    private Caching.Cache<String, Integer> myCache;

    public static void main(String... args) throws Exception {
        Options opts = new OptionsBuilder().include(CacheBenchmark.class.getSimpleName()).forks(1).addProfiler
                (StackProfiler.class).build();
        new Runner(opts).run();
    }

    @Test
    public void testMine() {
        System.out.println("----- Mine -----");
        Caching.Cache<Integer, String> cache = Caching.newLRU();
        for (int i = 0; i < 127; i++) {
            cache.put(i, "Hello, " + i);
        }
        for (int i = 0; i < 127; i++) {
            System.out.println(cache.get(i));
        }
        System.out.println("-----------------");
    }

    @Test
    public void testGuava() {
        System.out.println("----- Guava -----");
        Cache<Integer, String> cache = CacheBuilder.newBuilder().maximumSize(127).build();
        for (int i = 0; i < 127; i++) {
            cache.put(i, "Hello " + i);
        }
        for (int i = 0; i < 127; i++) {
            System.out.println(cache.getIfPresent(i));
        }
        System.out.println("------------------");
    }

    @Setup
    public void init() {
        this.guavaCache = CacheBuilder.newBuilder().maximumSize(127).build();
        this.myCache = Caching.newLRU();
    }

    @Benchmark
    public void guavaAPut() {
        for (int i = 0; i < 127; i++) {
            this.guavaCache.put("Hello, " + i, i);
        }
    }

    @Benchmark
    public void guavaBGet() {
        for (int i = 0; i < 127; i++) {
            this.guavaCache.getIfPresent("Hello, " + i);
        }
    }

    @Benchmark
    public void myAPut() {
        for (int i = 0; i < 127; i++) {
            this.myCache.put("Hello, " + i, i);
        }
    }

    @Benchmark
    public void myB() {
        for (int i = 0; i < 127; i++) {
            this.myCache.get("Hello, " + i);
        }
    }
}