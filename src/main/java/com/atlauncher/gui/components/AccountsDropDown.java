package com.atlauncher.gui.components;

import com.atlauncher.AppEventBus;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.events.account.AccountAddedEvent;
import com.atlauncher.events.account.AccountChangedEvent;
import com.atlauncher.events.account.AccountRemovedEvent;
import com.atlauncher.gui.AccountsDropDownRenderer;
import com.atlauncher.managers.AccountManager;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Optional;

public final class AccountsDropDown extends JComboBox<AbstractAccount>{
    private static final Logger LOG = LogManager.getLogger(AccountsDropDown.class);

    public AccountsDropDown(){
        this.setName("accountSelector");
        this.setRenderer(new AccountsDropDownRenderer());
        this.addItemListener((e) -> {
            if(e.getStateChange() == ItemEvent.SELECTED)
                AccountManager.switchAccount((AbstractAccount)getSelectedItem());
        });
        AccountManager.getAccounts()
            .forEach(this::addAccount);
        AppEventBus.registerToUIOnly(this);
    }

    public void addAccount(@Nonnull final AbstractAccount account){
        Preconditions.checkNotNull(account);
        LOG.debug("adding account: {}", account.username);
        this.addItem(account);
        this.setVisible(this.getItemCount() > 0);

        final Optional<AbstractAccount> selected = Optional.ofNullable(AccountManager.getSelectedAccount());
        selected.ifPresent(this::setSelectedItem);
    }

    public void removeAccount(@Nonnull final AbstractAccount account){
        Preconditions.checkNotNull(account);
        LOG.debug("removing account: {}", account.username);
        this.removeItem(account);
        this.setVisible(this.getItemCount() > 0);

        final Optional<AbstractAccount> selected = Optional.ofNullable(AccountManager.getSelectedAccount());
        selected.ifPresent(this::setSelectedItem);
    }

    @Subscribe
    public void onAccountChanged(final AccountChangedEvent event){
        this.setSelectedItem(event.getAccount());
    }

    @Subscribe
    public void onAccountAdded(final AccountAddedEvent event){
        this.addAccount(event.getAccount());
    }

    @Subscribe
    public void onAccountRemoved(final AccountRemovedEvent event){
        this.removeAccount(event.getAccount());
    }
}