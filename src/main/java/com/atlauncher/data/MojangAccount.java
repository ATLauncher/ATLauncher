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
package com.atlauncher.data;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.mojang.api.MinecraftProfileResponse;
import com.atlauncher.data.mojang.api.ProfileTexture;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Authentication;
import com.atlauncher.utils.MojangAPIUtils;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

public class MojangAccount extends AbstractAccount {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 2979677130644015196L;

    /**
     * The account's password to login to Mojang servers.
     */
    public transient String password;

    /**
     * The encrypted password.
     */
    public String encryptedPassword;

    /**
     * The client token.
     */
    public String clientToken;

    /**
     * If this account should remember the password or not.
     */
    public boolean remember;

    /**
     * This is the store for this username as returned by Mojang.
     */
    public Map<String, Object> store;

    public MojangAccount(String username, String password, LoginResponse response, Boolean remember,
            String clientToken) {
        this(username, password, response.getAuth().getSelectedProfile().getName(),
                response.getAuth().getSelectedProfile().getId().toString(), remember, clientToken,
                response.getAuth().saveForStorage());
    }

    public MojangAccount(String username, String password, String minecraftUsername, String uuid, Boolean remember,
            String clientToken, Map<String, Object> store) {
        this.username = username;
        if (remember) {
            this.password = password;
            this.encryptedPassword = Utils.encrypt(password);
        }
        this.minecraftUsername = minecraftUsername;
        this.uuid = uuid;
        this.remember = remember;
        this.clientToken = clientToken;
        this.store = store;
        this.type = "mojang";
    }

    @Override
    public String getAccessToken() {
        if (this.store == null) {
            return null;
        }

        return (String) this.store.get("accessToken");
    }

    /**
     * Sets the password for this Account.
     *
     * @param password The password for the Account
     */
    public void setPassword(String password) {
        this.password = password;
        this.encryptedPassword = Utils.encrypt(this.password);
    }

    /**
     * Sets this Account to remember or not remember the password.
     *
     * @param remember True if the password should be remembered, False if it
     *                 shouldn't be remembered
     */
    public void setRemember(boolean remember) {
        this.remember = remember;
        if (!this.remember) {
            this.password = "";
            this.encryptedPassword = "";
        }
    }

    @Override
    public String getCurrentUsername() {
        if (this.uuid == null) {
            LogManager.error("The account " + this.minecraftUsername + " has no UUID associated with it !");
            return null;
        }

        return MojangAPIUtils.getCurrentUsername(this.getUUIDNoDashes());
    }

    @Override
    public String getSkinUrl() {
        StringBuilder response;
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + this.getUUIDNoDashes());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");

            connection.setUseCaches(false);

            // Read the result

            if (connection.getResponseCode() != 200) {
                return null;
            }

            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException e) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            response = null;
        }

        if (response == null) {
            return null;
        }

        MinecraftProfileResponse profile = Gsons.DEFAULT.fromJson(response.toString(), MinecraftProfileResponse.class);

        if (!profile.hasProperties()) {
            return null;
        }

        ProfileTexture texture = profile.getUserProperty("textures").getTexture("SKIN");

        if (texture == null) {
            return null;
        }

        return texture.getUrl();
    }

    @Override
    public String getSessionToken() {
        return String.format("token:%s:%s", this.getAccessToken(), this.getUUIDNoDashes());
    }

    public LoginResponse login() {
        LoginResponse response = null;

        if (this.getAccessToken() != null) {
            LogManager.info("Trying to login with access token!");
            response = Authentication.login(this, false);
        }

        if (response == null || response.hasError()) {
            LogManager.error("Access token is NOT valid! Will attempt to get another one!");

            if (!this.remember) {
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                JLabel passwordLabel = new JLabel(GetText.tr("Enter password for {0}", this.minecraftUsername));

                JPasswordField passwordField = new JPasswordField();
                panel.add(passwordLabel, BorderLayout.NORTH);
                panel.add(passwordField, BorderLayout.CENTER);

                int ret = DialogManager.confirmDialog().setTitle(GetText.tr("Enter Password")).setContent(panel).show();

                if (ret == DialogManager.OK_OPTION) {
                    if (passwordField.getPassword().length == 0) {
                        LogManager.error("Aborting login for " + this.minecraftUsername + ", no password entered");
                        App.launcher.setMinecraftLaunched(false);
                        return null;
                    }

                    this.setPassword(new String(passwordField.getPassword()));
                } else {
                    LogManager.error("Aborting login for " + this.minecraftUsername);
                    App.launcher.setMinecraftLaunched(false);
                    return null;
                }
            }

            response = Authentication.login(MojangAccount.this, true);
        }

        if (response.hasError() && !response.isOffline()) {
            LogManager.error(response.getErrorMessage());

            DialogManager
                    .okDialog().setTitle(
                            GetText.tr("Error Logging In"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr("Couldn't login to Minecraft servers")
                            + "<br/><br/>" + response.getErrorMessage()).build())
                    .setType(DialogManager.ERROR).show();

            App.launcher.setMinecraftLaunched(false);
            return null;
        }

        if (!response.isOffline() && !response.getAuth().canPlayOnline()) {
            return null;
        }

        if (!response.isOffline()) {
            this.uuid = response.getAuth().getSelectedProfile().getId().toString();
            this.store = response.getAuth().saveForStorage();
            AccountManager.saveAccounts();
        }

        return response;
    }
}
