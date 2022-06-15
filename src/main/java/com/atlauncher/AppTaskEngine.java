package com.atlauncher;

import com.atlauncher.task.Task;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public final class AppTaskEngine{
    static final ExecutorService pool = Executors.newWorkStealingPool();

    private AppTaskEngine(){}

    public static Future<?> submit(final Task task){
        return pool.submit(task);
    }
}