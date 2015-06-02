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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;

@State(Scope.Thread)
public class CloningBenchmark {
    public static void main(String... args) throws RunnerException {
        Options opts = new OptionsBuilder().addProfiler(StackProfiler.class).include(CloningBenchmark.class
                .getSimpleName()).forks(1).build();

        new Runner(opts).run();
    }

    private Hashing.HashCode hashCode;

    @Setup
    public void init() {
        this.hashCode = Hashing.md5("Hello World");
    }

    @Benchmark
    public void benchClone() {
        byte[] bits = this.hashCode.bytes().clone();
    }

    @Benchmark
    public void benchArrayCopy() {
        byte[] bits = new byte[this.hashCode.bytes().length];
        System.arraycopy(this.hashCode.bytes(), 0, bits, 0, bits.length);
    }

    @Benchmark
    public void benchArraysCopy() {
        byte[] bits = Arrays.copyOf(this.hashCode.bytes(), this.hashCode.bytes().length);
    }
}