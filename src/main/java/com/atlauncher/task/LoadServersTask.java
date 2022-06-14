package com.atlauncher.task;

import com.atlauncher.AppTaskEngine;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.utils.Utils;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LoadServersTask implements Task{
    private static final Logger LOG = LogManager.getLogger(LoadServersTask.class);

    private final Path source;

    LoadServersTask(@Nonnull final Path source){
        Preconditions.checkNotNull(source);
        this.source = source;
    }

    public Path getSource(){
        return this.source;
    }

    private List<Path> findServerFolders() throws IOException {
        try (Stream<Path> walk = Files.walk(this.getSource())) {
            return walk
                .filter(Files::isDirectory)
                .filter(Utils.getServerFileFilterPredicate())
                .collect(Collectors.toList());
        }
    }

    private Function<Path, LoadServerTask> newTask(final CountDownLatch latch){
        return (path) -> LoadServerTask.of(path.resolve("server.json"))
            .withLatch(latch)
            .build();
    }

    @Override
    public void run() {
        try{
            PerformanceManager.start();
            LOG.debug("Loading servers");

            final List<Path> found = findServerFolders();
            final CountDownLatch latch = new CountDownLatch(found.size());

            LOG.info("loading {} servers...", found.size());
            // create and schedule tasks
            found.stream()
                .map(newTask(latch))
                .forEach(AppTaskEngine::submit);
            // wait for tasks to be done
            latch.await();
        } catch(Exception exc){
            LOG.error("error finding servers in {}", this.getSource(), exc);
        } finally {
            LOG.debug("Finished loading servers");
            PerformanceManager.end();
        }
    }

    public static final class Builder{
        private final Path source;

        Builder(@Nonnull final Path source){
            Preconditions.checkNotNull(source);
            this.source = source;
        }

        public Path getSource(){
            return this.source;
        }

        public LoadServersTask build(){
            return new LoadServersTask(this.source);
        }
    }

    public static Builder of(@Nonnull final Path source){
        return new Builder(source);
    }
}