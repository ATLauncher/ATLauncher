package com.atlauncher.task;

import com.atlauncher.FileSystem;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.Account;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.managers.AccountManager;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public final class ConvertAccountsTask implements Task{
    private static final Logger LOG = LogManager.getLogger(ConvertAccountsTask.class);
    private final List<AbstractAccount> converted = new LinkedList<>();

    private final Path source;

    ConvertAccountsTask(@Nonnull final Path source){
        Preconditions.checkNotNull(source);
        this.source = source;
    }

    private Path getSource(){
        return this.source;
    }

    @Override
    public void run() {
        try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(this.getSource()))) {
            Object next;
            while((next = ois.readObject()) != null) {
                Account account = (Account)next;
                converted.add(new MojangAccount(account.username, account.password, account.minecraftUsername, account.uuid, account.remember, account.clientToken, account.store));
            }
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Exception while trying to convert accounts from file.", e);
            return;
        }

        try {
            Files.delete(FileSystem.USER_DATA);
        } catch (IOException e) {
            LOG.error("Exception trying to remove old userdata file after conversion.", e);
            return;
        }

        AccountManager.saveAccounts();
    }

    public static final class Builder{
        private final Path source;

        Builder(@Nonnull final Path source){
            Preconditions.checkNotNull(source);
            this.source = source;
        }

        public ConvertAccountsTask build(){
            return new ConvertAccountsTask(source);
        }
    }

    public static Builder of(@Nonnull final Path source){
        return new Builder(source);
    }
}