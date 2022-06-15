package com.atlauncher.task;

import com.atlauncher.App;
import com.atlauncher.AppTaskEngine;
import com.atlauncher.Gsons;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.task.account.LoadAccountTask;
import com.atlauncher.utils.Utils;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LoadAccountsTask implements Task{
    private static final Logger LOG = LogManager.getLogger(LoadAccountsTask.class);
    private final Path source;
    private final Gson gson;
    private final CountDownLatch latch;

    LoadAccountsTask(@Nonnull final Path source,
                     @Nonnull final Gson gson,
                     @Nullable final CountDownLatch latch){
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(gson);
        this.source = source;
        this.gson = gson;
        this.latch = latch;
    }

    public Path getSource(){
        return this.source;
    }

    public Gson getGson(){
        return this.gson;
    }

    public Optional<CountDownLatch> getLatch(){
        return Optional.ofNullable(this.latch);
    }

    private List<Path> findAccountFiles() throws IOException{
        try(Stream<Path> walk = Files.walk(this.getSource())){
            return walk
                .filter(Files::isRegularFile)
                .filter(Utils.getAccountFilePredicate())
                .collect(Collectors.toList());
        }
    }

    @Override
    public void run() {
        try{
            final List<Path> found = this.findAccountFiles();
            final CountDownLatch latch = new CountDownLatch(found.size());

            LOG.info("loading {} accounts....", found.size());
            found.stream()
                .map(createTask(latch))
                .forEach(App.TASK_ENGINE::submit);

            latch.await();
        } catch(Exception exc){
            LOG.error("error loading accounts in {}", this.getSource(), exc);
        } finally{
            LOG.debug("Finished loading accounts.");
            this.getLatch().ifPresent(CountDownLatch::countDown);
        }
    }

    private Function<Path, LoadAccountTask> createTask(final CountDownLatch latch){
        return (path) -> LoadAccountTask.of(path)
            .withLatch(latch)
            .build();
    }

    public static final class Builder{
        private final Path file;
        private Gson gson = Gsons.DEFAULT;
        private CountDownLatch latch = null;

        Builder(@Nonnull final Path file){
            Preconditions.checkNotNull(file);
            this.file = file;
        }

        public Builder withGson(@Nonnull final Gson gson){
            Preconditions.checkNotNull(gson);
            this.gson = gson;
            return this;
        }

        public Builder withLatch(@Nonnull final CountDownLatch latch){
            Preconditions.checkNotNull(latch);
            this.latch = latch;
            return this;
        }

        public LoadAccountsTask build(){
            return new LoadAccountsTask(this.file, this.gson, this.latch);
        }
    }

    public static Builder of(@Nonnull final Path file){
        return new Builder(file);
    }
}