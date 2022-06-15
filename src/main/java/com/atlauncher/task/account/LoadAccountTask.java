package com.atlauncher.task.account;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.task.LoadAccountsTask;
import com.atlauncher.task.ResourceTask;
import com.atlauncher.task.Task;
import com.atlauncher.utils.Utils;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public final class LoadAccountTask extends ResourceTask {
    private static final Logger LOG = LogManager.getLogger(LoadAccountsTask.class);

    LoadAccountTask(@NotNull final Path file,
                    @NotNull final Gson gson,
                    @Nullable final CountDownLatch latch){
        super(file, gson, latch);
    }

    @Override
    public void run() {
        try(final BufferedReader reader = Files.newBufferedReader(this.getFile())){
            final AbstractAccount account = this.getGson().fromJson(reader, AbstractAccount.class);
            LOG.debug("account loaded for {}", account);

            if(isMojangAccount(account)){
                decryptMojangAccountPassword((MojangAccount)account);
            }

            AccountManager.addAccount(account);
            if(isLastAccount(account))
                AccountManager.setSelectedAccount(account);
        } catch(Exception exc){
            LOG.error("error loading account from {}", this.getFile(), exc);
        } finally{
            this.getLatch().ifPresent(CountDownLatch::countDown);
        }
    }

    private static boolean isLastAccount(final AbstractAccount account){
        return account.username.equalsIgnoreCase(App.settings.lastAccount);
    }

    private static boolean isMojangAccount(final AbstractAccount account){
        return account instanceof MojangAccount;
    }

    private static void decryptMojangAccountPassword(final MojangAccount account){
        if(account.encryptedPassword == null){
            account.password = "";
            account.remember = false;
        } else{
            account.password = Utils.decrypt(account.encryptedPassword);
            if(account.password == null){
                LOG.error("error decrypting password saved in file.");
                account.password = "";
                account.remember = false;
            }
        }
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

        public LoadAccountTask build(){
            return new LoadAccountTask(this.file, this.gson, this.latch);
        }
    }

    public static Builder of(@Nonnull final Path file){
        return new Builder(file);
    }
}