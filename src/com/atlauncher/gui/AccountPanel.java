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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

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

        fillerAccount = new Account("", "", "Add New Account", false);

        accountsComboBox = new JComboBox<Account>();
        accountsComboBox.addItem(fillerAccount);
        for (Account account : LauncherFrame.settings.getAccounts()) {
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
                        leftButton.setText("Add");
                        rightButton.setText("Clear");
                    } else {
                        usernameField.setText(account.getUsername());
                        passwordField.setText(account.getPassword());
                        rememberField.setSelected(account.isRemembered());
                        leftButton.setText("Save");
                        rightButton.setText("Delete");
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
        usernameLabel = new JLabel("Username:");
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
        passwordLabel = new JLabel("Password:");
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
        rememberLabel = new JLabel("Remember Password:");
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
        leftButton = new JButton("Add");
        leftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Account account;
                if (accountsComboBox.getSelectedIndex() == 0) {
                    account = new Account(usernameField.getText(), passwordField
                            .getPassword().toString(), usernameField.getText(), rememberField
                            .isSelected());
                    LauncherFrame.settings.getAccounts().add(account);
                    JOptionPane.showConfirmDialog(LauncherFrame.settings.getParent(),
                            "Account Added Successfully", "Account Added", JOptionPane.OK_OPTION);
                } else {
                    account = (Account) accountsComboBox.getSelectedItem();
                    account.setUsername(usernameField.getText());
                    account.setPassword(new String(passwordField.getPassword()));
                    account.setRemember(rememberField.isSelected());
                }
                LauncherFrame.settings.saveAccounts();
                accountsComboBox.removeAllItems();
                accountsComboBox.addItem(fillerAccount);
                for (Account accountt : LauncherFrame.settings.getAccounts()) {
                    accountsComboBox.addItem(accountt);
                }
                accountsComboBox.setSelectedItem(account);
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
                    int res = JOptionPane.showConfirmDialog(LauncherFrame.settings.getParent(),
                            "Are you sure you want to delete the user " + usernameField.getText()
                                    + "?", "Delete User", JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        LauncherFrame.settings.getAccounts().remove(account);
                        LauncherFrame.settings.reloadAccounts();
                    }
                    LauncherFrame.settings.saveAccounts();
                    accountsComboBox.removeAllItems();
                    accountsComboBox.addItem(fillerAccount);
                    for (Account accountt : LauncherFrame.settings.getAccounts()) {
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

        userSkin = new JLabel(fillerAccount.getMinecraftSkin());
        userSkin.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 0));
        add(userSkin, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    public void reload() {

    }

}