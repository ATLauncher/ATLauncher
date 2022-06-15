package com.atlauncher.task.account;

import com.atlauncher.Gsons;
import com.atlauncher.data.AbstractAccount;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

public final class SaveAccountTask extends AccountFileTask {
    private static final Logger LOG = LogManager.getLogger(SaveAccountTask.class);
    private final AbstractAccount account;

    SaveAccountTask(@Nonnull final AbstractAccount account,
                    @Nonnull final Path file,
                    @Nonnull final Gson gson,
                    @Nullable final CountDownLatch latch){
        super(file, gson, latch);
        Preconditions.checkNotNull(account);
        this.account = account;
    }

    public AbstractAccount getAccount(){
        return this.account;
    }

    @Override
    public void run() {
        LOG.debug("saving account {} to {}", this.getAccount(), this.getFile());
        try(final BufferedWriter writer = Files.newBufferedWriter(this.getFile(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);){
            this.getGson().toJson(this.getAccount(), AbstractAccount.class, writer);
        } catch (JsonIOException | IOException e) {
            LOG.error("couldn't save account {} to {}", this.getAccount(), this.getFile(), e);
        } finally{
            this.getLatch().ifPresent(CountDownLatch::countDown);
        }
    }

    public static final class Builder{
        private final AbstractAccount account;
        private final Path destination;
        private Gson gson = Gsons.DEFAULT;
        private CountDownLatch latch = null;

        Builder(@Nonnull final AbstractAccount account,
                @Nonnull final Path destination){
            Preconditions.checkNotNull(account);
            Preconditions.checkNotNull(destination);
            this.account = account;
            this.destination = destination;
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

        public SaveAccountTask build(){
            return new SaveAccountTask(this.account, this.destination, this.gson, this.latch);
        }
    }

    public static Builder of(@Nonnull final AbstractAccount account, @Nonnull final Path destination){
        return new Builder(account, destination);
    }
}