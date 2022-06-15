package com.atlauncher.task;

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.AppTaskEngine;
import com.atlauncher.Gsons;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.AccountTypeAdapter;
import com.atlauncher.data.ColorTypeAdapter;
import com.atlauncher.data.DateTypeAdapter;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.OauthTokenResponseTypeAdapter;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.task.account.LoadAccountTask;
import com.atlauncher.task.account.SaveAccountTask;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

public final class SaveAccountsTask implements Task{
    private static final Logger LOG = LogManager.getLogger(SaveAccountsTask.class);
    private final Path file;
    private final Gson gson;
    private final CountDownLatch latch;

    SaveAccountsTask(@Nonnull final Path file,
                     @Nonnull final Gson gson,
                     @Nullable final CountDownLatch latch){
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(gson);
        this.file = file;
        this.gson = gson;
        this.latch = latch;
    }

    public Path getFile(){
        return this.file;
    }

    public Gson getGson(){
        return this.gson;
    }

    public Optional<CountDownLatch> getLatch(){
        return Optional.ofNullable(this.latch);
    }

    @Override
    public void run() {
        try{
            final List<AbstractAccount> accounts = AccountManager.getAccounts();
            LOG.debug("saving {} accounts to {}", accounts.size(), this.getFile());

            final CountDownLatch latch = new CountDownLatch(accounts.size());
            accounts.stream()
                .map(createTask(latch))
                .forEach(App.TASK_ENGINE::submit);

            latch.await();
        } catch(Exception exc){
            LOG.error("error saving accounts in {}", this.getFile());
        } finally{
            LOG.debug("Finished saving accounts.");
            this.getLatch().ifPresent(CountDownLatch::countDown);
        }
    }

    private Function<AbstractAccount, SaveAccountTask> createTask(final CountDownLatch latch){
        return (account) -> SaveAccountTask.of(account, this.getFile().resolve(account.getUUIDNoDashes() + ".json"))
            .withLatch(latch)
            .build();
    }

    public static final class Builder{
        private final Path file;
        private Gson gson = new GsonBuilder()
            .registerTypeAdapter(AbstractAccount.class, new AccountTypeAdapter())
            .registerTypeAdapter(Date.class, new DateTypeAdapter())
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(OauthTokenResponse.class, new OauthTokenResponseTypeAdapter())
            .addSerializationExclusionStrategy(Gsons.exclusionAnnotationStrategy)
            .setPrettyPrinting()
            .create();
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

        public SaveAccountsTask build(){
            return new SaveAccountsTask(this.file, this.gson, this.latch);
        }
    }

    public static Builder of(@Nonnull final Path file){
        return new Builder(file);
    }
}