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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class HashComparisonBenchmark {
    private Hashing.HashCode original;
    private Hashing.HashCode test;

    public static void main(String... args) throws Exception {
        Options opts = new OptionsBuilder().include(HashComparisonBenchmark.class.getSimpleName()).forks(1)
                .addProfiler(StackProfiler.class).build();
        new Runner(opts).run();
    }

    @Setup
    public void setup() {
        this.original = Hashing.HashCode.fromString("b10a8db164e0754105b7a99be72e3fe5");
        this.test = Hashing.HashCode.fromString("b10a8db164e0754105b7a99be72e3fe5");
    }
}