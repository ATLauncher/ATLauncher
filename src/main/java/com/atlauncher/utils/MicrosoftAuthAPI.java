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
package com.atlauncher.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.microsoft.Entitlements;
import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.OauthDeviceCodeResponse;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.data.microsoft.XboxLiveAuthResponse;
import com.atlauncher.network.DownloadException;
import com.atlauncher.network.NetworkClient;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Various utility methods for interacting with the Microsoft Auth API.
 */
public class MicrosoftAuthAPI {
    public static OauthDeviceCodeResponse getDeviceCode() {
        RequestBody data = new FormBody.Builder().add("client_id", Constants.MICROSOFT_LOGIN_CLIENT_ID)
                .add("scope", String.join(" ", Constants.MICROSOFT_LOGIN_SCOPES)).build();

        OauthDeviceCodeResponse deviceCodeResponse = NetworkClient.post(Constants.MICROSOFT_DEVICE_CODE_URL,
                Headers.of("Content-Type", "application/x-www-form-urlencoded"), data, OauthDeviceCodeResponse.class);

        return deviceCodeResponse;
    }

    public static OauthTokenResponse checkDeviceCodeForToken(String deviceCode) throws IOException {
        RequestBody data = new FormBody.Builder().add("client_id", Constants.MICROSOFT_LOGIN_CLIENT_ID)
                .add("device_code", deviceCode).add("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                .build();

        OauthTokenResponse deviceCodeTokenResponse = NetworkClient.post(Constants.MICROSOFT_AUTH_TOKEN_URL,
                Headers.of("Content-Type", "application/x-www-form-urlencoded"), data, OauthTokenResponse.class);

        return deviceCodeTokenResponse;
    }

    public static OauthTokenResponse refreshAccessToken(String refreshToken) {
        RequestBody data = new FormBody.Builder().add("client_id", Constants.MICROSOFT_LOGIN_CLIENT_ID)
                .add("refresh_token", refreshToken).add("grant_type", "refresh_token")
                .add("redirect_uri", Constants.MICROSOFT_LOGIN_REDIRECT_URL).build();

        OauthTokenResponse oauthTokenResponse = NetworkClient.post(Constants.MICROSOFT_AUTH_TOKEN_URL,
                Headers.of("Content-Type", "application/x-www-form-urlencoded"), data, OauthTokenResponse.class);

        return oauthTokenResponse;
    }

    public static XboxLiveAuthResponse getXBLToken(String accessToken) {
        Map<Object, Object> properties = new HashMap<>();
        properties.put("AuthMethod", "RPS");
        properties.put("SiteName", "user.auth.xboxlive.com");
        properties.put("RpsTicket", "d=" + accessToken);

        Map<Object, Object> data = new HashMap<>();
        data.put("Properties", properties);
        data.put("RelyingParty", "http://auth.xboxlive.com");
        data.put("TokenType", "JWT");

        XboxLiveAuthResponse xblAuthResponse = NetworkClient.post(Constants.MICROSOFT_XBL_AUTH_TOKEN_URL,
                Headers.of("Content-Type", "application/json", "Accept", "application/json", "x-xbl-contract-version",
                        "1"),
                RequestBody.create(Gsons.DEFAULT.toJson(data), MediaType.get("application/json; charset=utf-8")),
                XboxLiveAuthResponse.class);

        return xblAuthResponse;
    }

    public static XboxLiveAuthResponse getXstsToken(String xblToken) throws IOException {
        Map<Object, Object> properties = new HashMap<>();
        properties.put("SandboxId", "RETAIL");

        List<String> userTokens = new ArrayList<>();
        userTokens.add(xblToken);
        properties.put("UserTokens", userTokens);

        Map<Object, Object> data = new HashMap<>();
        data.put("Properties", properties);
        data.put("RelyingParty", "rp://api.minecraftservices.com/");
        data.put("TokenType", "JWT");

        XboxLiveAuthResponse xstsAuthResponse = NetworkClient.post(Constants.MICROSOFT_XSTS_AUTH_TOKEN_URL,
                Headers.of("Content-Type", "application/json", "Accept", "application/json", "x-xbl-contract-version",
                        "1"),
                RequestBody.create(Gsons.DEFAULT.toJson(data), MediaType.get("application/json; charset=utf-8")),
                XboxLiveAuthResponse.class);

        return xstsAuthResponse;
    }

    public static LoginResponse loginToMinecraft(String xstsToken) {
        Map<Object, Object> data = new HashMap<>();
        data.put("xtoken", xstsToken);
        data.put("platform", "PC_LAUNCHER");

        LoginResponse loginResponse = NetworkClient.post(Constants.MICROSOFT_MINECRAFT_LOGIN_URL,
                Headers.of("Content-Type", "application/json", "Accept", "application/json"),
                RequestBody.create(Gsons.DEFAULT.toJson(data), MediaType.get("application/json; charset=utf-8")),
                LoginResponse.class);

        return loginResponse;
    }

    public static Entitlements getEntitlements(String accessToken) {
        Entitlements entitlementsResponse = NetworkClient.get(
                String.format("%s?requestId=%s", Constants.MICROSOFT_MINECRAFT_ENTITLEMENTS_URL,
                        UUID.randomUUID()),
                Headers.of("Authorization", "Bearer " + accessToken, "Content-Type", "application/json", "Accept",
                        "application/json"),
                Entitlements.class);

        return entitlementsResponse;
    }

    @Nullable
    public static Profile getMcProfile(String accessToken) throws DownloadException {
        return NetworkClient.getWithThrow(
                Constants.MICROSOFT_MINECRAFT_PROFILE_URL,
                Headers.of("Authorization", "Bearer " + accessToken),
                Profile.class);
    }
}
