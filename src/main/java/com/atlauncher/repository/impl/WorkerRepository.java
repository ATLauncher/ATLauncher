package com.atlauncher.repository.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlauncher.data.WorkerInfo;
import com.atlauncher.repository.base.IWorkerRepository;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Stores worker info in memory.
 *
 * @since 2024 / 06 / 08
 */
public class WorkerRepository implements IWorkerRepository {

    private static WorkerRepository repository;

    private final ReentrantLock lock = new ReentrantLock();

    private final BehaviorSubject<Collection<WorkerInfo>> sum = BehaviorSubject.createDefault(Collections.emptyList());

    private WorkerRepository() {
    }

    public static IWorkerRepository get() {
        if (repository == null) {
            repository = new WorkerRepository();
        }
        return repository;
    }

    @NotNull
    @Override
    public Observable<Collection<WorkerInfo>> getAll() {
        return sum;
    }

    @Override
    public synchronized void add(WorkerInfo info) {
        try {
            lock.lock();

            ArrayList<WorkerInfo> newInfo = new ArrayList(sum.getValue());

            if (newInfo.add(info)) {
                sum.onNext(newInfo);
            }
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    @Override
    public WorkerInfo get(UUID uuid) {
        Collection<WorkerInfo> workers = sum.getValue();

        for (WorkerInfo info : workers) {
            if (info.id == uuid) {
                return info;
            }
        }

        return null;
    }

    @Override
    public synchronized void update(WorkerInfo info) {
        try {
            lock.lock();

            ArrayList<WorkerInfo> newInfo = new ArrayList(sum.getValue());

            if (newInfo.remove(info)) {
                if (newInfo.add(info)) {
                    sum.onNext(newInfo);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public synchronized void remove(UUID id) {
        try {
            lock.lock();

            ArrayList<WorkerInfo> newInfo = new ArrayList(sum.getValue());

            if (newInfo.removeIf((it) -> it.id == id)) {
                sum.onNext(newInfo);
            }
        } finally {
            lock.unlock();
        }
    }
}
