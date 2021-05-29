/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.evnt.listener.AccountListener;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.AccountsDropDownRenderer;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class LauncherBottomBar extends BottomBar implements RelocalizationListener, AccountListener {
    private boolean dontSave = false;
    private JButton toggleConsole;
    private JButton openFolder;
    private JButton updateData;
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
        leftSide.add(updateData, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        middle.add(username, gbc);

        username.setVisible(AccountManager.getAccounts().size() != 0);

        add(leftSide, BorderLayout.WEST);
        add(middle, BorderLayout.CENTER);
        RelocalizationManager.addListener(this);
        com.atlauncher.evnt.manager.AccountManager.addListener(this);
    }

    /**
     * Sets up the listeners on the buttons
     */
    private void setupListeners() {
        toggleConsole.addActionListener(e -> App.console.setVisible(!App.console.isVisible()));
        openFolder.addActionListener(e -> OS.openFileExplorer(FileSystem.BASE_DIR));
        updateData.addActionListener(e -> {
            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Checking For Updates"), 0,
                    GetText.tr("Checking For Updates"), "Aborting Update Check!");
            dialog.addThread(new Thread(() -> {
                Analytics.sendEvent("UpdateData", "Launcher");
                App.launcher.updateData();
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
        ConsoleCloseManager.addListener(() -> toggleConsole.setText(GetText.tr("Show Console")));
        ConsoleOpenManager.addListener(() -> toggleConsole.setText(GetText.tr("Hide Console")));
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

        updateData = new JButton(GetText.tr("Update Data"));
        updateData.setName("updateData");

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

    @Override
    public void onRelocalization() {
        if (App.console.isVisible()) {
            toggleConsole.setText(GetText.tr("Hide Console"));
        } else {
            toggleConsole.setText(GetText.tr("Show Console"));
        }
        this.updateData.setText(GetText.tr("Update Data"));
        this.openFolder.setText(GetText.tr("Open Folder"));
    }

    @Override
    public void onAccountsChanged() {
        reloadAccounts();
    }
}
