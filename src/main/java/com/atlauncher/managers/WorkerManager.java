package com.atlauncher.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.atlauncher.data.WorkerInfo;
import com.atlauncher.repository.base.IWorkerRepository;
import com.atlauncher.repository.impl.WorkerRepository;

/**
 * Manages background workers.
 *
 * @since 2024 / 06 / 08
 */
public class WorkerManager {
    private static final HashMap<UUID, Thread> jobs = new HashMap<>();

    private static final IWorkerRepository repo = WorkerRepository.get();

    /**
     * Start a worker
     *
     * @param name Name of the worker
     * @param icon Icon to load for the worker
     * @param job  task that is given its own id.
     * @return id of the worker.
     */
    public static UUID start(String name, String icon, Worker job) {
        UUID uuid = UUID.randomUUID();
        WorkerInfo info = new WorkerInfo(uuid, name, icon, false, -1);

        Thread thread = new Thread(() -> {
            try {
                job.work(info, repo::update);
            } finally {
                stop(uuid);
            }
        });

        // Save the thread
        jobs.put(uuid, thread);

        // Start it
        thread.start();

        repo.add(info);

        // Return id of the thread
        return uuid;
    }

    public static boolean stop(UUID uuid) {
        Thread result = jobs.remove(uuid);
        if (result != null) {
            repo.remove(uuid);
            return true;
        }
        return false;
    }

    /**
     * Kill all workers
     */
    public static void stopAll() {
        Set<UUID> keys = new HashSet<>(jobs.keySet());

        jobs.forEach((id, thread) -> thread.interrupt());

        keys.forEach(jobs::remove);
        keys.forEach(repo::remove);
    }

    /**
     * Defines a worker.
     * <p>
     * Used to perform long tasks in the background.
     */
    public interface Worker {
        /**
         * Called when work is to be performed.
         *
         * @param info     info about this work.
         * @param onUpdate update info on this work.
         */
        void work(WorkerInfo info, Consumer<WorkerInfo> onUpdate);
    }
}
