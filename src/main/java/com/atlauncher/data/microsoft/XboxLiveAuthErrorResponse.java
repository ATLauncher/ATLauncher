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

import com.google.gson.annotations.SerializedName;

import org.mini2Dx.gettext.GetText;

//{"Identity":"0","XErr":2148916233,"Message":"","Redirect":"https://start.ui.xboxlive.com/CreateAccount"}
public class XboxLiveAuthErrorResponse {
    @SerializedName("Identity")
    public String identity;

    @SerializedName("XErr")
    public long xErr;

    @SerializedName("Message")
    public String message;

    @SerializedName("Redirect")
    public String redirect;

    public String getErrorMessageForCode() {
        if (xErr == 2148916233l) {
            return GetText.tr(
                    "Account doesn't have an Xbox account.<br/><br/>Please create one by logging into minecraft.net before trying to login again.");
        }

        if (xErr == 2148916238l) {
            return GetText.tr("Child accounts cannot login without being part of a family.");
        }

        return null;
    }

    public String getBrowserLinkForCode() {
        if (xErr == 2148916233l) {
            return "https://minecraft.net/login";
        }

        return null;
    }
}
