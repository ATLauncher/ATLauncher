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
package com.atlauncher.gui.tabs;

import com.atlauncher.App;
import com.atlauncher.data.Account;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Authentication;
import com.atlauncher.utils.HTMLUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AccountsTab extends JPanel implements Tab {
    private static final long serialVersionUID = 2493791137600123223L;
    private final Insets TOP_INSETS = new Insets(0, 0, 20, 0);
    private final Insets BOTTOM_INSETS = new Insets(10, 0, 0, 0);
    private final Insets LABEL_INSETS = new Insets(3, 0, 3, 10);
    private final Insets FIELD_INSETS = new Insets(3, 0, 3, 0);
    private JLabel userSkin;
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

        fillerAccount = new Account(LanguageManager.localize("account.add"));

        accountsComboBox = new JComboBox<>();
        accountsComboBox.addItem(fillerAccount);
        for (Account account : AccountManager.getAccounts()) {
            accountsComboBox.addItem(account);
        }
        accountsComboBox.setSelectedIndex(0);
        accountsComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Account account = (Account) accountsComboBox.getSelectedItem();
                    if (accountsComboBox.getSelectedIndex() == 0) {
                        usernameField.setText("");
                        passwordField.setText("");
                        rememberField.setSelected(false);
                        leftButton.setText(LanguageManager.localize("common.add"));
                        rightButton.setText(LanguageManager.localize("common.clear"));
                    } else {
                        usernameField.setText(account.getUsername());
                        passwordField.setText(account.getPassword());
                        rememberField.setSelected(account.isRemembered());
                        leftButton.setText(LanguageManager.localize("common.save"));
                        rightButton.setText(LanguageManager.localize("common.delete"));
                    }
                    userSkin.setIcon(account.getMinecraftSkin());
                }
            }
        });
        bottomPanel.add(accountsComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        usernameLabel = new JLabel(LanguageManager.localize("account.usernameemail") + ":");
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
        passwordLabel = new JLabel(LanguageManager.localize("account.password") + ":");
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
        rememberLabel = new JLabel(LanguageManager.localize("account.remember") + ":");
        bottomPanel.add(rememberLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        rememberField = new JCheckBox();
        bottomPanel.add(rememberField, gbc);
        rememberField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (rememberField.isSelected()) {
                    String[] options = {LanguageManager.localize("common.yes"), LanguageManager.localize("common"
                            + ".no")};
                    int ret = JOptionPane.showOptionDialog(App.frame, HTMLUtils.centerParagraph(LanguageManager
                                    .localizeWithReplace("account" + "" +
                                            ".rememberpasswordwarning", "<br/><br/>")), LanguageManager.localize("account"
                            + ".securitywarningtitle"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                            options, options[0]);
                    if (ret != 0) {
                        rememberField.setSelected(false);
                    }
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
        leftButton = new JButton(LanguageManager.localize("common.add"));
        leftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                leftButtonActions();
            }
        });
        rightButton = new JButton(LanguageManager.localize("common.clear"));
        rightButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (accountsComboBox.getSelectedIndex() == 0) {
                    usernameField.setText("");
                    passwordField.setText("");
                    rememberField.setSelected(false);
                } else {
                    Account account = (Account) accountsComboBox.getSelectedItem();
                    int res = JOptionPane.showConfirmDialog(App.frame, LanguageManager.localizeWithReplace("account" +
                            ".deletesure", usernameField.getText()), LanguageManager

                            .localize("account.delete"), JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        AccountManager.removeAccount(account);
                        accountsComboBox.removeAllItems();
                        accountsComboBox.addItem(fillerAccount);
                        for (Account accountt : AccountManager.getAccounts()) {
                            accountsComboBox.addItem(accountt);
                        }
                        accountsComboBox.setSelectedIndex(0);
                    }
                }
            }
        });
        buttons.add(leftButton);
        buttons.add(rightButton);
        bottomPanel.add(buttons, gbc);

        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(bottomPanel, BorderLayout.CENTER);

        contextMenu = new JPopupMenu();

        updateSkin = new JMenuItem(LanguageManager.localize("account.reloadskin"));
        updateSkin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Account account = ((Account) accountsComboBox.getSelectedItem());
                account.updateSkin();
                userSkin.setIcon(account.getMinecraftSkin());
            }
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
        add(userSkin, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void leftButtonActions() {
        if (App.settings.isInOfflineMode()) {
            String[] options = {LanguageManager.localize("common.ok")};
            JOptionPane.showOptionDialog(App.frame, LanguageManager.localize("account" + "" +
                    ".offlinemode"), LanguageManager.localize("common.offline"), JOptionPane
                    .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        } else {
            Account account;
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            boolean remember = rememberField.isSelected();
            if (AccountManager.getAccountByName(username) != null && accountsComboBox.getSelectedIndex() == 0) {
                String[] options = {LanguageManager.localize("common.ok")};
                JOptionPane.showOptionDialog(App.frame, LanguageManager.localize("account" + "" +
                        ".exists"), LanguageManager.localize("account.notadded"), JOptionPane
                        .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                return;
            }

            LogManager.info("Logging into Minecraft!");
            final ProgressDialog dialog = new ProgressDialog(LanguageManager.localize("account" + "" +
                    ".loggingin"), 0, LanguageManager.localize("account.loggingin"), "Aborting login for " +
                    usernameField.getText());
            dialog.addThread(new Thread() {
                public void run() {
                    // TODO: Change this to use Mojang authlib.
                    LoginResponse resp = Authentication.checkAccount(usernameField.getText(), new String
                            (passwordField.getPassword()));
                    dialog.setReturnValue(resp);
                    dialog.close();
                }
            });
            dialog.start();
            LoginResponse response = (LoginResponse) dialog.getReturnValue();
            if (response != null && response.hasAuth() && response.isValidAuth()) {
                if (accountsComboBox.getSelectedIndex() == 0) {
                    account = new Account(username, password, response.getAuth().getSelectedProfile().getName(),
                            response.getAuth().getSelectedProfile().getId().toString(), remember);
                    AccountManager.addAccount(account);
                    LogManager.info("Added Account " + account);
                    String[] options = {LanguageManager.localize("common.yes"), LanguageManager.localize("common"
                            + ".no")};
                    int ret = JOptionPane.showOptionDialog(App.frame, LanguageManager.localize
                            ("account.addedswitch"), LanguageManager.localize("account.added"), JOptionPane
                            .DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                    if (ret == 0) {
                        AccountManager.switchAccount(account);
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
                    LogManager.info("Edited Account " + account);
                    String[] options = {LanguageManager.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.frame, LanguageManager.localize("account" + "" +
                                    ".editeddone"), LanguageManager.localize("account.edited"), JOptionPane
                                    .DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                }
                response.save();
                EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.AccountsChangeEvent.class));
                accountsComboBox.removeAllItems();
                accountsComboBox.addItem(fillerAccount);
                for (Account accountt : AccountManager.getAccounts()) {
                    accountsComboBox.addItem(accountt);
                }
                accountsComboBox.setSelectedItem(account);
            } else {
                LogManager.error(response.getErrorMessage());
                String[] options = {LanguageManager.localize("common.ok")};
                JOptionPane.showOptionDialog(App.frame, HTMLUtils.centerParagraph(LanguageManager
                        .localize("account.incorrect") +
                        "<br/><br/>" + response.getErrorMessage()), LanguageManager.localize("account" + "" +
                        ".notadded"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
            }
        }
    }

    @Override
    public String getTitle() {
        return LanguageManager.localize("tabs.accounts");
    }
}