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
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Account;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Authentication;
import com.atlauncher.utils.OS;

import org.mini2Dx.gettext.GetText;

public class AccountsTab extends JPanel implements Tab, RelocalizationListener {
    private static final long serialVersionUID = 2493791137600123223L;
    private final Insets TOP_INSETS = new Insets(0, 0, 20, 0);
    private final Insets BOTTOM_INSETS = new Insets(10, 0, 0, 0);
    private final Insets LABEL_INSETS = new Insets(3, 0, 3, 10);
    private final Insets FIELD_INSETS = new Insets(3, 0, 3, 0);
    private JLabel userSkin;
    private JPanel infoPanel;
    private JPanel rightPanel;
    private JPanel topPanel;
    private JComboBox<Account> accountsComboBox;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JLabel rememberLabel;
    private JCheckBox rememberField;
    private JPanel buttons;
    private JButton leftButton;
    private JButton rightButton;
    private JPanel bottomPanel;
    private JMenuItem updateSkin;
    private JPopupMenu contextMenu; // Right click menu
    private Account fillerAccount;

    public AccountsTab() {
        setLayout(new BorderLayout());

        RelocalizationManager.addListener(this);

        infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(60, 250, 0, 250));

        JEditorPane infoTextPane = new JEditorPane("text/html", "<html>" + GetText.tr(
                "In order to login and use ATLauncher modpacks, you must authenticate with your existing Minecraft/Mojang account. You must own and have paid for the Minecraft Java edition (not the Windows 10 edition) and use the same login here.<br><br>If you don't have an existing account, you can get one <a href=\"https://my.minecraft.net/en-us/store/minecraft/#register\">by buying Minecraft here</a>. The launcher doesn't work with cracked accounts.")
                + "</html>");
        infoTextPane.setEditable(false);
        infoTextPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OS.openWebBrowser(e.getURL());
            }
        });

        infoPanel.add(infoTextPane);

        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        topPanel = new JPanel();

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = TOP_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;

        fillerAccount = new Account(GetText.tr("Add An Account"));

        accountsComboBox = new JComboBox<>();
        accountsComboBox.addItem(fillerAccount);
        for (Account account : App.settings.getAccounts()) {
            accountsComboBox.addItem(account);
        }
        accountsComboBox.setSelectedIndex(0);
        accountsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Account account = (Account) accountsComboBox.getSelectedItem();
                if (accountsComboBox.getSelectedIndex() == 0) {
                    usernameField.setText("");
                    passwordField.setText("");
                    rememberField.setSelected(false);
                    leftButton.setText(GetText.tr("Add"));
                    rightButton.setText(GetText.tr("Clear"));
                } else {
                    usernameField.setText(account.getUsername());
                    passwordField.setText(account.getPassword());
                    rememberField.setSelected(account.isRemembered());
                    leftButton.setText(GetText.tr("Save"));
                    rightButton.setText(GetText.tr("Delete"));
                }
                userSkin.setIcon(account.getMinecraftSkin());
            }
        });
        topPanel.add(accountsComboBox, gbc);
        bottomPanel.add(accountsComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        usernameLabel = new JLabel(GetText.tr("Username/Email") + ":");
        bottomPanel.add(usernameLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        usernameField = new JTextField(16);
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    leftButtonActions();
                }
            }
        });
        bottomPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        passwordLabel = new JLabel(GetText.tr("Password") + ":");
        bottomPanel.add(passwordLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        passwordField = new JPasswordField(16);
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    leftButtonActions();
                }
            }
        });
        bottomPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        rememberLabel = new JLabel(GetText.tr("Remember Password") + ":");
        bottomPanel.add(rememberLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        rememberField = new JCheckBox();
        bottomPanel.add(rememberField, gbc);
        rememberField.addActionListener(e -> {
            if (rememberField.isSelected()) {
                int ret = DialogManager.optionDialog().setTitle(GetText.tr("Security Warning"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "Make sure you only do this on a computer you trust.<br/>If you do this on a shared computer, your password may be stolen.<br/>Do you still want to save your password?"))
                                .build())
                        .setType(DialogManager.ERROR).addOption(GetText.tr("Yes"), true).addOption(GetText.tr("No"))
                        .show();

                if (ret != 0) {
                    rememberField.setSelected(false);
                }
            }
        });

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = BOTTOM_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        leftButton = new JButton(GetText.tr("Add"));
        leftButton.addActionListener(e -> leftButtonActions());
        rightButton = new JButton(GetText.tr("Clear"));
        rightButton.addActionListener(e -> {
            if (accountsComboBox.getSelectedIndex() == 0) {
                usernameField.setText("");
                passwordField.setText("");
                rememberField.setSelected(false);
            } else {
                Account account = (Account) accountsComboBox.getSelectedItem();
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Delete"))
                        .setContent(GetText.tr("Are you sure you want to delete this account?"))
                        .setType(DialogManager.WARNING).show();
                if (ret == DialogManager.YES_OPTION) {
                    Analytics.sendEvent("Delete", "Account");
                    App.settings.removeAccount(account);
                    accountsComboBox.removeAllItems();
                    accountsComboBox.addItem(fillerAccount);
                    for (Account accountt : App.settings.getAccounts()) {
                        accountsComboBox.addItem(accountt);
                    }
                    accountsComboBox.setSelectedIndex(0);
                }
            }
        });
        buttons.add(leftButton);
        buttons.add(rightButton);
        bottomPanel.add(buttons, gbc);

        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(bottomPanel, BorderLayout.CENTER);

        contextMenu = new JPopupMenu();

        updateSkin = new JMenuItem(GetText.tr("Reload Skin"));
        updateSkin.addActionListener(e -> {
            final Account account = ((Account) accountsComboBox.getSelectedItem());
            Analytics.sendEvent("UpdateSkin", "Account");
            account.updateSkin();
            userSkin.setIcon(account.getMinecraftSkin());
        });
        contextMenu.add(updateSkin);

        userSkin = new JLabel(fillerAccount.getMinecraftSkin());
        userSkin.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (accountsComboBox.getSelectedItem() != fillerAccount) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        contextMenu.show(userSkin, e.getX(), e.getY());
                    }
                }
            }
        });
        userSkin.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 0));
        add(infoPanel, BorderLayout.NORTH);
        add(userSkin, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void leftButtonActions() {
        Account account;
        String clientToken = UUID.randomUUID().toString().replace("-", "");
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        boolean remember = rememberField.isSelected();
        if (App.settings.isAccountByName(username) && accountsComboBox.getSelectedIndex() == 0) {
            DialogManager.okDialog().setTitle(GetText.tr("Account Not Added"))
                    .setContent(GetText.tr("This account already exists.")).setType(DialogManager.ERROR).show();
            return;
        }

        LogManager.info("Logging into Minecraft!");
        final ProgressDialog dialog = new ProgressDialog(GetText.tr("Logging Into Minecraft"), 0,
                GetText.tr("Logging Into Minecraft"), "Aborting login for " + usernameField.getText());
        dialog.addThread(new Thread(() -> {
            LoginResponse resp = Authentication.checkAccount(usernameField.getText(),
                    new String(passwordField.getPassword()), clientToken);
            dialog.setReturnValue(resp);
            dialog.close();
        }));
        dialog.start();
        LoginResponse response = (LoginResponse) dialog.getReturnValue();
        if (response != null && response.hasAuth() && response.isValidAuth()) {
            if (accountsComboBox.getSelectedIndex() == 0) {
                account = new Account(username, password, response.getAuth().getSelectedProfile().getName(),
                        response.getAuth().getSelectedProfile().getId().toString(), remember, clientToken);
                account.setStore(response.getAuth().saveForStorage());
                App.settings.addAccount(account);
                Analytics.sendEvent("Add", "Account");
                LogManager.info("Added Account " + account);

                int ret = DialogManager.optionDialog().setTitle(GetText.tr("Account Added"))
                        .setContent(GetText.tr("Account added successfully. Switch to it now?"))
                        .setType(DialogManager.INFO).addOption(GetText.tr("Yes"), true).addOption(GetText.tr("No"))
                        .show();

                if (ret == 0) {
                    App.settings.switchAccount(account);
                }
            } else {
                account = (Account) accountsComboBox.getSelectedItem();
                account.setUsername(username);
                account.setMinecraftUsername(response.getAuth().getSelectedProfile().getName());
                account.setUUID(response.getAuth().getSelectedProfile().getId().toString());
                if (remember) {
                    account.setPassword(password);
                }
                account.setRemember(remember);
                account.setClientToken(clientToken);
                account.setStore(response.getAuth().saveForStorage());
                Analytics.sendEvent("Edit", "Account");
                LogManager.info("Edited Account " + account);
                DialogManager.okDialog().setTitle(GetText.tr("Account Edited"))
                        .setContent(GetText.tr("Account edited successfully")).setType(DialogManager.INFO).show();
            }
            App.settings.saveAccounts();
            App.settings.reloadAccounts();
            accountsComboBox.removeAllItems();
            accountsComboBox.addItem(fillerAccount);
            for (Account accountt : App.settings.getAccounts()) {
                accountsComboBox.addItem(accountt);
            }
            accountsComboBox.setSelectedItem(account);
        } else {
            LogManager.error(response.getErrorMessage());
            DialogManager.okDialog().setTitle(GetText.tr("Account Not Added")).setContent(new HTMLBuilder().center()
                    // #. {0} is the error message from Mojang as to why we couldn't login
                    .text(GetText.tr("Account not added as login details were incorrect.<br/><br/>{0}",
                            response.getErrorMessage()))
                    .build()).setType(DialogManager.INFO).show();
        }
    }

    @Override
    public String getTitle() {
        return GetText.tr("Accounts");
    }

    @Override
    public void onRelocalization() {
        fillerAccount.setMinecraftUsername(GetText.tr("Add An Account"));

        if (accountsComboBox.getSelectedIndex() == 0) {
            leftButton.setText(GetText.tr("Add"));
            rightButton.setText(GetText.tr("Clear"));
        } else {
            leftButton.setText(GetText.tr("Save"));
            rightButton.setText(GetText.tr("Delete"));
        }

        usernameLabel.setText(GetText.tr("Username/Email") + ":");
        passwordLabel.setText(GetText.tr("Password") + ":");
        rememberLabel.setText(GetText.tr("Remember Password") + ":");
        updateSkin.setText(GetText.tr("Reload Skin"));
    }
}
