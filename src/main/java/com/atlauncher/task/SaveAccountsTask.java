package com.atlauncher.task;

import com.atlauncher.Gsons;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.managers.AccountManager;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class SaveAccountsTask implements Task{
    private static final Logger LOG = LogManager.getLogger(SaveAccountsTask.class);
    private final Path file;
    private final Gson gson;

    SaveAccountsTask(@Nonnull final Path file,
                     @Nonnull final Gson gson){
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(gson);
        this.file = file;
        this.gson = gson;
    }

    public Path getFile(){
        return this.file;
    }

    public Gson getGson(){
        return this.gson;
    }

    @Override
    public void run() {
        final List<AbstractAccount> accounts = AccountManager.getAccounts();
        if(!accounts.isEmpty()){
            try(OutputStream os = Files.newOutputStream(this.getFile(), StandardOpenOption.CREATE_NEW)){
                LOG.debug("saving {} accounts to {}", accounts.size(), this.getFile());
                this.getGson().toJson(accounts, AccountManager.ACCOUNT_LIST_TYPE, new OutputStreamWriter(os));
            } catch (JsonIOException | IOException e) {
                LOG.error("couldn't save accounts to {}", this.getFile(), e);
                return;
            }
        }
        //TODO: notify listeners
    }

    public static final class Builder{
        private final Path file;
        private Gson gson = Gsons.DEFAULT;

        Builder(@Nonnull final Path file){
            Preconditions.checkNotNull(file);
            this.file = file;
        }

        public Builder withGson(@Nonnull final Gson gson){
            Preconditions.checkNotNull(gson);
            this.gson = gson;
            return this;
        }

        public SaveAccountsTask build(){
            return new SaveAccountsTask(this.file, this.gson);
        }
    }

    public static Builder of(@Nonnull final Path file){
        return new Builder(file);
    }
}