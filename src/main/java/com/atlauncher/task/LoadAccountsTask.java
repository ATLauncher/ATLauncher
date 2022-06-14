package com.atlauncher.task;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.managers.AccountManager;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public final class LoadAccountsTask implements Task{
    private static final Logger LOG = LogManager.getLogger(LoadAccountsTask.class);

    private final Path file;
    private final Gson gson;
    private final CountDownLatch latch;

    LoadAccountsTask(@Nonnull final Path file,
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

    private Optional<CountDownLatch> getLatch(){
        return Optional.ofNullable(this.latch);
    }

    @Override
    public void run() {
        if(!Files.exists(this.getFile())){
            this.getLatch()
                .ifPresent(CountDownLatch::countDown);
            return;
        }

        try(InputStream is = Files.newInputStream(this.getFile())){
            final List<AbstractAccount> accounts = this.getGson().fromJson(new InputStreamReader(is), AccountManager.ACCOUNT_LIST_TYPE);

            if(!accounts.isEmpty()){
                accounts.stream()
                    .filter(LoadAccountsTask::isMojangAccount)
                    .map((acc) -> (MojangAccount)acc)
                    .forEach(LoadAccountsTask::decryptMojangAccountPassword);

                final Optional<AbstractAccount> account = accounts.stream()
                    .filter(LoadAccountsTask::isLastAccount)
                    .findAny();

                AccountManager.setAccounts(accounts);
                AccountManager.setSelectedAccount(account.orElseGet(() -> accounts.get(0)));
            }
        } catch (IOException exc) {
            LOG.error("error loading accounts: ", exc);
        } finally{
            //TODO: notify listeners
            this.getLatch()
                .ifPresent(CountDownLatch::countDown);
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

        public LoadAccountsTask build(){
            return new LoadAccountsTask(this.file, this.gson, this.latch);
        }
    }

    public static Builder of(@Nonnull final Path file){
        return new Builder(file);
    }
}