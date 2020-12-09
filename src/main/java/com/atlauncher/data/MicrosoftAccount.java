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

import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.network.Download;

public class MicrosoftAccount extends AbstractAccount {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 5483749902584257559L;

    /**
     * The access token.
     */
    public String accessToken;

    public MicrosoftAccount(LoginResponse loginResponse, Profile profile) {
        this.accessToken = loginResponse.accessToken;
        this.minecraftUsername = profile.name;
        this.uuid = profile.id;
        this.username = loginResponse.username;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public void updateUsername() {
        // TODO: Handle failed authorization
        Profile profile = Download.build().setUrl(Constants.MICROSOFT_MINECRAFT_PROFILE_URL)
                .header("Authorization", "Bearer " + this.accessToken).asClass(Profile.class);

        this.minecraftUsername = profile.name;
    }

    @Override
    public void updateSkin() {
        // TODO Auto-generated method stub

    }
}
