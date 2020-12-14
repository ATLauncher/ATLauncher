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
package com.atlauncher.data;

import java.util.Date;
import java.util.Optional;

import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.data.microsoft.XboxLiveAuthResponse;
import com.atlauncher.network.Download;
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
        this.type = "Xbox";

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
    public String getCurrentUsername() {
        // TODO: handle auth failures
        Profile profile = Download.build().setUrl(Constants.MICROSOFT_MINECRAFT_PROFILE_URL)
                .header("Authorization", "Bearer " + this.accessToken).asClass(Profile.class);

        return Optional.of(profile.name).orElse(null);
    }

    @Override
    public String getSkinUrl() {
        // TODO: handle auth failures
        Profile profile = Download.build().setUrl(Constants.MICROSOFT_MINECRAFT_PROFILE_URL)
                .header("Authorization", "Bearer " + this.accessToken).asClass(Profile.class);

        return profile.skins.stream().filter(s -> s.state.equalsIgnoreCase("ACTIVE")).findFirst().map(s -> s.url)
                .orElse(null);
    }

    public void refreshAccessToken() {
        // TODO: handle auth failures

        OauthTokenResponse oauthTokenResponse = MicrosoftAuthAPI.refreshAccessToken(oauthToken.refreshToken);

        this.oauthToken = oauthTokenResponse;

        XboxLiveAuthResponse xboxLiveAuthResponse = MicrosoftAuthAPI.getXBLToken(oauthTokenResponse.accessToken);
        this.xstsAuth = MicrosoftAuthAPI.getXstsToken(xboxLiveAuthResponse.token);

        LoginResponse loginResponse = MicrosoftAuthAPI.loginToMinecraft(this.getIdentityToken());

        this.accessToken = loginResponse.accessToken;
        this.username = loginResponse.username;

        this.accessTokenExpiresAt = new Date();
        this.accessTokenExpiresAt.setTime(this.accessTokenExpiresAt.getTime() + (loginResponse.expiresIn * 1000));
    }

    private String getIdentityToken() {
        return "XBL3.0 x=" + xstsAuth.displayClaims.xui.get(0).uhs + ";" + xstsAuth.token;
    }
}
