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
package com.atlauncher.data.microsoft;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class OauthTokenResponseTypeAdapter implements JsonDeserializer<OauthTokenResponse> {
    @Override
    public OauthTokenResponse deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        OauthTokenResponse oauthTokenResponse = new OauthTokenResponse();
        JsonObject rootObject = json.getAsJsonObject();

        oauthTokenResponse.tokenType = rootObject.get("token_type").getAsString();
        oauthTokenResponse.expiresIn = rootObject.get("expires_in").getAsInt();
        oauthTokenResponse.scope = rootObject.get("scope").getAsString();
        oauthTokenResponse.accessToken = rootObject.get("access_token").getAsString();
        oauthTokenResponse.refreshToken = rootObject.get("refresh_token").getAsString();
        oauthTokenResponse.userId = rootObject.get("user_id").getAsString();

        if (rootObject.has("foci")) {
            oauthTokenResponse.foci = rootObject.get("foci").getAsString();
        }

        if (rootObject.has("expires_at")) {
            oauthTokenResponse.expiresAt = context.deserialize(rootObject.get("expires_at"), Date.class);
        } else {
            oauthTokenResponse.expiresAt = new Date();
            oauthTokenResponse.expiresAt
                    .setTime(oauthTokenResponse.expiresAt.getTime() + (oauthTokenResponse.expiresIn * 1000));
        }

        return oauthTokenResponse;
    }
}
