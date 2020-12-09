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
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.data.Constants;
import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.data.microsoft.Store;
import com.atlauncher.data.microsoft.XboxLiveAuthResponse;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.network.Download;

import org.mini2Dx.gettext.GetText;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

@SuppressWarnings("serial")
public final class LoginWithMicrosoftDialog extends JDialog {

    public LoginWithMicrosoftDialog() {
        super(App.launcher.getParent(), GetText.tr("Login with Microsoft"), ModalityType.APPLICATION_MODAL);

        this.setMinimumSize(new Dimension(600, 650));
        this.setResizable(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JFXPanel jfxPanel = new JFXPanel();
        this.add(jfxPanel, BorderLayout.CENTER);

        Platform.runLater(() -> {
            WebView webView = new WebView();

            webView.getEngine().load(Constants.MICROSOFT_LOGIN_URL);
            webView.getEngine().setJavaScriptEnabled(true);
            webView.setPrefHeight(600);
            webView.setPrefWidth(650);

            // listen to end oauth flow
            webView.getEngine().getHistory().getEntries().addListener((ListChangeListener<WebHistory.Entry>) c -> {
                if (c.next() && c.wasAdded()) {
                    for (WebHistory.Entry entry : c.getAddedSubList()) {
                        if (entry.getUrl().startsWith(Constants.MICROSOFT_REDIRECT_URL_SUFFIX)) {
                            String authCode = entry.getUrl().substring(entry.getUrl().indexOf("=") + 1,
                                    entry.getUrl().indexOf("&"));
                            add(new LoadingPanel(), BorderLayout.CENTER);
                            acquireAccessToken(authCode);
                        }
                    }
                }
            });

            jfxPanel.setScene(new Scene(webView));
        });

        this.setLocationRelativeTo(App.launcher.getParent());
        this.setVisible(true);
    }

    private void acquireAccessToken(String authcode) {
        RequestBody data = new FormBody.Builder().add("client_id", "00000000402b5328").add("code", authcode)
                .add("grant_type", "authorization_code")
                .add("redirect_uri", "https://login.live.com/oauth20_desktop.srf")
                .add("scope", "service::user.auth.xboxlive.com::MBI_SSL").build();

        OauthTokenResponse oauthTokenResponse = Download.build().setUrl(Constants.MICROSOFT_AUTH_TOKEN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded").post(data)
                .asClass(OauthTokenResponse.class);
        System.out.println("oauthTokenResponse: " + Gsons.DEFAULT.toJson(oauthTokenResponse));

        acquireXBLToken(oauthTokenResponse.accessToken);
    }

    private void acquireXBLToken(String accessToken) {
        Map<Object, Object> data = Map.of("Properties",
                Map.of("AuthMethod", "RPS", "SiteName", "user.auth.xboxlive.com", "RpsTicket", accessToken),
                "RelyingParty", "http://auth.xboxlive.com", "TokenType", "JWT");

        XboxLiveAuthResponse xblAuthResponse = Download.build().setUrl(Constants.MICROSOFT_XBL_AUTH_TOKEN_URL)
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .post(RequestBody.create(Gsons.DEFAULT.toJson(data), MediaType.get("application/json; charset=utf-8")))
                .asClass(XboxLiveAuthResponse.class);
        System.out.println("xblAuthResponse: " + Gsons.DEFAULT.toJson(xblAuthResponse));

        acquireXsts(xblAuthResponse.token);
    }

    private void acquireXsts(String xblToken) {
        Map<Object, Object> data = Map.of("Properties", Map.of("SandboxId", "RETAIL", "UserTokens", List.of(xblToken)),
                "RelyingParty", "rp://api.minecraftservices.com/", "TokenType", "JWT");

        XboxLiveAuthResponse xstsAuthResponse = Download.build().setUrl(Constants.MICROSOFT_XSTS_AUTH_TOKEN_URL)
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .post(RequestBody.create(Gsons.DEFAULT.toJson(data), MediaType.get("application/json; charset=utf-8")))
                .asClass(XboxLiveAuthResponse.class);
        System.out.println("xstsAuthResponse: " + Gsons.DEFAULT.toJson(xstsAuthResponse));

        acquireMinecraftToken(xstsAuthResponse.displayClaims.xui.get(0).uhs, xstsAuthResponse.token);
    }

    private void acquireMinecraftToken(String xblUhs, String xblXsts) {
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
    }
}
