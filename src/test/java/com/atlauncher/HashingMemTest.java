package com.atlauncher;

import com.atlauncher.collection.Caching;
import com.atlauncher.utils.Hashing;
import org.junit.Before;
import org.junit.Test;

public final class HashingMemTest{
    private Runtime runtime;

    @Before
    public void init(){
        this.runtime = Runtime.getRuntime();
    }

    @Test
    public void test()
    throws Exception{
        long start = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Before " + ((start / 1024) / 1024) + "MB");
        for(int i = 0; i < Caching.MAX_SIZE; i++){
            Hashing.md5("Hello, " + i);
        }
        long current = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Iteration 1 " + (((start / 1024) / 1024) + (((current - start) / 1024) / 1024)) + "MB");
        for(int i = 0; i < Caching.MAX_SIZE; i++){
            Hashing.md5("Hello, " + i);
        }
        current = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Iteration 2 " + (((start / 1024) / 1024) + (((current - start) / 1024) / 1024)) + "MB");
        for(int i = 0; i < Caching.MAX_SIZE; i++){
            Hashing.md5("Hello, " + i);
        }
        current = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Iteration 3 " + (((start / 1024) / 1024) + (((current - start) / 1024) / 1024)) + "MB");
        for(int i = 0; i < Caching.MAX_SIZE; i++){
            Hashing.md5("Hello, " + i);
        }
        current = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Iteration 4 " + (((start / 1024) / 1024) + (((current - start) / 1024) / 1024)) + "MB");
        for(int i = 0; i < Caching.MAX_SIZE; i++){
            Hashing.md5("Hello, " + i);
        }
        current = this.runtime.totalMemory() - this.runtime.freeMemory();
        System.out.println("Final " + (((start / 1024) / 1024) + (((current - start) / 1024) / 1024)) + "MB");
    }
}