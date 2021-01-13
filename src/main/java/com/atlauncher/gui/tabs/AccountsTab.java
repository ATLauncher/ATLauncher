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

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.dialogs.LoginWithMicrosoftDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Authentication;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.SkinUtils;

import org.mini2Dx.gettext.GetText;

public class AccountsTab extends JPanel implements Tab, RelocalizationListener {
    private static final long serialVersionUID = 2493791137600123223L;

    private JLabel userSkin;
    private final JComboBox<ComboItem<AbstractAccount>> accountsComboBox;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JLabel rememberLabel;
    private JCheckBox rememberField;
    private JButton leftButton;
    private JButton rightButton;
    private JButton loginWithMicrosoftButton;
    private JMenuItem refreshAccessTokenMenuItem;
    private final JMenuItem updateSkin;
    private final JPopupMenu contextMenu; // Right click menu

    @SuppressWarnings("unchecked")
    public AccountsTab() {
        setLayout(new BorderLayout());

        RelocalizationManager.addListener(this);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(60, 250, 0, 250));

        JEditorPane infoTextPane = new JEditorPane("text/html", new HTMLBuilder().center().text(GetText.tr(
                "In order to login and use ATLauncher modpacks, you must authenticate with your existing Minecraft/Mojang account. You must own and have paid for the Minecraft Java edition (not the Windows 10 edition) and use the same login here.<br><br>If you don't have an existing account, you can get one <a href=\"https://atl.pw/create-account\">by buying Minecraft here</a>. ATLauncher doesn't work with cracked accounts."))
                .build());
        infoTextPane.setEditable(false);
        infoTextPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OS.openWebBrowser(e.getURL());
            }
        });

        infoPanel.add(infoTextPane);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        Insets TOP_INSETS = new Insets(0, 0, 20, 0);
        gbc.insets = TOP_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;

        accountsComboBox = new JComboBox<>();
        accountsComboBox.setName("accountsTabAccountsComboBox");
        accountsComboBox.addItem(new ComboItem<>(null, GetText.tr("Add An Account")));
        for (AbstractAccount account : AccountManager.getAccounts()) {
            accountsComboBox.addItem(new ComboItem<>(account, account.minecraftUsername));
        }
        accountsComboBox.setSelectedIndex(0);
        accountsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (accountsComboBox.getSelectedIndex() == 0) {
                    usernameField.setText("");
                    passwordField.setText("");
                    rememberField.setSelected(false);
                    leftButton.setText(GetText.tr("Add"));
                    rightButton.setText(GetText.tr("Clear"));
                    userSkin.setIcon(SkinUtils.getDefaultSkin());

                    usernameLabel.setVisible(true);
                    usernameField.setVisible(true);
                    passwordLabel.setVisible(true);
                    passwordField.setVisible(true);
                    rememberLabel.setVisible(true);
                    rememberField.setVisible(true);
                    leftButton.setVisible(true);
                    rightButton.setVisible(true);
                    loginWithMicrosoftButton.setVisible(true);
                    refreshAccessTokenMenuItem.setVisible(false);
                } else {
                    AbstractAccount account = ((ComboItem<AbstractAccount>) accountsComboBox.getSelectedItem())
                            .getValue();

                    usernameLabel.setVisible(account instanceof MojangAccount);
                    usernameField.setVisible(account instanceof MojangAccount);
                    passwordLabel.setVisible(account instanceof MojangAccount);
                    passwordField.setVisible(account instanceof MojangAccount);
                    rememberLabel.setVisible(account instanceof MojangAccount);
                    rememberField.setVisible(account instanceof MojangAccount);
                    leftButton.setVisible(account instanceof MojangAccount);
                    rightButton.setVisible(true);
                    loginWithMicrosoftButton.setVisible(account instanceof MicrosoftAccount);
                    refreshAccessTokenMenuItem.setVisible(account instanceof MicrosoftAccount);

                    if (account instanceof MojangAccount) {
                        MojangAccount mojangAccount = (MojangAccount) account;
                        usernameField.setText(mojangAccount.username);
                        passwordField.setText(mojangAccount.password);
                        rememberField.setSelected(mojangAccount.remember);
                    } else {
                        usernameField.setText("");
                        passwordField.setText("");
                        rememberField.setSelected(false);
                    }

                    leftButton.setText(GetText.tr("Save"));
                    rightButton.setText(GetText.tr("Delete"));
                    userSkin.setIcon(account.getMinecraftSkin());
                }
            }
        });
        topPanel.add(accountsComboBox, gbc);
        bottomPanel.add(accountsComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        usernameLabel = new JLabel(GetText.tr("Username/Email") + ":");
        bottomPanel.add(usernameLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        usernameField = new JTextField(16);
        usernameField.setName("usernameField");
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
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        passwordLabel = new JLabel(GetText.tr("Password") + ":");
        bottomPanel.add(passwordLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        passwordField = new JPasswordField(16);
        passwordField.setName("passwordField");
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
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        rememberLabel = new JLabel(GetText.tr("Remember Password") + ":");
        bottomPanel.add(rememberLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
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
        Insets BOTTOM_INSETS = new Insets(10, 0, 0, 0);
        gbc.insets = BOTTOM_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        leftButton = new JButton(GetText.tr("Add"));
        leftButton.setName("leftButton");
        leftButton.addActionListener(e -> leftButtonActions());
        rightButton = new JButton(GetText.tr("Clear"));
        rightButton.addActionListener(e -> {
            if (accountsComboBox.getSelectedIndex() == 0) {
                usernameField.setText("");
                passwordField.setText("");
                rememberField.setSelected(false);
            } else {
                AbstractAccount account = ((ComboItem<AbstractAccount>) accountsComboBox.getSelectedItem()).getValue();
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Delete"))
                        .setContent(GetText.tr("Are you sure you want to delete this account?"))
                        .setType(DialogManager.WARNING).show();
                if (ret == DialogManager.YES_OPTION) {
                    Analytics.sendEvent("Delete", "Account");
                    AccountManager.removeAccount(account);
                    accountsComboBox.removeAllItems();
                    accountsComboBox.addItem(new ComboItem<>(null, GetText.tr("Add An Account")));
                    for (AbstractAccount accountt : AccountManager.getAccounts()) {
                        accountsComboBox.addItem(new ComboItem<>(accountt, accountt.minecraftUsername));
                    }
                    accountsComboBox.setSelectedIndex(0);
                }
            }
        });
        loginWithMicrosoftButton = new JButton(GetText.tr("Login with Microsoft"));
        loginWithMicrosoftButton.addActionListener(e -> {
            new LoginWithMicrosoftDialog();
            accountsComboBox.removeAllItems();
            accountsComboBox.addItem(new ComboItem<>(null, GetText.tr("Add An Account")));
            for (AbstractAccount accountt : AccountManager.getAccounts()) {
                accountsComboBox.addItem(new ComboItem<>(accountt, accountt.minecraftUsername));
            }
            accountsComboBox.setSelectedItem(AccountManager.getSelectedAccount());
        });
        buttons.add(leftButton);
        buttons.add(rightButton);
        buttons.add(loginWithMicrosoftButton);
        bottomPanel.add(buttons, gbc);

        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(bottomPanel, BorderLayout.CENTER);

        contextMenu = new JPopupMenu();

        updateSkin = new JMenuItem(GetText.tr("Reload Skin"));
        updateSkin.addActionListener(e -> {
            final AbstractAccount account = ((ComboItem<AbstractAccount>) accountsComboBox.getSelectedItem())
                    .getValue();
            Analytics.sendEvent("UpdateSkin", "Account");
            account.updateSkin();
            userSkin.setIcon(account.getMinecraftSkin());
        });
        contextMenu.add(updateSkin);

        JMenuItem updateUsername = new JMenuItem(GetText.tr("Update Username"));
        updateUsername.addActionListener(e -> {
            final AbstractAccount account = ((ComboItem<AbstractAccount>) accountsComboBox.getSelectedItem())
                    .getValue();
            Analytics.sendEvent("UpdateUsername", "Account");
            account.updateUsername();
            AccountManager.saveAccounts();
        });
        contextMenu.add(updateUsername);

        refreshAccessTokenMenuItem = new JMenuItem(GetText.tr("Refresh Access Token"));
        refreshAccessTokenMenuItem.setVisible(false);
        refreshAccessTokenMenuItem.addActionListener(e -> {
            final MicrosoftAccount account = (MicrosoftAccount) ((ComboItem<AbstractAccount>) accountsComboBox
                    .getSelectedItem()).getValue();
            Analytics.sendEvent("RefreshAccessToken", "Account");

            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Refreshing Access Token"), 0,
                    GetText.tr("Refreshing Access Token For {0}", account.minecraftUsername),
                    "Aborting refreshing access token for " + account.minecraftUsername);
            dialog.addThread(new Thread(() -> {
                boolean success = account.refreshAccessToken(true);
                AccountManager.saveAccounts();

                if (success) {
                    DialogManager.okDialog().setTitle(GetText.tr("Access Token Refreshed"))
                            .setContent(GetText.tr("Access token refreshed successfully")).setType(DialogManager.INFO)
                            .show();
                } else {
                    account.mustLogin = true;
                    AccountManager.saveAccounts();

                    DialogManager.okDialog().setTitle(GetText.tr("Failed To Refresh Access Token"))
                            .setContent(GetText.tr("Failed to refresh accessToken. Please login again."))
                            .setType(DialogManager.ERROR).show();

                    new LoginWithMicrosoftDialog(account);
                }

                dialog.close();
            }));
            dialog.start();
        });
        contextMenu.add(refreshAccessTokenMenuItem);

        userSkin = new JLabel(SkinUtils.getDefaultSkin());
        userSkin.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (accountsComboBox.getSelectedIndex() != 0) {
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

    @SuppressWarnings("unchecked")
    private void leftButtonActions() {
        AbstractAccount account;
        String clientToken = UUID.randomUUID().toString().replace("-", "");
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        boolean remember = rememberField.isSelected();
        if (AccountManager.isAccountByName(username) && accountsComboBox.getSelectedIndex() == 0) {
            DialogManager.okDialog().setTitle(GetText.tr("Account Not Added"))
                    .setContent(GetText.tr("This account already exists.")).setType(DialogManager.ERROR).show();
            return;
        }

        LogManager.info("Logging into Minecraft!");
        final ProgressDialog<LoginResponse> dialog = new ProgressDialog<>(GetText.tr("Logging Into Minecraft"), 0,
                GetText.tr("Logging Into Minecraft"), "Aborting login for " + usernameField.getText());
        dialog.setName("loginDialog");
        dialog.addThread(new Thread(() -> {
            LoginResponse resp = Authentication.checkAccount(usernameField.getText(),
                    new String(passwordField.getPassword()), clientToken);
            dialog.setReturnValue(resp);
            dialog.close();
        }));
        dialog.start();
        LoginResponse response = dialog.getReturnValue();
        if (response != null && response.hasAuth() && response.isValidAuth()) {
            if (accountsComboBox.getSelectedIndex() == 0) {
                account = new MojangAccount(username, password, response, remember, clientToken);
                AccountManager.addAccount(account);
            } else {
                account = ((ComboItem<AbstractAccount>) accountsComboBox.getSelectedItem()).getValue();

                if (account instanceof MojangAccount) {
                    MojangAccount mojangAccount = (MojangAccount) account;

                    mojangAccount.username = username;
                    mojangAccount.minecraftUsername = response.getAuth().getSelectedProfile().getName();
                    mojangAccount.uuid = response.getAuth().getSelectedProfile().getId().toString();
                    if (remember) {
                        mojangAccount.setPassword(password);
                    } else {
                        mojangAccount.encryptedPassword = null;
                        mojangAccount.password = null;
                    }
                    mojangAccount.remember = remember;
                    mojangAccount.clientToken = clientToken;
                    mojangAccount.store = response.getAuth().saveForStorage();

                    AccountManager.saveAccounts();
                    com.atlauncher.evnt.manager.AccountManager.post();
                }

                Analytics.sendEvent("Edit", "Account");
                LogManager.info("Edited Account " + account);
                DialogManager.okDialog().setTitle(GetText.tr("Account Edited"))
                        .setContent(GetText.tr("Account edited successfully")).setType(DialogManager.INFO).show();
            }
            accountsComboBox.removeAllItems();
            accountsComboBox.addItem(new ComboItem<>(null, GetText.tr("Add An Account")));
            for (AbstractAccount accountt : AccountManager.getAccounts()) {
                accountsComboBox.addItem(new ComboItem<>(accountt, accountt.minecraftUsername));
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
