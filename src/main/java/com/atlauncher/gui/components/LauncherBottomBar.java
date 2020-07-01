/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
import com.atlauncher.data.Account;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.AccountsDropDownRenderer;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class LauncherBottomBar extends BottomBar implements RelocalizationListener {
    private JPanel leftSide;
    private JPanel middle;
    private boolean dontSave = false;
    private JButton toggleConsole;
    private JButton openFolder;
    private JButton updateData;
    private JComboBox<Account> username;

    public LauncherBottomBar() {
        leftSide = new JPanel();
        leftSide.setLayout(new GridBagLayout());
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        createButtons();
        setupListeners();

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 5, 0, 5);
        leftSide.add(toggleConsole, gbc);
        gbc.gridx++;
        leftSide.add(openFolder, gbc);
        gbc.gridx++;
        leftSide.add(updateData, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        middle.add(username, gbc);

        username.setVisible(App.settings.getAccounts().size() != 0);

        add(leftSide, BorderLayout.WEST);
        add(middle, BorderLayout.CENTER);
        RelocalizationManager.addListener(this);
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
                if (App.settings.checkForUpdatedFiles()) {
                    App.settings.reloadLauncherData();
                }
                dialog.close();
            }));
            dialog.start();
        });
        username.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (!dontSave) {
                    Analytics.sendEvent("Switch", "Account");
                    App.settings.switchAccount((Account) username.getSelectedItem());
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

        username = new JComboBox<>();
        username.setRenderer(new AccountsDropDownRenderer());

        for (Account account : App.settings.getAccounts()) {
            username.addItem(account);
        }

        Account active = App.settings.getAccount();

        if (active != null) {
            username.setSelectedItem(active);
        }
    }

    public void reloadAccounts() {
        dontSave = true;
        username.removeAllItems();

        for (Account account : App.settings.getAccounts()) {
            username.addItem(account);
        }

        if (App.settings.getAccount() != null) {
            username.setSelectedItem(App.settings.getAccount());
        }

        username.setVisible(App.settings.getAccounts().size() != 0);

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
}
