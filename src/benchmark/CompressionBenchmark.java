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

import com.atlauncher.utils.CompressionUtils;
import com.atlauncher.utils.FileUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@State(Scope.Thread)
public class CompressionBenchmark {
    private Path zip;
    private Path dir;
    private Path output;

    public static void main(String... args) throws Exception {
        Options opts = new OptionsBuilder().include(CompressionBenchmark.class.getSimpleName()).forks(1).addProfiler
                (StackProfiler.class).build();
        new Runner(opts).run();
    }

    @Setup
    public void init() {
        this.dir = Paths.get(System.getProperty("user.home"), "Desktop", "Test");
        this.zip = Paths.get(System.getProperty("user.home"), "Desktop", "Test.zip");
        this.output = Paths.get(System.getProperty("user.home"), "Desktop", "Test2");
    }

    @Benchmark
    public void oldCompress() {
        FileUtils.zip(this.dir, this.zip);
    }

    @Benchmark
    public void oldDecompress() {
        FileUtils.unzip(this.zip, this.dir);
    }

    @Benchmark
    public void newCompress() throws IOException {
        CompressionUtils.zip(this.zip, this.dir);
    }

    @Benchmark
    public void newDecompress() throws IOException {
        CompressionUtils.unzip(this.zip, this.output);
    }
}