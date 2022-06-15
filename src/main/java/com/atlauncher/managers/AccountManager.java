/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.managers;

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.AppTaskEngine;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.events.account.AccountAddedEvent;
import com.atlauncher.events.account.AccountChangedEvent;
import com.atlauncher.events.account.AccountRemovedEvent;
import com.atlauncher.task.ConvertAccountsTask;
import com.atlauncher.task.LoadAccountsTask;
import com.atlauncher.task.SaveAccountsTask;
import com.atlauncher.task.account.SaveAccountTask;
import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("deprecation")
public class AccountManager {
    private static final Logger LOG = LogManager.getLogger(AccountManager.class);
    public static final Type ACCOUNT_LIST_TYPE = new TypeToken<List<AbstractAccount>>() {}.getType();
    private static final AtomicReference<AbstractAccount> currentRef = new AtomicReference<>(null);
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static final Set<AbstractAccount> loaded = new HashSet<>();

    public static int getNumberOfAccounts(){
        ReentrantReadWriteLock.ReadLock lock = rwLock.readLock();
        try{
            lock.lock();
            return loaded.size();
        } finally{
            lock.unlock();
        }
    }

    public static void setSelectedAccount(@Nullable final AbstractAccount account){
        currentRef.getAndSet(account);
        AppEventBus.post(AccountChangedEvent.of(account));
    }

    public static void setAccounts(@Nonnull final Collection<AbstractAccount> accounts){
        Preconditions.checkNotNull(accounts);
        Preconditions.checkArgument(accounts.size() > 0);

        ReentrantReadWriteLock.WriteLock lock = rwLock.writeLock();
        try{
            lock.lock();
            loaded.addAll(accounts);
        } finally{
            lock.unlock();
        }
    }

    public static List<AbstractAccount> getAccounts() {
        ReentrantReadWriteLock.ReadLock lock = rwLock.readLock();
        try{
            lock.lock();
            return new ArrayList<>(loaded);
        } finally{
            lock.unlock();
        }
    }

    public static AbstractAccount getSelectedAccount() {
        return currentRef.get();
    }

    /**
     * Loads the saved Accounts
     */
    public static void loadAccounts() {
        PerformanceManager.start();
        LOG.debug("Loading accounts");

        if (Files.exists(FileSystem.USER_DATA)) {
            LOG.info("Converting old account format to new format.");
            convertAccounts();
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final LoadAccountsTask task = LoadAccountsTask.of(FileSystem.ACCOUNTS)
            .withLatch(latch)
            .build();
        AppTaskEngine.submit(task);

        try{
            latch.await();
            LOG.info("selected account: {}", getSelectedAccount());
        } catch(Exception exc){
            LOG.error("failed to wait for LoadAccountsTask:", exc);
        }

        LOG.debug("Finished loading accounts");
        PerformanceManager.end();
    }

    /**
     * Converts the saved Accounts from old format to new one
     */
    private static void convertAccounts() {
        final ConvertAccountsTask task = ConvertAccountsTask.of(FileSystem.ACCOUNTS)
            .build();
        AppTaskEngine.submit(task);
    }

    public static void saveAccounts() {
        //TODO: implement
    }

    private static void saveAccount(final AbstractAccount account){
        final CountDownLatch latch = new CountDownLatch(1);
        final SaveAccountTask task = SaveAccountTask.of(account, FileSystem.ACCOUNTS.resolve(account.getUUIDNoDashes() + ".json"))
            .withGson(Gsons.DEFAULT)
            .withLatch(latch)
            .build();

        AppTaskEngine.submit(task);

        try{
            latch.await();
            LOG.info("account saved");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void registerAccount(final AbstractAccount account){
        ReentrantReadWriteLock.WriteLock lock = rwLock.writeLock();
        try{
            lock.lock();
            loaded.add(account);
        } finally{
            lock.unlock();
        }
    }

    public static void addAccount(@Nonnull final AbstractAccount account) {
        Preconditions.checkNotNull(account);

        registerAccount(account);
        if (getNumberOfAccounts() > 1) {
            // not first account? ask if they want to switch to it
            int ret = DialogManager.optionDialog()
                .setTitle(GetText.tr("Account Added"))
                .setContent(GetText.tr("Account added successfully. Switch to it now?"))
                .setType(DialogManager.INFO)
                .addOption(GetText.tr("Yes"), true)
                .addOption(GetText.tr("No")).show();

            if (ret == 0) {
                switchAccount(account);
            }
        } else {
            // first account? switch to it immediately
            switchAccount(account);
        }

        saveAccount(account);
        AppEventBus.post(AccountAddedEvent.of(account));
    }

    private static Optional<AbstractAccount> getFirstAccount(){
        ReentrantReadWriteLock.ReadLock lock = rwLock.readLock();
        try{
            lock.lock();
            return loaded.stream()
                .findFirst();
        } finally{
            lock.unlock();
        }
    }

    public static void removeAccount(AbstractAccount account) {
        if (getSelectedAccount() == account) {
            // if they have more accounts, switch to the first one
            getFirstAccount()
                .ifPresent(AccountManager::switchAccount);
        }

        loaded.remove(account);
        saveAccounts();
        AppEventBus.post(AccountRemovedEvent.of(account));
    }

    /**
     * Switch account currently used and save it
     *
     * @param account Account to switch to
     */
    public static void switchAccount(AbstractAccount account) {
        if (account == null) {
            LOG.info("Logging out of account");
            setSelectedAccount(null);
            App.settings.lastAccount = null;
        } else {
            LOG.info("Changed account to " + account);
            setSelectedAccount(account);
            App.settings.lastAccount = account.username;
        }

        App.launcher.refreshPacksBrowserPanel();
        App.launcher.reloadInstancesPanel();
        App.launcher.reloadServersPanel();

        AppEventBus.post(AccountChangedEvent.of(account));
        App.settings.save();
    }

    /**
     * Finds an Account from the given username
     *
     * @param username Username of the Account to find
     * @return Account if the Account is found from the username
     */
    public static Optional<AbstractAccount> getAccountByUsername(@Nonnull final String username){
        Preconditions.checkNotNull(username);
        return getAccounts()
            .stream()
            .filter((acc) -> acc.username.equalsIgnoreCase(username))
            .findFirst();
    }

    /**
     * Finds if an Account is available
     *
     * @param username The username of the Account
     * @return true if found, false if not
     */
    public static boolean isAccountByName(@Nonnull final String username){
        return getAccountByUsername(username).isPresent();
    }
}