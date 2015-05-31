package com.atlauncher;

public final class MemorySpy{
    private final Runtime runtime;
    private final long start;

    public MemorySpy(){
        this.runtime = Runtime.getRuntime();
        this.start = this.now();
    }

    public long now(){
        return this.runtime.totalMemory() - this.runtime.freeMemory();
    }

    public long used(){
        return (start + (this.now() - this.start));
    }
}