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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.microsoft.Entitlements;
import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.OauthDeviceCodeResponse;
import com.atlauncher.data.microsoft.OauthDeviceCodeTokenError;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.data.microsoft.XboxLiveAuthErrorResponse;
import com.atlauncher.data.microsoft.XboxLiveAuthResponse;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.DownloadException;
import com.atlauncher.utils.MicrosoftAuthAPI;
import com.atlauncher.utils.OS;

@SuppressWarnings("serial")
public final class LoginWithMicrosoftDialog extends JDialog {
    public MicrosoftAccount account = null;
    private ScheduledExecutorService codeCheckExecutor = Executors.newScheduledThreadPool(1);

    public LoginWithMicrosoftDialog() {
        this(null);
    }

    public LoginWithMicrosoftDialog(MicrosoftAccount account) {
        super(App.launcher.getParent(), GetText.tr("Login with Microsoft"), ModalityType.DOCUMENT_MODAL);

        this.account = account;
        this.setMinimumSize(new Dimension(400, 350));
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        OauthDeviceCodeResponse deviceCodeResponse = MicrosoftAuthAPI.getDeviceCode();

        this.add(new LoadingPanel(null), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel linkPanel = new JPanel(new FlowLayout());

        JButton copyCodeButton = new JButton(GetText.tr("Copy Code"));
        copyCodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OS.copyToClipboard(deviceCodeResponse.userCode);
            }
        });
        linkPanel.add(copyCodeButton);

        JButton openLinkButton = new JButton(GetText.tr("Open Link"));
        openLinkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OS.openWebBrowser("https://www.microsoft.com/link");
            }
        });
        linkPanel.add(openLinkButton);

        JEditorPane infoPane = new JEditorPane("text/html", new HTMLBuilder().center().text(GetText.tr(
                "To complete login, open <a href=\"https://www.microsoft.com/link\">https://www.microsoft.com/link</a> in a web browser and enter the code <b>{0}</b>.",
                deviceCodeResponse.userCode)).build());
        infoPane.setEditable(false);
        infoPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OS.openWebBrowser(e.getURL());
            }
        });
        infoPane.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 32));

        bottomPanel.add(infoPane, BorderLayout.CENTER);
        bottomPanel.add(linkPanel, BorderLayout.SOUTH);

        this.add(bottomPanel, BorderLayout.SOUTH);

        setVisible(false);
        dispose();

        pollForToken(deviceCodeResponse);

        setLocationRelativeTo(App.launcher.getParent());
        setVisible(true);
    }

    private void close() {
        if (!codeCheckExecutor.isShutdown()) {
            codeCheckExecutor.shutdown();
        }

        setVisible(false);
        dispose();
    }

    private void pollForToken(OauthDeviceCodeResponse deviceCodeResponse) {
        Runnable checkDeviceCodeRunnable = new Runnable() {
            public void run() {
                try {
                    OauthTokenResponse oauthTokenResponse = MicrosoftAuthAPI
                            .checkDeviceCodeForToken(deviceCodeResponse.deviceCode);

                    System.out.println(Gsons.DEFAULT.toJson(oauthTokenResponse));

                    acquireXBLToken(oauthTokenResponse);

                    close();
                } catch (DownloadException e) {
                    if (e.response != null) {
                        OauthDeviceCodeTokenError deviceCodeTokenError = Gsons.DEFAULT.fromJson(e.response,
                                OauthDeviceCodeTokenError.class);

                        if (deviceCodeTokenError.error.equalsIgnoreCase("authorization_declined")) {
                            LogManager.error("Authorization was declined");
                            close();
                        } else if (deviceCodeTokenError.error.equalsIgnoreCase("expired_token")) {
                            LogManager.error("Token expired. Please try again");
                            close();
                        } else if (!deviceCodeTokenError.error.equalsIgnoreCase("authorization_pending")) {
                            LogManager.error("An unknown error occured. Please try again");
                            close();
                        }
                    } else {
                        LogManager.error("An unknown error occured. Please try again");
                        close();
                    }
                } catch (Exception e) {
                    LogManager.logStackTrace("Error when checking for device code for token", e);
                    close();
                }
            }
        };

        try {
            codeCheckExecutor.scheduleAtFixedRate(checkDeviceCodeRunnable, deviceCodeResponse.interval,
                    deviceCodeResponse.interval, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    private void addAccount(OauthTokenResponse oauthTokenResponse, XboxLiveAuthResponse xstsAuthResponse,
            LoginResponse loginResponse, Profile profile) throws Exception {
        if (account != null || AccountManager.isAccountByName(loginResponse.username)) {
            MicrosoftAccount account = (MicrosoftAccount) AccountManager.getAccountByName(loginResponse.username);

            if (account == null) {
                return;
            }

            // if forced to relogin, then make sure they logged into correct account
            if (account != null && this.account != null && !account.username.equals(this.account.username)) {
                DialogManager.okDialog().setTitle(GetText.tr("Incorrect account"))
                        .setContent(
                                GetText.tr("Logged into incorrect account. Please login again on the Accounts tab."))
                        .setType(DialogManager.ERROR).show();
                return;
            }

            account.update(oauthTokenResponse, xstsAuthResponse, loginResponse, profile);
            AccountManager.saveAccounts();
        } else {
            MicrosoftAccount account = new MicrosoftAccount(oauthTokenResponse, xstsAuthResponse, loginResponse,
                    profile);

            AccountManager.addAccount(account);
            this.account = account;
        }
    }

    private void acquireXBLToken(OauthTokenResponse oauthTokenResponse) throws Exception {
        XboxLiveAuthResponse xblAuthResponse = MicrosoftAuthAPI.getXBLToken(oauthTokenResponse.accessToken);

        acquireXsts(oauthTokenResponse, xblAuthResponse.token);
    }

    private void acquireXsts(OauthTokenResponse oauthTokenResponse, String xblToken) throws Exception {
        XboxLiveAuthResponse xstsAuthResponse = null;

        try {
            xstsAuthResponse = MicrosoftAuthAPI.getXstsToken(xblToken);
            System.out.println(Gsons.DEFAULT.toJson(xstsAuthResponse));
        } catch (DownloadException e) {
            if (e.response != null) {
                LogManager.debug(Gsons.DEFAULT.toJson(e.response));
                XboxLiveAuthErrorResponse xboxLiveAuthErrorResponse = Gsons.DEFAULT.fromJson(e.response,
                        XboxLiveAuthErrorResponse.class);

                String error = xboxLiveAuthErrorResponse.getErrorMessageForCode();

                if (error != null) {
                    LogManager.warn(error);
                    DialogManager dialog = DialogManager.okDialog().setTitle(GetText.tr("Error logging into Xbox Live"))
                            .setContent(new HTMLBuilder().center().text(error).build()).setType(DialogManager.ERROR);

                    String link = xboxLiveAuthErrorResponse.getBrowserLinkForCode();

                    throw new DialogException(dialog, e.getMessage(), link);
                }

                throw e;
            }
        }

        if (xstsAuthResponse != null) {
            acquireMinecraftToken(oauthTokenResponse, xstsAuthResponse);
        }
    }

    private void acquireMinecraftToken(OauthTokenResponse oauthTokenResponse, XboxLiveAuthResponse xstsAuthResponse)
            throws Exception {
        String xblUhs = xstsAuthResponse.displayClaims.xui.get(0).uhs;
        String xblXsts = xstsAuthResponse.token;

        LoginResponse loginResponse = MicrosoftAuthAPI.loginToMinecraft("XBL3.0 x=" + xblUhs + ";" + xblXsts);

        if (loginResponse == null) {
            throw new Exception("Failed to login to Minecraft");
        }

        Entitlements entitlements = MicrosoftAuthAPI.getEntitlements(loginResponse.accessToken);

        if (!(entitlements.items.stream().anyMatch(i -> i.name.equalsIgnoreCase("product_minecraft"))
                && entitlements.items.stream().anyMatch(i -> i.name.equalsIgnoreCase("game_minecraft")))) {
            DialogManager dialog = DialogManager.okDialog().setTitle(GetText.tr("Minecraft Has Not Been Purchased"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "This account doesn't have a valid purchase of Minecraft.<br/><br/>Please make sure you've bought the Java edition of Minecraft and then try again.<br/><br/>If you have Xbox Game Pass, you need to login through the Minecraft launcher<br/>at least once and launch the game once in order to create your username."))
                            .build())
                    .setType(DialogManager.ERROR);

            throw new DialogException(dialog, "Account does not own Minecraft");
        }

        Profile profile = null;
        try {
            profile = MicrosoftAuthAPI.getMcProfile(loginResponse.accessToken);
        } catch (DownloadException e) {
            LogManager.error("Minecraft profile not found");

            new CreateMinecraftProfileDialog(loginResponse.accessToken);

            try {
                profile = MicrosoftAuthAPI.getMcProfile(loginResponse.accessToken);
            } catch (IOException e1) {
                LogManager.logStackTrace("Failed to get Minecraft profile", e1);
                throw new Exception("Failed to get Minecraft profile");
            }
        }

        if (profile == null) {
            throw new Exception("Failed to get Minecraft profile");
        }

        // add the account
        addAccount(oauthTokenResponse, xstsAuthResponse, loginResponse, profile);
    }
}
