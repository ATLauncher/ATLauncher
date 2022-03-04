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
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;
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

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.listener.ThemeListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.evnt.manager.ThemeManager;
import com.atlauncher.gui.dialogs.LoginWithMicrosoftDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.SkinUtils;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

public class AccountsTab extends JPanel implements Tab, RelocalizationListener, ThemeListener {
    private static final long serialVersionUID = 2493791137600123223L;

    private JLabel userSkin;
    private final JComboBox<ComboItem<AbstractAccount>> accountsComboBox;
    private JButton deleteButton;
    private JButton loginWithMicrosoftButton;
    private JMenuItem refreshAccessTokenMenuItem;
    private JEditorPane mojangAccountWarning;
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
                "In order to login and use ATLauncher modpacks, you must authenticate with your existing Minecraft/Mojang account. You must own and have paid for the Minecraft Java edition (not the Windows 10 edition) and then login to the same Microsoft account here.<br><br>If you don't have an existing account, you can get one <a href=\"https://atl.pw/create-account\">by buying Minecraft here</a>. ATLauncher doesn't work with cracked accounts."))
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
                    userSkin.setIcon(SkinUtils.getDefaultSkin());
                    deleteButton.setVisible(false);
                    refreshAccessTokenMenuItem.setVisible(false);
                    mojangAccountWarning.setVisible(false);
                } else {
                    AbstractAccount account = ((ComboItem<AbstractAccount>) accountsComboBox.getSelectedItem())
                            .getValue();

                    deleteButton.setVisible(true);
                    mojangAccountWarning.setVisible(account instanceof MojangAccount);
                    refreshAccessTokenMenuItem.setVisible(account instanceof MicrosoftAccount);
                    userSkin.setIcon(account.getMinecraftSkin());
                }
            }
        });
        topPanel.add(accountsComboBox, gbc);
        bottomPanel.add(accountsComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        mojangAccountWarning = new JEditorPane("text/html", new HTMLBuilder().center().text(GetText.tr(
                "This account is a Mojang account and can no longer be used.<br/><br/>Please migrate your account at <a href=\"https://minecraft.net/move\">minecraft.net/move</a> and then login to your Microsoft account below."))
                .build());
        mojangAccountWarning.setEditable(false);
        mojangAccountWarning.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OS.openWebBrowser(e.getURL());
            }
        });
        mojangAccountWarning.setVisible(false);
        bottomPanel.add(mojangAccountWarning, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        Insets BOTTOM_INSETS = new Insets(10, 0, 0, 0);
        gbc.insets = BOTTOM_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        loginWithMicrosoftButton = new JButton(
                Utils.getIconImage("/assets/image/ms-login-" + (App.THEME.isDark() ? "dark" : "light") + ".png"));
        loginWithMicrosoftButton.addActionListener(e -> {
            int numberOfAccountsBefore = AccountManager.getAccounts().size();
            new LoginWithMicrosoftDialog();

            if (numberOfAccountsBefore != AccountManager.getAccounts().size()) {
                accountsComboBox.removeAllItems();
                accountsComboBox.addItem(new ComboItem<>(null, GetText.tr("Add An Account")));
                for (AbstractAccount accountt : AccountManager.getAccounts()) {
                    accountsComboBox.addItem(new ComboItem<>(accountt, accountt.minecraftUsername));
                }
                accountsComboBox.setSelectedItem(AccountManager.getSelectedAccount());
            }
        });
        bottomPanel.add(loginWithMicrosoftButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = BOTTOM_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        deleteButton = new JButton(GetText.tr("Delete"));
        deleteButton.setVisible(false);
        deleteButton.addActionListener(e -> {
            if (accountsComboBox.getSelectedIndex() != 0) {
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
        bottomPanel.add(deleteButton, gbc);

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

        ThemeManager.addListener(this);
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
        deleteButton.setText(GetText.tr("Delete"));
        updateSkin.setText(GetText.tr("Reload Skin"));
    }

    @Override
    public void onThemeChange() {
        loginWithMicrosoftButton.setIcon(
                Utils.getIconImage("/assets/image/ms-login-" + (App.THEME.isDark() ? "dark" : "light") + ".png"));
    }
}
