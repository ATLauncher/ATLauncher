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
package com.atlauncher.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.data.microsoft.Entitlements;
import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.data.microsoft.XboxLiveAuthResponse;
import com.atlauncher.gui.dialogs.CreateMinecraftProfileDialog;
import com.atlauncher.gui.dialogs.LoginWithMicrosoftDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.DownloadException;
import com.atlauncher.utils.MicrosoftAuthAPI;

public class MicrosoftAccount extends AbstractAccount {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 5483749902584257559L;

    /**
     * The access token.
     */
    public String accessToken;

    /**
     * The Microsoft oauth token.
     */
    public OauthTokenResponse oauthToken;

    /**
     * The xsts auth response.
     */
    public XboxLiveAuthResponse xstsAuth;

    /**
     * The date that the accessToken expires at.
     */
    public Date accessTokenExpiresAt;

    /**
     * If the user must login again. This is usually the result of a failed
     * accessToken refresh.
     */
    public boolean mustLogin;

    public MicrosoftAccount(OauthTokenResponse oauthTokenResponse, XboxLiveAuthResponse xstsAuthResponse,
            LoginResponse loginResponse, Profile profile) {
        update(oauthTokenResponse, xstsAuthResponse, loginResponse, profile);
    }

    public void update(OauthTokenResponse oauthTokenResponse, XboxLiveAuthResponse xstsAuthResponse,
            LoginResponse loginResponse, Profile profile) {
        this.oauthToken = oauthTokenResponse;
        this.xstsAuth = xstsAuthResponse;
        this.accessToken = loginResponse.accessToken;
        this.minecraftUsername = profile.name;
        this.uuid = profile.id;
        this.username = loginResponse.username;
        this.mustLogin = false;

        this.accessTokenExpiresAt = new Date();
        this.accessTokenExpiresAt.setTime(this.accessTokenExpiresAt.getTime() + (loginResponse.expiresIn * 1000));
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String getSessionToken() {
        return accessToken;
    }

    @Override
    public String getUserType() {
        return "msa";
    }

    @Override
    public String getCurrentUsername() {
        Profile profile = null;

        try {
            profile = MicrosoftAuthAPI.getMcProfile(accessToken);
        } catch (Exception e) {
            LogManager.error("Error getting Minecraft profile");
            return null;
        }

        if (profile == null) {
            LogManager.error("Error getting Minecraft profile");
            return null;
        }

        return Optional.of(profile.name).orElse(null);
    }

    @Override
    public void updateSkinPreCheck() {
        this.refreshAccessToken();
    }

    @Override
    public void changeSkinPreCheck() {
        this.refreshAccessToken();
    }

    @Override
    public String getSkinUrl() {
        Profile profile = null;

        try {
            profile = MicrosoftAuthAPI.getMcProfile(accessToken);
        } catch (Exception e) {
            LogManager.error("Error getting Minecraft profile");
            return null;
        }

        if (profile == null) {
            LogManager.error("Error getting Minecraft profile");
            return null;
        }

        return Optional.of(profile.skins).orElse(new ArrayList<>()).stream()
                .filter(s -> s.state.equalsIgnoreCase("ACTIVE")).findFirst().map(s -> s.url).orElse(null);
    }

    public boolean refreshAccessToken() {
        return refreshAccessToken(false);
    }

    public boolean refreshAccessToken(boolean force) {
        try {
            if (force || new Date().after(this.oauthToken.expiresAt)) {
                LogManager.info("Oauth token expired. Attempting to refresh");
                OauthTokenResponse oauthTokenResponse = MicrosoftAuthAPI.refreshAccessToken(oauthToken.refreshToken);

                if (oauthTokenResponse == null) {
                    mustLogin = true;
                    AccountManager.saveAccounts();
                    LogManager.error("Failed to refresh accessToken");
                    return false;
                }

                this.oauthToken = oauthTokenResponse;

                AccountManager.saveAccounts();
            }

            if (force || new Date().after(this.xstsAuth.notAfter)) {
                LogManager.info("xsts auth expired. Attempting to get new auth");
                XboxLiveAuthResponse xboxLiveAuthResponse = MicrosoftAuthAPI.getXBLToken(this.oauthToken.accessToken);
                this.xstsAuth = MicrosoftAuthAPI.getXstsToken(xboxLiveAuthResponse.token);

                if (xstsAuth == null) {
                    mustLogin = true;
                    AccountManager.saveAccounts();
                    LogManager.error("Failed to get XBLToken");
                    return false;
                }

                AccountManager.saveAccounts();
            }

            if (force || new Date().after(this.accessTokenExpiresAt)) {
                LoginResponse loginResponse = MicrosoftAuthAPI.loginToMinecraft(this.getIdentityToken());

                if (loginResponse == null) {
                    mustLogin = true;
                    AccountManager.saveAccounts();
                    LogManager.error("Failed to login to Minecraft");
                    return false;
                }

                Entitlements entitlements = MicrosoftAuthAPI.getEntitlements(loginResponse.accessToken);

                if (!(entitlements.items.stream().anyMatch(i -> i.name.equalsIgnoreCase("product_minecraft"))
                        && entitlements.items.stream().anyMatch(i -> i.name.equalsIgnoreCase("game_minecraft")))) {
                    LogManager.error("This account doesn't have a valid purchase of Minecraft");
                    return false;
                }

                // make sure they have a Minecraft profile before saving logins
                if (!checkAndUpdateProfile(loginResponse.accessToken)) {
                    mustLogin = true;
                    AccountManager.saveAccounts();

                    return false;
                }

                this.mustLogin = false;
                this.accessToken = loginResponse.accessToken;
                this.username = loginResponse.username;

                this.accessTokenExpiresAt = new Date();
                this.accessTokenExpiresAt
                        .setTime(this.accessTokenExpiresAt.getTime() + (loginResponse.expiresIn * 1000));

                AccountManager.saveAccounts();
            }
        } catch (Exception e) {
            mustLogin = true;
            AccountManager.saveAccounts();

            LogManager.logStackTrace("Exception refreshing accessToken", e);
            return false;
        }

        return true;
    }

    /**
     * This will check the user has a Minecraft profile (Game Pass subscribers will
     * not until first login of the Minecraft Launcher).
     *
     * If an account doesn't have a Minecraft profile, then they either don't own
     * Minecraft Java edition, or their Game Pass subscription has expired and no
     * longer has a profile.
     *
     * @param accessToken
     */
    private boolean checkAndUpdateProfile(String accessToken) {
        Profile profile = null;

        try {
            profile = MicrosoftAuthAPI.getMcProfile(accessToken);
        } catch (DownloadException e) {
            LogManager.error("Minecraft profile not found");

            new CreateMinecraftProfileDialog(accessToken);

            try {
                profile = MicrosoftAuthAPI.getMcProfile(accessToken);
            } catch (IOException e1) {
                LogManager.logStackTrace("Failed to get Minecraft profile", e);
                return false;
            }
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to get Minecraft profile", e);
            return false;
        }

        if (profile == null) {
            LogManager.error("Failed to get Minecraft profile");
            return false;
        }

        this.minecraftUsername = profile.name;
        this.uuid = profile.id;

        return true;
    }

    private String getIdentityToken() {
        return "XBL3.0 x=" + xstsAuth.displayClaims.xui.get(0).uhs + ";" + xstsAuth.token;
    }

    public boolean ensureAccountIsLoggedIn() {
        boolean hasCancelled = false;
        while (mustLogin) {
            int ret = DialogManager.okCancelDialog().setTitle(GetText.tr("You Must Login Again"))
                    .setContent(GetText.tr("You must login again in order to continue.")).setType(DialogManager.INFO)
                    .show();

            if (ret != 0) {
                hasCancelled = true;
                break;
            }

            new LoginWithMicrosoftDialog(this);
        }

        if (hasCancelled) {
            return false;
        }

        return true;
    }

    public boolean ensureAccessTokenValid() {
        if (!ensureAccountIsLoggedIn()) {
            return false;
        }

        if (!new Date().after(accessTokenExpiresAt)) {
            return true;
        }

        LogManager.info("Access Token has expired. Attempting to refresh it.");

        try {
            return refreshAccessToken();
        } catch (Exception e) {
            LogManager.logStackTrace("Exception while attempting to refresh access token", e);
        }

        return false;
    }
}
