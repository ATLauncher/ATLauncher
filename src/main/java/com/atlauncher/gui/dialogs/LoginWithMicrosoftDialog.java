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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.swing.JDialog;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.data.microsoft.Store;
import com.atlauncher.data.microsoft.XboxLiveAuthErrorResponse;
import com.atlauncher.data.microsoft.XboxLiveAuthResponse;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.DownloadException;
import com.atlauncher.utils.MicrosoftAuthAPI;
import com.atlauncher.utils.OS;

import org.mini2Dx.gettext.GetText;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.VirtualHost;

@SuppressWarnings("serial")
public final class LoginWithMicrosoftDialog extends JDialog {
    private static HTTPServer server = new HTTPServer(Constants.MICROSOFT_LOGIN_REDIRECT_PORT);
    private static VirtualHost host = server.getVirtualHost(null);

    private MicrosoftAccount account = null;

    public LoginWithMicrosoftDialog() {
        this(null);
    }

    public LoginWithMicrosoftDialog(MicrosoftAccount account) {
        super(App.launcher.getParent(), GetText.tr("Login with Microsoft"), ModalityType.DOCUMENT_MODAL);

        this.account = account;
        this.setMinimumSize(new Dimension(400, 400));
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        this.add(new LoadingPanel(GetText.tr("Browser opened to complete the login process")), BorderLayout.CENTER);

        setVisible(false);
        dispose();

        OS.openWebBrowser(Constants.MICROSOFT_LOGIN_URL);

        try {
            startServer();
        } catch (IOException e) {
            LogManager.logStackTrace("Error starting web server for Microsoft login", e);

            close();
        }

        this.setLocationRelativeTo(App.launcher.getParent());
        this.setVisible(true);
    }

    private void close() {
        server.stop();
        setVisible(false);
        dispose();
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
            // #. {0} is the name of the launcher
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
            MicrosoftAccount account = (MicrosoftAccount) AccountManager.getAccountByName(loginResponse.username);

            if (account == null) {
                return;
            }

            // if forced to relogin, then make sure they logged into correct account
            if (account != null && this.account != null && account.username != this.account.username) {
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
                XboxLiveAuthErrorResponse xboxLiveAuthErrorResponse = Gsons.DEFAULT.fromJson(e.response,
                        XboxLiveAuthErrorResponse.class);

                String error = xboxLiveAuthErrorResponse.getErrorMessageForCode();

                if (error != null) {
                    DialogManager.okDialog().setTitle(GetText.tr("Error logging into Xbox Live"))
                            .setContent(new HTMLBuilder().center().text(error).build()).setType(DialogManager.ERROR)
                            .show();

                    String link = xboxLiveAuthErrorResponse.getBrowserLinkForCode();

                    if (link != null) {
                        OS.openWebBrowser(link);
                    }
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

        Store store = MicrosoftAuthAPI.getMcEntitlements(loginResponse.accessToken);

        if (!(store.items.stream().anyMatch(i -> i.name.equalsIgnoreCase("product_minecraft"))
                && store.items.stream().anyMatch(i -> i.name.equalsIgnoreCase("game_minecraft")))) {
            DialogManager.okDialog().setTitle(GetText.tr("Minecraft Has Not Been Purchased"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "This account doesn't have a valid purchase of Minecraft.<br/><br/>Please make sure you've bought the Java edition of Minecraft and then try again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
            throw new Exception("Account does not own Minecraft");
        }

        Profile profile = MicrosoftAuthAPI.getMcProfile(loginResponse.accessToken);

        if (profile == null) {
            throw new Exception("Failed to get Minecraft profile");
        }

        // add the account
        addAccount(oauthTokenResponse, xstsAuthResponse, loginResponse, profile);
    }
}
