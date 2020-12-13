/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.data.Constants;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.data.microsoft.Store;
import com.atlauncher.data.microsoft.XboxLiveAuthResponse;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.atlauncher.utils.OS;

import org.mini2Dx.gettext.GetText;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.VirtualHost;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

@SuppressWarnings("serial")
public final class LoginWithMicrosoftDialog extends JDialog {
    private static HTTPServer server = new HTTPServer(Constants.MICROSOFT_LOGIN_REDIRECT_PORT);
    private static VirtualHost host = server.getVirtualHost(null);

    public LoginWithMicrosoftDialog() {
        super(App.launcher.getParent(), GetText.tr("Login with Microsoft"), ModalityType.APPLICATION_MODAL);

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
                LogManager.error("Error logging into Microsoft account: "
                        + URLDecoder.decode(req.getParams().get("error_description"), StandardCharsets.UTF_8));
                close();
                return 0;
            }

            if (!req.getParams().containsKey("code")) {
                res.getHeaders().add("Content-Type", "text/plain");
                res.send(400, GetText.tr("Code is missing"));
                close();
                return 0;
            }

            String authCode = req.getParams().get("code");
            System.out.println("authCode: " + authCode);

            try {
                acquireAccessToken(authCode);
            } catch (Exception e) {
                LogManager.logStackTrace("Error starting web server for Microsoft login", e);
                res.getHeaders().add("Content-Type", "text/plain");
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

    private void addAccount(OauthTokenResponse oauthTokenResponse, LoginResponse loginResponse, Profile profile) {
        MicrosoftAccount account = new MicrosoftAccount(oauthTokenResponse, loginResponse, profile);

        AccountManager.addAccount(account);
    }

    private void acquireAccessToken(String authcode) {
        RequestBody data = new FormBody.Builder().add("client_id", Constants.MICROSOFT_LOGIN_CLIENT_ID)
                .add("code", authcode).add("grant_type", "authorization_code")
                .add("redirect_uri", Constants.MICROSOFT_LOGIN_REDIRECT_URL)
                .add("scope", String.join(" ", Constants.MICROSOFT_LOGIN_SCOPES)).build();

        OauthTokenResponse oauthTokenResponse = Download.build().setUrl(Constants.MICROSOFT_AUTH_TOKEN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded").post(data)
                .asClass(OauthTokenResponse.class);
        System.out.println("oauthTokenResponse: " + Gsons.DEFAULT.toJson(oauthTokenResponse));

        acquireXBLToken(oauthTokenResponse);
    }

    private void acquireXBLToken(OauthTokenResponse oauthTokenResponse) {
        Map<Object, Object> data = Map.of("Properties",
                Map.of("AuthMethod", "RPS", "SiteName", "user.auth.xboxlive.com", "RpsTicket", "d=" + oauthTokenResponse.accessToken),
                "RelyingParty", "http://auth.xboxlive.com", "TokenType", "JWT");

        XboxLiveAuthResponse xblAuthResponse = Download.build().setUrl(Constants.MICROSOFT_XBL_AUTH_TOKEN_URL)
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .header("x-xbl-contract-version", "1")
                .post(RequestBody.create(Gsons.DEFAULT.toJson(data), MediaType.get("application/json; charset=utf-8")))
                .asClass(XboxLiveAuthResponse.class);
        System.out.println("xblAuthResponse: " + Gsons.DEFAULT.toJson(xblAuthResponse));

        acquireXsts(oauthTokenResponse, xblAuthResponse.token);
    }

    private void acquireXsts(OauthTokenResponse oauthTokenResponse, String xblToken) {
        Map<Object, Object> data = Map.of("Properties", Map.of("SandboxId", "RETAIL", "UserTokens", List.of(xblToken)),
                "RelyingParty", "rp://api.minecraftservices.com/", "TokenType", "JWT");

        XboxLiveAuthResponse xstsAuthResponse = Download.build().setUrl(Constants.MICROSOFT_XSTS_AUTH_TOKEN_URL)
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .header("x-xbl-contract-version", "1")
                .post(RequestBody.create(Gsons.DEFAULT.toJson(data), MediaType.get("application/json; charset=utf-8")))
                .asClass(XboxLiveAuthResponse.class);
        System.out.println("xstsAuthResponse: " + Gsons.DEFAULT.toJson(xstsAuthResponse));

        acquireMinecraftToken(oauthTokenResponse, xstsAuthResponse.displayClaims.xui.get(0).uhs, xstsAuthResponse.token);
    }

    private void acquireMinecraftToken(OauthTokenResponse oauthTokenResponse, String xblUhs, String xblXsts) {
        LoginResponse loginResponse = Download.build().setUrl(Constants.MICROSOFT_MINECRAFT_LOGIN_URL)
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .post(RequestBody.create(
                        Gsons.DEFAULT.toJson(Map.of("identityToken", "XBL3.0 x=" + xblUhs + ";" + xblXsts)),
                        MediaType.get("application/json; charset=utf-8")))
                .asClass(LoginResponse.class);
        System.out.println("loginResponse: " + Gsons.DEFAULT.toJson(loginResponse));

        Store store = Download.build().setUrl(Constants.MICROSOFT_MINECRAFT_STORE_URL)
                .header("Authorization", "Bearer " + loginResponse.accessToken).asClass(Store.class);
        System.out.println("store: " + Gsons.DEFAULT.toJson(store));

        Profile profile = Download.build().setUrl(Constants.MICROSOFT_MINECRAFT_PROFILE_URL)
                .header("Authorization", "Bearer " + loginResponse.accessToken).asClass(Profile.class);
        System.out.println("profile: " + Gsons.DEFAULT.toJson(profile));

        // add the account
        addAccount(oauthTokenResponse, loginResponse, profile);
    }
}
