package com.atlauncher;

import com.atlauncher.task.Task;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Singleton
public final class AppTaskEngine{
    private final ExecutorService pool;

    @Inject
    private AppTaskEngine(@Named("taskPool") final ExecutorService pool){
        this.pool = pool;
    }

    public Future<?> submit(final Task task){
        return pool.submit(task);
    }
}