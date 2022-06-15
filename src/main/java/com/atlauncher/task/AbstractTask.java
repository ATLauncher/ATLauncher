package com.atlauncher.task;

import com.atlauncher.data.AbstractAccount;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public abstract class AbstractTask implements Task{
    private final CountDownLatch latch;

    protected AbstractTask(@Nullable final CountDownLatch latch){
        this.latch = latch;
    }

    public final Optional<CountDownLatch> getLatch(){
        return Optional.ofNullable(this.latch);
    }
}