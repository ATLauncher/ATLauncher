/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.data.Account;

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
                Account account;
                boolean loggedIn = false;
                String url = null;
                String username = usernameField.getText();
                String minecraftUsername = null;
                String password = new String(passwordField.getPassword());
                boolean remember = rememberField.isSelected();
                if (App.settings.isAccountByName(username)
                        && accountsComboBox.getSelectedIndex() == 0) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("account.exists"),
                            App.settings.getLocalizedString("account.notadded"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                    return;
                }
                try {
                    url = "https://login.minecraft.net/?user="
                            + URLEncoder.encode(username, "UTF-8") + "&password="
                            + URLEncoder.encode(password, "UTF-8") + "&version=999";
                } catch (UnsupportedEncodingException e1) {
                    App.settings.getConsole().logStackTrace(e1);
                }
                String auth = Utils.urlToString(url);
                if (auth.contains(":")) {
                    String[] parts = auth.split(":");
                    if (parts.length == 5) {
                        loggedIn = true;
                        minecraftUsername = parts[2];
                    }
                }
                if (!loggedIn) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(), "<html><center>"
                            + App.settings.getLocalizedString("account.incorrect") + "<br/><br/>"
                            + auth + "</center></html>",
                            App.settings.getLocalizedString("account.notadded"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    if (accountsComboBox.getSelectedIndex() == 0) {
                        account = new Account(username, password, minecraftUsername, remember);
                        App.settings.getAccounts().add(account);
                        App.settings.getConsole().log("Added Account " + account);
                        String[] options = { App.settings.getLocalizedString("common.yes"),
                                App.settings.getLocalizedString("common.no") };
                        int ret = JOptionPane.showOptionDialog(App.settings.getParent(),
                                App.settings.getLocalizedString("account.addedswitch"),
                                App.settings.getLocalizedString("account.added"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                                options, options[0]);
                        if (ret == 0) {
                            App.settings.switchAccount(account);
                        }
                    } else {
                        account = (Account) accountsComboBox.getSelectedItem();
                        account.setUsername(username);
                        account.setMinecraftUsername(minecraftUsername);
                        if (remember) {
                            account.setPassword(password);
                        }
                        account.setRemember(remember);
                        App.settings.getConsole().log("Edited Account " + account);
                        String[] options = { App.settings.getLocalizedString("common.ok") };
                        JOptionPane.showOptionDialog(App.settings.getParent(),
                                App.settings.getLocalizedString("account.editeddone"),
                                App.settings.getLocalizedString("account.edited"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                                options, options[0]);
                    }
                    App.settings.saveAccounts();
                    App.settings.reloadAccounts();
                    accountsComboBox.removeAllItems();
                    accountsComboBox.addItem(fillerAccount);
                    for (Account accountt : App.settings.getAccounts()) {
                        accountsComboBox.addItem(accountt);
                    }
                    accountsComboBox.setSelectedItem(account);
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

        userSkin = new JLabel(fillerAccount.getMinecraftSkin());
        userSkin.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 0));
        add(userSkin, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

}