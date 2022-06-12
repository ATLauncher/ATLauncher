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
package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.events.AccountEvent;
import com.atlauncher.events.ConsoleEvent;
import com.atlauncher.events.LocalizationEvent;
import com.atlauncher.gui.AccountsDropDownRenderer;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

@SuppressWarnings("serial")
public class LauncherBottomBar extends BottomBar{
    private boolean dontSave = false;
    private JButton toggleConsole;
    private JButton openFolder;
    private JButton checkForUpdates;
    private JComboBox<AbstractAccount> username;

    public LauncherBottomBar() {
        JPanel leftSide = new JPanel();
        leftSide.setLayout(new GridBagLayout());
        JPanel middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        createButtons();
        setupListeners();

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        gbc.insets = new Insets(0, 5, 0, 5);
        leftSide.add(toggleConsole, gbc);

        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.gridx++;
        leftSide.add(openFolder, gbc);

        gbc.gridx++;
        leftSide.add(checkForUpdates, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        middle.add(username, gbc);

        username.setVisible(AccountManager.getAccounts().size() != 0);

        add(leftSide, BorderLayout.WEST);
        add(middle, BorderLayout.CENTER);
        App.EVENT_BUS.register(this);
    }

    /**
     * Sets up the listeners on the buttons
     */
    private void setupListeners() {
        toggleConsole.addActionListener(e -> App.console.setVisible(!App.console.isVisible()));
        openFolder.addActionListener(e -> OS.openFileExplorer(FileSystem.BASE_DIR));
        checkForUpdates.addActionListener(e -> {
            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Checking For Updates"), 0,
                    GetText.tr("Checking For Updates"), "Aborting Update Check!");
            dialog.addThread(new Thread(() -> {
                Analytics.sendEvent("UpdateData", "Launcher");
                App.launcher.updateData(true);
                dialog.close();
            }));
            dialog.start();
        });
        username.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (!dontSave) {
                    Analytics.sendEvent("Switch", "Account");
                    AccountManager.switchAccount((AbstractAccount) username.getSelectedItem());
                }
            }
        });

        App.EVENT_BUS.register(this);
    }

    @Subscribe
    public final void onConsoleOpened(final ConsoleEvent.ConsoleOpenedEvent event){
        this.toggleConsole.setText(GetText.tr("Hide Console"));
    }

    @Subscribe
    public final void onConsoleClosed(final ConsoleEvent.ConsoleClosedEvent event){
        this.toggleConsole.setText(GetText.tr("Show Console"));
    }

    /**
     * Creates the JButton's for use in the bar
     */
    private void createButtons() {
        if (App.console.isVisible()) {
            toggleConsole = new JButton(GetText.tr("Hide Console"));
        } else {
            toggleConsole = new JButton(GetText.tr("Show Console"));
        }

        openFolder = new JButton(GetText.tr("Open Folder"));

        checkForUpdates = new JButton(GetText.tr("Check For Updates"));
        checkForUpdates.setName("checkForUpdates");

        username = new JComboBox<>();
        username.setName("accountSelector");
        username.setRenderer(new AccountsDropDownRenderer());

        for (AbstractAccount account : AccountManager.getAccounts()) {
            username.addItem(account);
        }

        AbstractAccount active = AccountManager.getSelectedAccount();

        if (active != null) {
            username.setSelectedItem(active);
        }
    }

    private void reloadAccounts() {
        dontSave = true;
        username.removeAllItems();

        for (AbstractAccount account : AccountManager.getAccounts()) {
            username.addItem(account);
        }

        if (AccountManager.getSelectedAccount() != null) {
            username.setSelectedItem(AccountManager.getSelectedAccount());
        }

        username.setVisible(AccountManager.getAccounts().size() != 0);

        dontSave = false;
    }

    @Subscribe
    public final void onLocalizationChanged(final LocalizationEvent.LocalizationChangedEvent event){
        if (App.console.isVisible()) {
            toggleConsole.setText(GetText.tr("Hide Console"));
        } else {
            toggleConsole.setText(GetText.tr("Show Console"));
        }
        this.checkForUpdates.setText(GetText.tr("Check For Updates"));
        this.openFolder.setText(GetText.tr("Open Folder"));
    }

    @Subscribe
    public void onAccountChanged(final AccountEvent.AccountChangedEvent event){
        this.reloadAccounts();
    }
}
