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

import com.atlauncher.utils.Hashing;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.LinkedList;
import java.util.List;

public class HashCachingBenchmark {
    public static void main(String... args) throws Exception {
        Options opts = new OptionsBuilder().include(HashCachingBenchmark.class.getSimpleName()).forks(1).addProfiler
                (StackProfiler.class).build();
        new Runner(opts).run();
    }

    @Benchmark
    public void fromString() {
        Hashing.HashCode code = Hashing.HashCode.fromString("b10a8db164e0754105b7a99be72e3fe5");
        List<Hashing.HashCode> hashes = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            hashes.add(Hashing.HashCode.fromString("b10a8db164e0754105b7a99be72e3fe5"));
        }
        for (int i = 0; i < 10; i++) {

        }
    }

    @Benchmark
    public void md5() {
        Hashing.HashCode code = Hashing.md5("Hello World");
        List<Hashing.HashCode> hashes = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            hashes.add(Hashing.md5("Hello World"));
        }
        for (int i = 0; i < 10; i++) {
        }
    }
}