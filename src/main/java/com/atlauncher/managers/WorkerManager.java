package com.atlauncher.managers;

import java.util.HashMap;
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
    public static UUID start(String name, String icon, Consumer<UUID> job) {
        UUID uuid = UUID.randomUUID();
        Thread thread = new Thread(() -> job.accept(uuid));

        // Save the thread
        jobs.put(uuid, thread);

        // Start it
        thread.start();

        repo.add(new WorkerInfo(uuid, name, icon, false, -1));

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
        Set<UUID> keys = jobs.keySet();

        jobs.forEach((id, thread) -> thread.interrupt());

        keys.forEach(jobs::remove);
        keys.forEach(repo::remove);
    }
}
