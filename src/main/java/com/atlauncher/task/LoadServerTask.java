package com.atlauncher.task;

import com.atlauncher.Gsons;
import com.atlauncher.data.Server;
import com.atlauncher.managers.ServerManager;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public final class LoadServerTask implements Task{
    private static final Logger LOG = LogManager.getLogger(LoadServerTask.class);

    private final Path source;
    private final Gson gson;
    private final CountDownLatch latch;

    LoadServerTask(@Nonnull final Path source,
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

    @Override
    public void run() {
        try(InputStream is = Files.newInputStream(this.getSource())){
            LOG.debug("loading server from {}...", this.getSource());
            final Server server = this.getGson().fromJson(new InputStreamReader(is), Server.class);
            ServerManager.addServer(server);
        } catch(Exception exc){
            LOG.error("Failed to load server from {} ", this.getSource(), exc);
        } finally{
            this.getLatch()
                .ifPresent(CountDownLatch::countDown);
        }
    }

    public static final class Builder{
        private final Path source;
        private Gson gson = Gsons.DEFAULT;
        private CountDownLatch latch = null;

        Builder(@Nonnull final Path source){
            Preconditions.checkNotNull(source);
            this.source = source;
        }

        public Builder withGson(@Nonnull final Gson gson){
            this.gson = gson;
            return this;
        }

        public Builder withLatch(@Nonnull final CountDownLatch latch){
            this.latch = latch;
            return this;
        }

        public LoadServerTask build(){
            return new LoadServerTask(this.source, this.gson, this.latch);
        }
    }

    public static Builder of(@Nonnull final Path source){
        return new Builder(source);
    }
}