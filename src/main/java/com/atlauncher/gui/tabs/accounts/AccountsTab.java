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
package com.atlauncher.gui.tabs.accounts;

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

import org.mini2Dx.gettext.GetText;

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
import com.atlauncher.gui.tabs.Tab;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.SkinUtils;
import com.atlauncher.viewmodel.base.IAccountsViewModel;
import com.atlauncher.viewmodel.base.IAccountsViewModel.LoginPostResult;
import com.atlauncher.viewmodel.base.IAccountsViewModel.LoginPreCheckResult;
import com.atlauncher.viewmodel.impl.AccountsViewModel;

public class AccountsTab extends JPanel implements Tab, RelocalizationListener {
    private static final long serialVersionUID = 2493791137600123223L;

    private final IAccountsViewModel viewModel;

    private JLabel userSkin;
    private final JComboBox<ComboItem<String>> accountsComboBox;
    private final JLabel usernameLabel;
    private final JTextField usernameField;
    private final JLabel passwordLabel;
    private final JPasswordField passwordField;
    private final JLabel rememberLabel;
    private final JCheckBox rememberField;
    private final JButton leftButton;
    private final JButton rightButton;
    private final JButton loginWithMicrosoftButton;
    private final JMenuItem refreshAccessTokenMenuItem;
    private final JMenuItem updateSkin;
    private final JMenuItem changeSkin;
    private final JPopupMenu contextMenu; // Right click menu

    private final JEditorPane infoTextPane;

    private final JMenuItem updateUsername;

    @SuppressWarnings("unchecked")
    public AccountsTab() {
        viewModel = new AccountsViewModel();
        setLayout(new BorderLayout());

        RelocalizationManager.addListener(this);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(60, 250, 0, 250));

        infoTextPane = new JEditorPane("text/html", null);
        infoTextPane.setEditable(false);
        infoTextPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OS.openWebBrowser(e.getURL());
            }
        });

        infoPanel.add(infoTextPane);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

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
        accountsComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                viewModel.setSelectedAccount(accountsComboBox.getSelectedIndex());
            }
        });
        bottomPanel.add(accountsComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        usernameLabel = new JLabel();
        bottomPanel.add(usernameLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        usernameField = new JTextField(16);
        usernameField.setName("usernameField");
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                viewModel.setLoginUsername(usernameField.getText());
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!viewModel.isLoginPasswordSet())
                        passwordField.grabFocus();
                    else
                        login();
                }
            }
        });
        bottomPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        passwordLabel = new JLabel();
        bottomPanel.add(passwordLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        passwordField = new JPasswordField(16);
        passwordField.setName("passwordField");
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                viewModel.setLoginPassword(
                    new String(passwordField.getPassword()));
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (!viewModel.isLoginUsernameSet())
                        usernameField.grabFocus();
                    else
                        login();
                }
            }
        });
        bottomPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        rememberLabel = new JLabel();
        bottomPanel.add(rememberLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        rememberField = new JCheckBox();
        bottomPanel.add(rememberField, gbc);
        rememberField.addActionListener(e -> {
            viewModel.setRememberLogin(rememberField.isSelected());
            if (rememberField.isSelected()) {
                int ret = DialogManager
                    .optionDialog()
                    .setTitle(GetText.tr("Security Warning"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "Make sure you only do this on a " +
                                "computer you trust.<br/>If you do this " +
                                "on a shared computer, your password may be " +
                                "stolen.<br/>Do you still want to save " +
                                "your password?"))
                        .build())
                    .setType(DialogManager.ERROR)
                    .addOption(GetText.tr("Yes"), true)
                    .addOption(GetText.tr("No"))
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
        leftButton = new JButton();
        leftButton.setName("leftButton");
        leftButton.addActionListener(e -> login());
        rightButton = new JButton(getClearText());
        rightButton.addActionListener(e -> {
            if (accountsComboBox.getSelectedIndex() == 0) {
                clearLogin();
            } else {
                int ret = DialogManager
                    .yesNoDialog()
                    .setTitle(GetText.tr("Delete"))
                    .setContent(GetText.tr("Are you sure you want " +
                        "to delete this account?"))
                    .setType(DialogManager.WARNING).show();
                if (ret == DialogManager.YES_OPTION) {
                    viewModel.deleteAccount();
                }
            }
        });
        loginWithMicrosoftButton = new JButton();
        loginWithMicrosoftButton.addActionListener(e -> {
            // TODO This should be handled by some reaction via listener
            int numberOfAccountsBefore = viewModel.accountCount();
            new LoginWithMicrosoftDialog();

            if (numberOfAccountsBefore != viewModel.accountCount()) {
                viewModel.pushNewAccounts();
                accountsComboBox.setSelectedItem(AccountManager.getSelectedAccount());
            }
        });
        buttons.add(leftButton);
        buttons.add(rightButton);
        buttons.add(loginWithMicrosoftButton);
        bottomPanel.add(buttons, gbc);

        rightPanel.add(bottomPanel, BorderLayout.CENTER);

        contextMenu = new JPopupMenu();

        changeSkin = new JMenuItem();
        changeSkin.addActionListener(e -> {
            viewModel.changeSkin();

            // TODO Have this done via listener
            // To describe, userSkin icon should be reactive, not active.
            AbstractAccount account = viewModel.getSelectedAccount();
            userSkin.setIcon(account.getMinecraftSkin());
        });
        contextMenu.add(changeSkin);

        updateSkin = new JMenuItem();
        updateSkin.addActionListener(e -> {
            viewModel.updateSkin();

            // TODO Have this done via listener
            // To describe, userSkin icon should be reactive, not active.
            AbstractAccount account = viewModel.getSelectedAccount();
            userSkin.setIcon(account.getMinecraftSkin());
        });
        contextMenu.add(updateSkin);

        updateUsername = new JMenuItem();
        updateUsername.addActionListener(e -> viewModel.updateUsername());
        contextMenu.add(updateUsername);

        refreshAccessTokenMenuItem = new JMenuItem();
        refreshAccessTokenMenuItem.setVisible(false);
        refreshAccessTokenMenuItem.addActionListener(e -> {
            refreshAccessToken();
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
        userSkin.setBorder(
            BorderFactory.createEmptyBorder(0, 60, 0, 0));
        add(infoPanel, BorderLayout.NORTH);
        add(userSkin, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        observe();

        accountsComboBox.setSelectedIndex(0);
        onRelocalization();
    }

    /**
     * Refresh the access token, and react to result
     */
    private void refreshAccessToken() {
        MicrosoftAccount account = viewModel.getSelectedAccountAs();
        if (account == null)
            return;

        final ProgressDialog<Boolean> dialog = new ProgressDialog<>(
            GetText.tr("Refreshing Access Token For {0}", account.minecraftUsername),
            0,
            GetText.tr("Refreshing Access Token For {0}", account.minecraftUsername),
            "Aborting refreshing access token for " + account.minecraftUsername);

        dialog.addThread(new Thread(() -> {
            boolean success = viewModel.refreshAccessToken();
            dialog.setReturnValue(success);
            dialog.close();
        }));
        dialog.start();

        boolean success = dialog.getReturnValue();

        if (success) {
            DialogManager
                .okDialog()
                .setTitle(GetText.tr("Access Token Refreshed"))
                .setContent(
                    GetText.tr("Access token refreshed successfully"))
                .setType(DialogManager.INFO)
                .show();
        } else {
            DialogManager
                .okDialog()
                .setTitle(GetText.tr("Failed To Refresh Access Token"))
                .setContent(GetText.tr("Failed to refresh accessToken. Please login again."))
                .setType(DialogManager.ERROR)
                .show();

            new LoginWithMicrosoftDialog(account);
        }
    }

    /**
     * Start observing state changes from view model
     */
    private void observe() {
        viewModel.onAccountSelected(account -> {
            if (account == null) {
                usernameField.setText("");
                passwordField.setText("");
                rememberField.setSelected(false);
                leftButton.setText(getAddText());
                rightButton.setText(getClearText());
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

                usernameLabel.setVisible(account instanceof MojangAccount);
                usernameField.setVisible(account instanceof MojangAccount);
                passwordLabel.setVisible(account instanceof MojangAccount);
                passwordField.setVisible(account instanceof MojangAccount);
                rememberLabel.setVisible(account instanceof MojangAccount);
                rememberField.setVisible(account instanceof MojangAccount);
                leftButton.setVisible(account instanceof MojangAccount);
                rightButton.setVisible(true);
                loginWithMicrosoftButton.setVisible(
                    account instanceof MicrosoftAccount);
                refreshAccessTokenMenuItem.setVisible(
                    account instanceof MicrosoftAccount);

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

                leftButton.setText(getSaveText());
                rightButton.setText(getDeleteText());
                userSkin.setIcon(account.getMinecraftSkin());
            }
        });
        viewModel.onAccountsNamesChanged(accounts -> {
            accountsComboBox.removeAllItems();
            accountsComboBox.addItem(
                new ComboItem<>(
                    null,
                    GetText.tr("Add An Account")));
            for (String account : accounts) {
                accountsComboBox.addItem(new ComboItem<>(null, account));
            }
        });

    }

    /**
     * User requests to clear out the login attempt
     */
    private void clearLogin() {
        usernameField.setText("");
        viewModel.setLoginUsername("");

        passwordField.setText("");
        viewModel.setLoginPassword("");

        rememberField.setSelected(false);
        viewModel.setRememberLogin(false);
    }

    /**
     * Run login steps, and react accordingly
     */
    @SuppressWarnings("unchecked")
    private void login() {
        // Pre check
        LoginPreCheckResult preCheckResult = viewModel.loginPreCheck();
        if (preCheckResult instanceof LoginPreCheckResult.Exists) {
            DialogManager
                .okDialog()
                .setTitle(GetText.tr("Account Not Added"))
                .setContent(GetText.tr("This account already exists."))
                .setType(DialogManager.ERROR)
                .show();
            return;
        }

        LogManager.info("Logging into Minecraft!");
        final ProgressDialog<LoginResponse> dialog = new ProgressDialog<>(
            GetText.tr("Logging Into Minecraft"),
            0,
            GetText.tr("Logging Into Minecraft"),
            "Aborting login for " + viewModel.getLoginUsername());
        dialog.addThread(new Thread(() -> {
            viewModel.login();
            dialog.close();
        }));
        dialog.start();

        LoginPostResult postResult = viewModel.loginPost();

        if (postResult instanceof LoginPostResult.Error) {
            String error = ((LoginPostResult.Error) postResult).errorContent;
            LogManager.error("error response: " + error);
            DialogManager
                .okDialog()
                .setTitle(GetText.tr("Account Not Added"))
                .setContent(
                    new HTMLBuilder()
                        .center()
                        // #. {0} is the error message from Mojang as to why we couldn't login
                        .text(GetText.tr(
                            "Account not added as login " +
                                "details were incorrect.<br/><br/>{0}",
                            error))
                        .build())
                .setType(DialogManager.INFO)
                .show();
        } else {
            if (postResult instanceof LoginPostResult.Edited) {
                DialogManager
                    .okDialog()
                    .setTitle(GetText.tr("Account Edited"))
                    .setContent(
                        GetText.tr("Account edited successfully"))
                    .setType(DialogManager.INFO)
                    .show();
            }

            viewModel.pushNewAccounts();
            accountsComboBox.setSelectedIndex(viewModel.getSelectedIndex());
        }
    }

    private static String getInfo() {
        return new HTMLBuilder()
            .center()
            .text(GetText.tr(
                "In order to login and use ATLauncher modpacks, " +
                    "you must authenticate with your existing " +
                    "Minecraft/Mojang account. You must own and have paid " +
                    "for the Minecraft Java edition " +
                    "(not the Windows 10 edition) and use the same " +
                    "login here.<br><br>If you don't have an existing " +
                    "account, you can get one " +
                    "<a href=\"https://atl.pw/create-account\">by buying " +
                    "Minecraft here</a>. ATLauncher doesn't work with cracked" +
                    " accounts."
            ))
            .build();
    }

    private static String getClearText() {
        return GetText.tr("Clear");
    }

    private static String getAddText() {
        return GetText.tr("Add");
    }

    private static String getSaveText() {
        return GetText.tr("Save");
    }

    private static String getDeleteText() {
        return GetText.tr("Delete");
    }

    @Override
    public String getTitle() {
        return GetText.tr("Accounts");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Accounts";
    }

    @Override
    public void onRelocalization() {
        if (accountsComboBox.getSelectedIndex() == 0) {
            leftButton.setText(getAddText());
            rightButton.setText(getClearText());
        } else {
            leftButton.setText(getSaveText());
            rightButton.setText(getDeleteText());
        }

        usernameLabel.setText(GetText.tr("Username/Email") + ":");
        passwordLabel.setText(GetText.tr("Password") + ":");
        rememberLabel.setText(GetText.tr("Remember Password") + ":");
        updateSkin.setText(GetText.tr("Reload Skin"));
        changeSkin.setText(GetText.tr("Change Skin"));
        updateUsername.setText(GetText.tr("Update Username"));
        refreshAccessTokenMenuItem.setText(GetText.tr("Refresh Access Token"));
        infoTextPane.setText(getInfo());
        loginWithMicrosoftButton.setText(GetText.tr("Login with Microsoft"));

        viewModel.pushNewAccounts(); // I do not like this @_@
    }
}
