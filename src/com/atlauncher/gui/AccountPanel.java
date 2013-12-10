/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

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

import com.atlauncher.App;
import com.atlauncher.data.Account;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.atlauncher.utils.Authentication;

public class AccountPanel extends JPanel {

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

    private final Insets TOP_INSETS = new Insets(0, 0, 20, 0);
    private final Insets BOTTOM_INSETS = new Insets(10, 0, 0, 0);
    private final Insets LABEL_INSETS = new Insets(3, 0, 3, 10);
    private final Insets FIELD_INSETS = new Insets(3, 0, 3, 0);

    public AccountPanel() {
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

        fillerAccount = new Account(App.settings.getLocalizedString("account.add"));

        accountsComboBox = new JComboBox<Account>();
        accountsComboBox.addItem(fillerAccount);
        for (Account account : App.settings.getAccounts()) {
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
                        leftButton.setText(App.settings.getLocalizedString("common.add"));
                        rightButton.setText(App.settings.getLocalizedString("common.clear"));
                    } else {
                        usernameField.setText(account.getUsername());
                        passwordField.setText(account.getPassword());
                        rememberField.setSelected(account.isRemembered());
                        leftButton.setText(App.settings.getLocalizedString("common.save"));
                        rightButton.setText(App.settings.getLocalizedString("common.delete"));
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
        usernameLabel = new JLabel(App.settings.getLocalizedString("account.username") + ":");
        bottomPanel.add(usernameLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        usernameField = new JTextField(16);
        bottomPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        passwordLabel = new JLabel(App.settings.getLocalizedString("account.password") + ":");
        bottomPanel.add(passwordLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        passwordField = new JPasswordField(16);
        bottomPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        rememberLabel = new JLabel(App.settings.getLocalizedString("account.remember") + ":");
        bottomPanel.add(rememberLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        rememberField = new JCheckBox();
        bottomPanel.add(rememberField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = BOTTOM_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        leftButton = new JButton(App.settings.getLocalizedString("common.add"));
        leftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInOfflineMode()) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("account.offlinemode"),
                            App.settings.getLocalizedString("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    Account account;
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());
                    boolean remember = rememberField.isSelected();
                    if (App.settings.isAccountByName(username)
                            && accountsComboBox.getSelectedIndex() == 0) {
                        String[] options = { App.settings.getLocalizedString("common.ok") };
                        JOptionPane.showOptionDialog(App.settings.getParent(),
                                App.settings.getLocalizedString("account.exists"),
                                App.settings.getLocalizedString("account.notadded"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
                        return;
                    }

                    App.settings.log("Logging into Minecraft!");
                    final ProgressDialog dialog = new ProgressDialog(App.settings
                            .getLocalizedString("account.loggingin"), 0, App.settings
                            .getLocalizedString("account.loggingin"), "Aborting login for "
                            + usernameField.getText());
                    dialog.addThread(new Thread() {
                        public void run() {
                            try {
                                dialog.setReturnValue(Authentication.checkAccount(
                                        usernameField.getText(),
                                        new String(passwordField.getPassword())));
                            } catch (IOException e1) {
                                App.settings.logStackTrace(e1);
                            }
                            dialog.close();
                        };
                    });
                    dialog.start();
                    AuthenticationResponse response = (AuthenticationResponse) dialog
                            .getReturnValue();
                    if (!response.hasError()) {
                        AuthenticationResponse resp = (AuthenticationResponse) response;
                        if (accountsComboBox.getSelectedIndex() == 0) {
                            account = new Account(username, password, resp.getSelectedProfile()
                                    .getName(), remember);
                            App.settings.getAccounts().add(account);
                            App.settings.log("Added Account " + account);
                            String[] options = { App.settings.getLocalizedString("common.yes"),
                                    App.settings.getLocalizedString("common.no") };
                            int ret = JOptionPane.showOptionDialog(App.settings.getParent(),
                                    App.settings.getLocalizedString("account.addedswitch"),
                                    App.settings.getLocalizedString("account.added"),
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                    null, options, options[0]);
                            if (ret == 0) {
                                App.settings.switchAccount(account);
                            }
                        } else {
                            account = (Account) accountsComboBox.getSelectedItem();
                            account.setUsername(username);
                            account.setMinecraftUsername(resp.getSelectedProfile().getName());
                            if (remember) {
                                account.setPassword(password);
                            }
                            account.setRemember(remember);
                            App.settings.log("Edited Account " + account);
                            String[] options = { App.settings.getLocalizedString("common.ok") };
                            JOptionPane.showOptionDialog(App.settings.getParent(),
                                    App.settings.getLocalizedString("account.editeddone"),
                                    App.settings.getLocalizedString("account.edited"),
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                    null, options, options[0]);
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
                        App.settings.log(response.getErrorMessage(), LogMessageType.error, false);
                        String[] options = { App.settings.getLocalizedString("common.ok") };
                        JOptionPane.showOptionDialog(App.settings.getParent(), "<html><center>"
                                + App.settings.getLocalizedString("account.incorrect")
                                + "<br/><br/>" + response.getErrorMessage() + "</center></html>",
                                App.settings.getLocalizedString("account.notadded"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
                    }
                }
            }
        });
        rightButton = new JButton("Clear");
        rightButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (accountsComboBox.getSelectedIndex() == 0) {
                    usernameField.setText("");
                    passwordField.setText("");
                    rememberField.setSelected(false);
                } else {
                    Account account = (Account) accountsComboBox.getSelectedItem();
                    int res = JOptionPane.showConfirmDialog(
                            App.settings.getParent(),
                            App.settings.getLocalizedString("account.deletesure",
                                    usernameField.getText()),
                            App.settings.getLocalizedString("account.delete"),
                            JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        App.settings.removeAccount(account);
                        accountsComboBox.removeAllItems();
                        accountsComboBox.addItem(fillerAccount);
                        for (Account accountt : App.settings.getAccounts()) {
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

        updateSkin = new JMenuItem(App.settings.getLocalizedString("account.reloadskin"));
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
                if (((Account) accountsComboBox.getSelectedItem()) != fillerAccount) {
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
}