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
import org.junit.Before;
import org.junit.Test;

public final class HashingMemTest {
    private Runtime runtime;

    @Before
    public void init() {
        this.runtime = Runtime.getRuntime();
    }

    @Test
    public void test() throws Exception {
        System.out.println("Doing 4 iterations, creating " + Caching.MAX_SIZE + " objects");

        long start = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Before " + ((start / 1024) / 1024) + "MB");
        for (int i = 0; i < Caching.MAX_SIZE; i++) {
            Hashing.md5("Hello, " + i);
        }
        long current = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Iteration 1 " + (((start / 1024) / 1024) + (((current - start) / 1024) / 1024)) + "MB");
        for (int i = 0; i < Caching.MAX_SIZE; i++) {
            Hashing.md5("Hello, " + i);
        }
        current = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Iteration 2 " + (((start / 1024) / 1024) + (((current - start) / 1024) / 1024)) + "MB");
        for (int i = 0; i < Caching.MAX_SIZE; i++) {
            Hashing.md5("Hello, " + i);
        }
        current = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Iteration 3 " + (((start / 1024) / 1024) + (((current - start) / 1024) / 1024)) + "MB");
        for (int i = 0; i < Caching.MAX_SIZE; i++) {
            Hashing.md5("Hello, " + i);
        }
        current = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Iteration 4 " + (((start / 1024) / 1024) + (((current - start) / 1024) / 1024)) + "MB");
        for (int i = 0; i < Caching.MAX_SIZE; i++) {
            Hashing.md5("Hello, " + i);
        }
        current = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Final " + (((start / 1024) / 1024) + (((current - start) / 1024) / 1024)) + "MB");
    }
}