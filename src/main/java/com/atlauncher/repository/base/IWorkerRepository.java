package com.atlauncher.repository.base;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlauncher.data.WorkerInfo;

import io.reactivex.rxjava3.core.Observable;

/**
 * SOT of any current workers that ATLauncher has running.
 *
 * @since 2024 / 06 / 08
 */
public interface IWorkerRepository {
    /**
     * Observe worker states.
     *
     * @return Live list of worker states.
     */
    @Nonnull
    Observable<Collection<WorkerInfo>> getAll();

    /**
     * Add a worker.
     * <p>
     * This does <b>NOT</b> start a worker, it simply adds the information about it.
     *
     * @param info of the worker.
     */
    void add(WorkerInfo info);

    /**
     * Get a worker by its id.
     *
     * @param uuid of the worker
     * @return the worker, or null
     */
    @Nullable
    WorkerInfo get(UUID uuid);

    /**
     * Update the state of a worker.
     *
     * @param info new info of the worker.
     */
    void update(WorkerInfo info);

    /**
     * Remove a worker.
     * <p>
     * This does <b>NOT</b> stop a worker, it simply removes the information about it.
     *
     * @param id UUID of the worker info.
     */
    void remove(UUID id);
}
