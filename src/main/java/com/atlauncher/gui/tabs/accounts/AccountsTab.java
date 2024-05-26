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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.HyperlinkEvent;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.microsoft.OauthDeviceCodeResponse;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.gui.dialogs.LoginWithMicrosoftDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.gui.tabs.Tab;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.MicrosoftAuthAPI;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.SkinUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.viewmodel.base.IAccountsViewModel;
import com.atlauncher.viewmodel.impl.AccountsViewModel;

public class AccountsTab extends HierarchyPanel implements Tab, RelocalizationListener {
    private static final long serialVersionUID = 2493791137600123223L;

    private IAccountsViewModel viewModel;

    private JLabel userSkin;
    private JComboBox<ComboItem<String>> accountsComboBox;
    private JButton deleteButton;
    private JButton loginWithMicrosoftButton;
    private JMenuItem refreshAccessTokenMenuItem;
    private JMenuItem updateSkin;
    private JMenuItem changeSkin;
    private JPopupMenu contextMenu; // Right click menu

    @SuppressWarnings("unchecked")
    public AccountsTab() {
        super(new BorderLayout());
    }

    @Override
    protected void onShow() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(60, 250, 0, 250));

        JEditorPane infoTextPane = new JEditorPane("text/html", new HTMLBuilder().center().text(GetText.tr(
                "In order to login and use ATLauncher modpacks, " +
                    "you must authenticate with your existing " +
                    "Minecraft/Mojang account. You must own and have paid " +
                    "for the Minecraft Java edition " +
                    "(not the Windows 10 edition) and use the same " +
                    "login here.<br><br>If you don't have an existing " +
                    "account, you can get one " +
                    "<a href=\"https://atl.pw/create-account\">by buying " +
                    "Minecraft here</a>. ATLauncher doesn't work with cracked" +
                    " accounts."))
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
        gbc.gridwidth = 2;
        Insets BOTTOM_INSETS = new Insets(10, 0, 0, 0);
        gbc.insets = BOTTOM_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        deleteButton = new JButton(GetText.tr("Delete"));
        deleteButton.setVisible(false);
        deleteButton.addActionListener(e -> {
            int ret = DialogManager
                .yesNoDialog()
                .setTitle(GetText.tr("Delete"))
                .setContent(GetText.tr("Are you sure you want " +
                    "to delete this account?"))
                .setType(DialogManager.WARNING).show();
            if (ret == DialogManager.YES_OPTION) {
                viewModel.deleteAccount();
            }
        });
        loginWithMicrosoftButton = new JButton();
        loginWithMicrosoftButton.setIcon(Utils.getIconImage("/assets/image/sign-in-with-microsoft.png"));
        loginWithMicrosoftButton.setToolTipText(GetText.tr("Login with Microsoft"));
        loginWithMicrosoftButton.addActionListener(e -> {
            // TODO This should be handled by some reaction via listener
            int numberOfAccountsBefore = viewModel.accountCount();

            ProgressDialog<OauthDeviceCodeResponse> codeDialog = new ProgressDialog<OauthDeviceCodeResponse>(
                    GetText.tr("Getting Login Code From Microsoft"), 0,
                    GetText.tr("Getting Login Code From Microsoft"),
                    "Aborting getting login code from Microsoft");
            codeDialog.addThread(new Thread(() -> {
                codeDialog.setReturnValue(MicrosoftAuthAPI.getDeviceCode());
                codeDialog.close();
            }));
            codeDialog.start();
            LoginWithMicrosoftDialog dialog = new LoginWithMicrosoftDialog(codeDialog.getReturnValue());

            if (numberOfAccountsBefore != viewModel.accountCount()) {
                // account was added, so get the skin
                if (dialog.account != null) {
                    dialog.account.updateSkin();
                }

                viewModel.pushNewAccounts();
                accountsComboBox.setSelectedItem(AccountManager.getSelectedAccount());
            }
        });
        buttons.add(deleteButton);
        buttons.add(loginWithMicrosoftButton);
        bottomPanel.add(buttons, gbc);

        rightPanel.add(bottomPanel, BorderLayout.CENTER);

        contextMenu = new JPopupMenu();

        changeSkin = new JMenuItem(GetText.tr("Change Skin"));
        changeSkin.addActionListener(e -> {
            viewModel.changeSkin();

            // TODO Have this done via listener
            // To describe, userSkin icon should be reactive, not active.
            AbstractAccount account = viewModel.getSelectedAccount();
            userSkin.setIcon(account.getMinecraftSkin());
        });
        contextMenu.add(changeSkin);

        updateSkin = new JMenuItem(GetText.tr("Reload Skin"));
        updateSkin.addActionListener(e -> {
            viewModel.updateSkin();

            // TODO Have this done via listener
            // To describe, userSkin icon should be reactive, not active.
            AbstractAccount account = viewModel.getSelectedAccount();
            userSkin.setIcon(account.getMinecraftSkin());
        });
        contextMenu.add(updateSkin);

        JMenuItem updateUsername = new JMenuItem(GetText.tr("Update Username"));
        updateUsername.addActionListener(e -> viewModel.updateUsername());
        contextMenu.add(updateUsername);

        refreshAccessTokenMenuItem = new JMenuItem(GetText.tr("Refresh Access Token"));
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
    }

    /**
     * Refresh the access token, and react to result
     */
    private void refreshAccessToken() {
        MicrosoftAccount account = viewModel.getSelectedAccount();
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

            ProgressDialog<OauthDeviceCodeResponse> codeDialog = new ProgressDialog<OauthDeviceCodeResponse>(
                GetText.tr("Getting Login Code From Microsoft"), 0,
                GetText.tr("Getting Login Code From Microsoft"),
                "Aborting getting login code from Microsoft");
            codeDialog.addThread(new Thread(() -> {
                codeDialog.setReturnValue(MicrosoftAuthAPI.getDeviceCode());
                codeDialog.close();
            }));
            codeDialog.start();

            new LoginWithMicrosoftDialog(account, codeDialog.getReturnValue());
        }
    }

    /**
     * Start observing state changes from view model
     */
    private void observe() {
        viewModel.onAccountSelected(account -> {
            if (account == null) {
                deleteButton.setVisible(false);
                userSkin.setIcon(SkinUtils.getDefaultSkin());
                loginWithMicrosoftButton.setVisible(true);
                refreshAccessTokenMenuItem.setVisible(false);
            } else {
                deleteButton.setVisible(true);
                loginWithMicrosoftButton.setVisible(
                    account instanceof MicrosoftAccount);
                refreshAccessTokenMenuItem.setVisible(
                    account instanceof MicrosoftAccount);

                deleteButton.setText(GetText.tr("Delete"));
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
            deleteButton.setText(GetText.tr("Clear"));
        } else {
            deleteButton.setText(GetText.tr("Delete"));
        }

        updateSkin.setText(GetText.tr("Reload Skin"));
    }

    @Override
    protected void createViewModel() {
        viewModel = new AccountsViewModel();
    }

    @Override
    protected void onDestroy() {
        removeAll();
        userSkin = null;
        accountsComboBox = null;
        deleteButton = null;
        loginWithMicrosoftButton = null;
        refreshAccessTokenMenuItem = null;
        updateSkin = null;
        changeSkin = null;
        contextMenu = null;
    }
}
