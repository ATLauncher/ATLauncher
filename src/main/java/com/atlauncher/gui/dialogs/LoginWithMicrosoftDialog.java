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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
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
import com.atlauncher.utils.Utils;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.VirtualHost;

@SuppressWarnings("serial")
public final class LoginWithMicrosoftDialog extends JDialog {
    private static final HTTPServer server = new HTTPServer(Constants.MICROSOFT_LOGIN_REDIRECT_PORT);
    private static final VirtualHost host = server.getVirtualHost(null);
    public MicrosoftAccount account = null;
    public OauthDeviceCodeResponse deviceCodeResponse = null;
    private ScheduledExecutorService codeCheckExecutor = Executors.newScheduledThreadPool(1);

    public LoginWithMicrosoftDialog() {
        this(null);
    }

    public LoginWithMicrosoftDialog(MicrosoftAccount account) {
        super(App.launcher.getParent(), GetText.tr("Login with Microsoft"), ModalityType.DOCUMENT_MODAL);

        this.account = account;
        this.setMinimumSize(new Dimension(500, 500));
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Browser login at the top
        JPanel topPanel = new JPanel(new FlowLayout());
        JButton browserLoginButton = new JButton(GetText.tr("Login with Browser"));
        browserLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    startServer();
                    OS.openWebBrowser(Constants.MICROSOFT_LOGIN_URL);
                } catch (IOException ex) {
                    LogManager.logStackTrace("Failed to start local server for browser login", ex);
                }
            }
        });
        topPanel.add(browserLoginButton);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel dividerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel linePanel = new JPanel(new BorderLayout(0, 0));
        JLabel orLabel = new JLabel(GetText.tr("OR"));
        orLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JSeparator leftLine = new JSeparator();
        JSeparator rightLine = new JSeparator();
        leftLine.setPreferredSize(new Dimension(200, 1));
        rightLine.setPreferredSize(new Dimension(200, 1));

        Box leftBox = Box.createVerticalBox();
        leftBox.add(Box.createVerticalGlue());
        leftBox.add(leftLine);
        leftBox.add(Box.createVerticalGlue());

        Box rightBox = Box.createVerticalBox();
        rightBox.add(Box.createVerticalGlue());
        rightBox.add(rightLine);
        rightBox.add(Box.createVerticalGlue());

        linePanel.add(leftBox, BorderLayout.WEST);
        linePanel.add(orLabel, BorderLayout.CENTER);
        linePanel.add(rightBox, BorderLayout.EAST);
        dividerPanel.add(linePanel);
        dividerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        mainPanel.add(dividerPanel, BorderLayout.CENTER);

        // Bottom panel with QR code and other options
        JPanel bottomContentPanel = new JPanel(new BorderLayout());
        bottomContentPanel.add(new JLabel(Utils.getIconImage("/assets/image/microsoft-link-qrcode.png")), BorderLayout.CENTER);

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

        JButton openLinkButton = new JButton(GetText.tr("Open Link & Copy Code"));
        openLinkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OS.copyToClipboard(deviceCodeResponse.userCode);
                OS.openWebBrowser("https://www.microsoft.com/link");
            }
        });
        linkPanel.add(openLinkButton);

        JEditorPane infoPane = new JEditorPane("text/html", "");
        infoPane.setEditable(false);
        infoPane.setHighlighter(null);
        infoPane.setFocusable(false);
        infoPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OS.openWebBrowser(e.getURL());
            }
        });
        infoPane.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        bottomPanel.add(infoPane, BorderLayout.CENTER);
        bottomPanel.add(linkPanel, BorderLayout.SOUTH);
        bottomContentPanel.add(bottomPanel, BorderLayout.SOUTH);

        LoadingPanel loadingPanel = new LoadingPanel(GetText.tr("Fetching Login Code..."));
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 100, 0));
        mainPanel.add(loadingPanel, BorderLayout.SOUTH);

        new Thread(() -> {
            deviceCodeResponse = MicrosoftAuthAPI.getDeviceCode();
            SwingUtilities.invokeLater(() -> {
                infoPane.setText(new HTMLBuilder().center().text(GetText.tr(
                    "Scan the above QR code or visit <a href=\"https://www.microsoft.com/link\">https://www.microsoft.com/link</a> in a web browser and enter the code <b>{0}</b>.",
                    deviceCodeResponse.userCode)).build());
                mainPanel.remove(loadingPanel);
                mainPanel.add(bottomContentPanel, BorderLayout.SOUTH);
                mainPanel.revalidate();
                mainPanel.repaint();
            });
            pollForToken(deviceCodeResponse);
        }).start();

        this.add(mainPanel);

        setVisible(false);
        dispose();

        setLocationRelativeTo(App.launcher.getParent());
    }

    private void close() {
        if (!codeCheckExecutor.isShutdown()) {
            codeCheckExecutor.shutdown();
        }

        server.stop();

        setVisible(false);
        dispose();
    }

    private void pollForToken(OauthDeviceCodeResponse deviceCodeResponse) {
        Runnable checkDeviceCodeRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    OauthTokenResponse oauthTokenResponse = MicrosoftAuthAPI
                        .checkDeviceCodeForToken(deviceCodeResponse.deviceCode);

                    if (oauthTokenResponse != null) {
                        acquireXBLToken(oauthTokenResponse);

                        close();
                    }
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
                            LogManager.error("An unknown error occurred. Please try again");
                            close();
                        }
                    } else {
                        LogManager.error("An unknown error occurred. Please try again");
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

    private void startServer() throws IOException {
        host.addContext("/", (req, res) -> {
            if (req.getParams().containsKey("error")) {
                res.getHeaders().add("Content-Type", "text/plain");
                res.send(500, GetText.tr("Error logging in. Check console for more information"));
                LogManager.error("Error logging into Microsoft account: " + URLDecoder
                    .decode(req.getParams().get("error_description"), StandardCharsets.UTF_8.toString()));
                close();
                return 0;
            }

            if (!req.getParams().containsKey("code")) {
                res.getHeaders().add("Content-Type", "text/plain");
                res.send(400, GetText.tr("Code is missing"));
                close();
                return 0;
            }

            try {
                acquireAccessToken(req.getParams().get("code"));
            } catch (Exception e) {
                LogManager.logStackTrace("Error acquiring accessToken", e);
                res.getHeaders().add("Content-Type", "text/html");
                res.send(500, GetText.tr("Error logging in. Check console for more information"));
                close();
                return 0;
            }

            res.getHeaders().add("Content-Type", "text/plain");
            // #. {0} is the name of the launcher (ATLauncher)
            res.send(200, GetText.tr("Login complete. You can now close this window and go back to {0}",
                Constants.LAUNCHER_NAME));
            close();
            return 0;
        }, "GET");

        server.start();
    }

    private void addAccount(OauthTokenResponse oauthTokenResponse, XboxLiveAuthResponse xstsAuthResponse,
                            LoginResponse loginResponse, Profile profile) throws Exception {
        if (account != null || AccountManager.isAccountByName(loginResponse.username)) {
            MicrosoftAccount existingAccount = AccountManager.getAccountByName(loginResponse.username);

            if (existingAccount == null) {
                return;
            }

            // if forced to relogin, then make sure they logged into correct account
            if (this.account != null && !existingAccount.username.equals(this.account.username)) {
                DialogManager.okDialog().setTitle(GetText.tr("Incorrect account"))
                    .setContent(
                        GetText.tr("Logged into incorrect account. Please login again on the Accounts tab."))
                    .setType(DialogManager.ERROR).show();
                return;
            }

            existingAccount.update(oauthTokenResponse, xstsAuthResponse, loginResponse, profile);
            AccountManager.saveAccounts();
        } else {
            MicrosoftAccount newAccount = new MicrosoftAccount(oauthTokenResponse, xstsAuthResponse, loginResponse,
                profile);

            AccountManager.addAccount(newAccount);
            this.account = newAccount;
        }
    }

    private void acquireAccessToken(String authcode) throws Exception {
        OauthTokenResponse oauthTokenResponse = MicrosoftAuthAPI.tradeCodeForAccessToken(authcode);

        acquireXBLToken(oauthTokenResponse);
    }

    private void acquireXBLToken(OauthTokenResponse oauthTokenResponse) throws Exception {
        XboxLiveAuthResponse xblAuthResponse = MicrosoftAuthAPI.getXBLToken(oauthTokenResponse.accessToken);

        acquireXsts(oauthTokenResponse, xblAuthResponse.token);
    }

    private void acquireXsts(OauthTokenResponse oauthTokenResponse, String xblToken) throws Exception {
        XboxLiveAuthResponse xstsAuthResponse = null;

        try {
            xstsAuthResponse = MicrosoftAuthAPI.getXstsToken(xblToken);
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

            CreateMinecraftProfileDialog createMinecraftProfileDialog = new CreateMinecraftProfileDialog(
                loginResponse.accessToken);
            createMinecraftProfileDialog.setVisible(true);

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
