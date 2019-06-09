/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
import com.atlauncher.data.Account;
import com.atlauncher.data.Language;
import com.atlauncher.data.Status;
import com.atlauncher.evnt.listener.ConsoleCloseListener;
import com.atlauncher.evnt.listener.ConsoleOpenListener;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.AccountsDropDownRenderer;
import com.atlauncher.gui.CustomLineBorder;
import com.atlauncher.gui.dialogs.GithubIssueReporterDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
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
public class LauncherBottomBar extends BottomBar implements RelocalizationListener {
    private final JButton submitError = new JButton("Submit Bug");
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
        submitError.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new GithubIssueReporterDialog(null).setVisible(true);
                    }
                });
            }
        });

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
        RelocalizationManager.addListener(this);
    }

    /**
     * Sets up the listeners on the buttons
     */
    private void setupListeners() {
        toggleConsole.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.getConsole().setVisible(!App.settings.isConsoleVisible());
            }
        });
        openFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openExplorer(App.settings.getBaseDir());
            }
        });
        updateData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final ProgressDialog dialog = new ProgressDialog(Language.INSTANCE.localize("common" + "" +
                        ".checkingforupdates"), 0, Language.INSTANCE.localize("common.checkingforupdates"), "Aborting" +
                        " Update Check!");
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
                        App.settings.switchAccount((Account) username.getSelectedItem());
                    }
                }
            }
        });
        ConsoleCloseManager.addListener(new ConsoleCloseListener() {
            @Override
            public void onConsoleClose() {
                toggleConsole.setText(Language.INSTANCE.localize("console.show"));
            }
        });
        ConsoleOpenManager.addListener(new ConsoleOpenListener() {
            @Override
            public void onConsoleOpen() {
                toggleConsole.setText(Language.INSTANCE.localize("console.hide"));
            }
        });
    }

    /**
     * Creates the JButton's for use in the bar
     */
    private void createButtons() {
        if (App.settings.isConsoleVisible()) {
            toggleConsole = new JButton(Language.INSTANCE.localize("console.hide"));
        } else {
            toggleConsole = new JButton(Language.INSTANCE.localize("console.show"));
        }

        openFolder = new JButton(Language.INSTANCE.localize("common.openfolder"));
        updateData = new JButton(Language.INSTANCE.localize("common.updatedata"));

        username = new JComboBox<Account>();
        username.setRenderer(new AccountsDropDownRenderer());
        fillerAccount = new Account(Language.INSTANCE.localize("account.select"));
        username.addItem(fillerAccount);
        for (Account account : App.settings.getAccounts()) {
            username.addItem(account);
        }
        Account active = App.settings.getAccount();
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
        statusIcon.setToolTipText(Language.INSTANCE.localize("status.minecraft.checking"));
    }

    /**
     * Update the status icon to show the current Minecraft server status.
     *
     * @param status The status of servers
     */
    public void updateStatus(Status status) {
        switch (status) {
            case UNKNOWN:
                statusIcon.setToolTipText(Language.INSTANCE.localize("status.minecraft.checking"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusWhite.png"));
                break;
            case ONLINE:
                statusIcon.setToolTipText(Language.INSTANCE.localize("status.minecraft.online"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusGreen.png"));
                break;
            case OFFLINE:
                statusIcon.setToolTipText(Language.INSTANCE.localize("status.minecraft.offline"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusRed.png"));
                break;
            case PARTIAL:
                statusIcon.setToolTipText(Language.INSTANCE.localize("status.minecraft.partial"));
                statusIcon.setIcon(Utils.getIconImage("/assets/image/StatusYellow.png"));
                break;
            default:
                break;
        }
    }

    public void reloadAccounts() {
        dontSave = true;
        username.removeAllItems();
        username.addItem(fillerAccount);
        for (Account account : App.settings.getAccounts()) {
            username.addItem(account);
        }
        if (App.settings.getAccount() == null) {
            username.setSelectedIndex(0);
        } else {
            username.setSelectedItem(App.settings.getAccount());
        }
        dontSave = false;
    }

    @Override
    public void onRelocalization() {
        if (App.settings.getConsole().isVisible()) {
            toggleConsole.setText(Language.INSTANCE.localize("console.hide"));
        } else {
            toggleConsole.setText(Language.INSTANCE.localize("console.show"));
        }
        this.updateData.setText(Language.INSTANCE.localize("common.updatedata"));
        this.openFolder.setText(Language.INSTANCE.localize("common.openfolder"));
        this.fillerAccount.setMinecraftUsername(Language.INSTANCE.localize("account.select"));
    }
}
