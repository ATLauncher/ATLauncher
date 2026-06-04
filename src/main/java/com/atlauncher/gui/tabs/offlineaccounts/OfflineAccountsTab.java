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
package com.atlauncher.gui.tabs.offlineaccounts;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.OfflineAccount;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.gui.tabs.Tab;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.OfflineAccountManager;

public class OfflineAccountsTab extends HierarchyPanel implements Tab {
    private static final long serialVersionUID = 1L;

    private JComboBox<OfflineAccount> accountsComboBox;
    private JButton deleteButton;
    private boolean reloading = false;

    public OfflineAccountsTab() {
        super(new BorderLayout());
    }

    @Override
    protected void onShow() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(60, 250, 0, 250));

        JEditorPane infoTextPane = new JEditorPane("text/html", new HTMLBuilder().center().text(GetText.tr(
                "Offline accounts let you play without a Microsoft account. They cannot connect to "
                        + "online (premium) servers. Add an account with a username, then use \"Play Offline\" "
                        + "on an instance."))
                .build());
        infoTextPane.setEditable(false);
        infoTextPane.setFocusable(false);
        infoPanel.add(infoTextPane);

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        accountsComboBox = new JComboBox<>();
        accountsComboBox.setName("offlineAccountsComboBox");
        accountsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && !reloading) {
                OfflineAccount selected = (OfflineAccount) accountsComboBox.getSelectedItem();
                if (selected != null) {
                    OfflineAccountManager.switchAccount(selected);
                }
                deleteButton.setVisible(selected != null);
            }
        });
        bottomPanel.add(accountsComboBox, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 0, 0);
        JPanel buttons = new JPanel(new FlowLayout());

        JButton addButton = new JButton(GetText.tr("Add Offline Account"));
        addButton.addActionListener(e -> onAdd());

        deleteButton = new JButton(GetText.tr("Delete"));
        deleteButton.addActionListener(e -> onDelete());

        buttons.add(addButton);
        buttons.add(deleteButton);
        bottomPanel.add(buttons, gbc);

        add(infoPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);

        reloadAccounts();
    }

    @Override
    protected void createViewModel() {
        // no view model; this tab talks to OfflineAccountManager directly
    }

    @Override
    protected void onDestroy() {
        removeAll();
        accountsComboBox = null;
        deleteButton = null;
    }

    private void reloadAccounts() {
        reloading = true;
        try {
            accountsComboBox.removeAllItems();
            for (OfflineAccount account : OfflineAccountManager.getAccounts()) {
                accountsComboBox.addItem(account);
            }
            OfflineAccount selected = OfflineAccountManager.getSelectedAccount();
            if (selected != null) {
                accountsComboBox.setSelectedItem(selected);
            }
            deleteButton.setVisible(selected != null);
        } finally {
            reloading = false;
        }
    }

    private void onAdd() {
        String name = DialogManager.okDialog().setTitle(GetText.tr("Add Offline Account"))
                .setContent(GetText.tr("Enter a username for the offline account:"))
                .showInput("");

        if (name == null) {
            return; // cancelled
        }
        name = name.trim();

        if (!name.matches("[A-Za-z0-9_]{1,16}")) {
            DialogManager.okDialog().setTitle(GetText.tr("Invalid Username"))
                    .setContent(GetText.tr(
                            "Usernames must be 1-16 characters and only contain letters, numbers and underscores."))
                    .setType(DialogManager.ERROR).show();
            return;
        }

        if (OfflineAccountManager.isAccountByName(name)) {
            DialogManager.okDialog().setTitle(GetText.tr("Account Exists"))
                    .setContent(GetText.tr("An offline account with that username already exists."))
                    .setType(DialogManager.ERROR).show();
            return;
        }

        OfflineAccountManager.addAccount(new OfflineAccount(name));
        reloadAccounts();
    }

    private void onDelete() {
        OfflineAccount selected = (OfflineAccount) accountsComboBox.getSelectedItem();
        if (selected == null) {
            return;
        }

        int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Delete"))
                .setContent(GetText.tr("Are you sure you want to delete this offline account?"))
                .setType(DialogManager.WARNING).show();

        if (ret == DialogManager.YES_OPTION) {
            OfflineAccountManager.removeAccount(selected);
            reloadAccounts();
        }
    }

    @Override
    public String getTitle() {
        return GetText.tr("Offline Accounts");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Offline Accounts";
    }
}
