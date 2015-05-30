package com.atlauncher.benchmark;

import com.atlauncher.utils.Hashing;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.LinkedList;
import java.util.List;

public class HashCachingBenchmark{
    public static void main(String... args) throws Exception {
        Options opts = new OptionsBuilder()
                .include(HashCachingBenchmark.class.getSimpleName())
                .forks(1)
                .addProfiler(StackProfiler.class)
                .build();
        new Runner(opts).run();
    }

    @Test
    public void test(){
        System.out.println("HashCachingBenchmark#fromString:");
        this.fromString();
        System.out.println("--------------------------------");
        System.out.println("HashCachingBenchmark#md5");
        this.md5();
        System.out.println("------------------------");
    }

    @Benchmark
    public void fromString(){
        Hashing.HashCode code = Hashing.HashCode.fromString("b10a8db164e0754105b7a99be72e3fe5");
        List<Hashing.HashCode> hashes = new LinkedList<>();
        for(int i = 0; i < 10; i++){
            hashes.add(Hashing.HashCode.fromString("b10a8db164e0754105b7a99be72e3fe5"));
        }
        for(int i = 0; i < 10; i++){
            Assert.assertEquals(hashes.get(i), code);
        }
    }

    @Benchmark
    public void md5(){
        Hashing.HashCode code = Hashing.md5("Hello World");
        List<Hashing.HashCode> hashes = new LinkedList<>();
        for(int i = 0; i < 10; i++){
            hashes.add(Hashing.md5("Hello World"));
        }
        for(int i = 0; i < 10; i++){
            Assert.assertEquals(hashes.get(i), code);
        }
    }
}