package com.atlauncher.benchmark;

import com.atlauncher.utils.Hashing;
import org.junit.Assert;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class HashComparisonBenchmark{
    private Hashing.HashCode original;
    private Hashing.HashCode test;

    public static void main(String... args)
    throws Exception{
        Options opts = new OptionsBuilder()
                .include(HashComparisonBenchmark.class.getSimpleName())
                .forks(1)
                .addProfiler(StackProfiler.class)
                .build();
        new Runner(opts).run();
    }

    @Setup
    public void setup(){
        this.original = Hashing.HashCode.fromString("b10a8db164e0754105b7a99be72e3fe5");
        this.test = Hashing.HashCode.fromString("b10a8db164e0754105b7a99be72e3fe5");
    }

    @Benchmark
    public void stringComparison(){
        Assert.assertEquals(original.toString(), "b10a8db164e0754105b7a99be72e3fe5");
    }

    @Benchmark
    public void equalsComparison(){
        Assert.assertEquals(original, test);
    }
}