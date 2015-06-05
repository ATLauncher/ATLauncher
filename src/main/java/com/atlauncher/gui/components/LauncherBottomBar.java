/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
import com.atlauncher.annot.Subscribe;
import com.atlauncher.data.Account;
import com.atlauncher.data.Status;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.AccountsDropDownRenderer;
import com.atlauncher.gui.CustomLineBorder;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.utils.Utils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * TODO: Rewrite with the other @link BottomBar classes
 */

@SuppressWarnings("serial")
public class LauncherBottomBar extends BottomBar {
    private JPanel leftSide;
    private JPanel middle;
    private Account fillerAccount;
    private boolean dontSave = false;
    private JButton toggleConsole;
    private JButton openFolder;
    private JButton updateData;
    private JComboBox<Account> username;

    private JLabel statusIcon;

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
        gbc.insets = new Insets(0, 0, 0, 5);
        leftSide.add(toggleConsole, gbc);
        gbc.gridx++;
        leftSide.add(openFolder, gbc);
        gbc.gridx++;
        leftSide.add(updateData, gbc);
        // gbc.gridx++;
        // leftSide.add(submitError, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        middle.add(username, gbc);
        gbc.gridx++;
        middle.add(statusIcon, gbc);

        add(leftSide, BorderLayout.WEST);
        add(middle, BorderLayout.CENTER);
        EventHandler.EVENT_BUS.subscribe(this);
    }

    /**
     * Sets up the listeners on the buttons
     */
    private void setupListeners() {
        toggleConsole.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.console.setVisible(!App.console.isVisible());
            }
        });

        openFolder.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Utils.openExplorer(FileSystem.BASE_DIR);
                    }
                });

        updateData.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        final ProgressDialog dialog = new ProgressDialog(LanguageManager.localize("common" + "" +
                                ".checkingforupdates"), 0, LanguageManager.localize("common" +
                                ".checkingforupdates"), "Aborting" + " Update Check!");
                        dialog.addThread(new Thread() {
                                    public void run() {
                                        if (App.settings.hasUpdatedFiles()) {
                                            App.settings.reloadLauncherData();
                                        }
                                        dialog.close();
                                    }
                                });
                        dialog.start();
                    }
                });

        username.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            if (!dontSave) {
                                AccountManager.switchAccount((Account) username.getSelectedItem());
                            }
                        }
                    }
                });

/*        ConsoleCloseManager.addListener(new ConsoleCloseListener() {
            @Override
            public void onConsoleClose() {
                toggleConsole.setText(LanguageManager.localize("console.show"));
            }
        });

        ConsoleOpenManager.addListener(new ConsoleOpenListener() {
            @Override
            public void onConsoleOpen() {
                toggleConsole.setText(LanguageManager.localize("console.hide"));
            }
        });*/
    }

    @Subscribe
    private void onConsoleOpen(EventHandler.ConsoleOpenEvent e) {
        this.toggleConsole.setText(LanguageManager.localize("console.hide"));
    }

    @Subscribe
    private void onConsoleClosed(EventHandler.ConsoleCloseEvent e) {
        this.toggleConsole.setText(LanguageManager.localize("console.show"));
    }

    /**
     * Creates the JButton's for use in the bar
     */
    private void createButtons() {
        if (App.console.isVisible()) {
            toggleConsole = new JButton(LanguageManager.localize("console.hide"));
        } else {
            toggleConsole = new JButton(LanguageManager.localize("console.show"));
        }

        openFolder = new JButton(LanguageManager.localize("common.openfolder"));
        updateData = new JButton(LanguageManager.localize("common.updatedata"));

        username = new JComboBox<>();
        username.setRenderer(new AccountsDropDownRenderer());
        fillerAccount = new Account(LanguageManager.localize("account.select"));
        username.addItem(fillerAccount);
        for (Account account : AccountManager.getAccounts()) {
            username.addItem(account);
        }
        Account active = AccountManager.getActiveAccount();
        if (active == null) {
            username.setSelectedIndex(0);
        } else {
            username.setSelectedItem(active);
        }

        statusIcon = new JLabel(Utils.getIconImage("/assets/image/StatusWhite.png")) {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        statusIcon.setBorder(BorderFactory.createEmptyBorder());
        statusIcon.setToolTipText(LanguageManager.localize("status.minecraft.checking"));
    }

    /**
     * Update the status icon to show the current Minecraft SERVER status.
     *
     * @param status The status of servers
     */
    public void updateStatus(Status status) {
        switch (status) {
            case UNKNOWN:
                statusIcon.setToolTipText(LanguageManager.localize("status.minecraft.checking"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusWhite.png"));
                break;
            case ONLINE:
                statusIcon.setToolTipText(LanguageManager.localize("status.minecraft.online"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusGreen.png"));
                break;
            case OFFLINE:
                statusIcon.setToolTipText(LanguageManager.localize("status.minecraft.offline"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusRed.png"));
                break;
            case PARTIAL:
                statusIcon.setToolTipText(LanguageManager.localize("status.minecraft.partial"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusYellow.png"));
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void onRelocalization(EventHandler.RelocalizationEvent e) {
        if (App.console.isVisible()) {
            toggleConsole.setText(LanguageManager.localize("console.hide"));
        } else {
            toggleConsole.setText(LanguageManager.localize("console.show"));
        }
        this.updateData.setText(LanguageManager.localize("common.updatedata"));
        this.openFolder.setText(LanguageManager.localize("common.openfolder"));
    }

    @Subscribe
    private void onAccountsChanged(EventHandler.AccountsChangeEvent e) {
        dontSave = true;
        username.removeAllItems();
        username.addItem(fillerAccount);

        for (Account account : AccountManager.getAccounts()) {
            username.addItem(account);
        }

        if (AccountManager.getActiveAccount() == null) {
            username.setSelectedIndex(0);
        } else {
            username.setSelectedItem(AccountManager.getActiveAccount());
        }

        dontSave = false;
    }
}
