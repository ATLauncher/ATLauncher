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

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class OauthDeviceCodeTokenError {
    public String error;

    @SerializedName("error_description")
    public String errorDescription;

    @SerializedName("error_codes")
    public List<Integer> errorCodes;

    public String timestamp;

    @SerializedName("trace_id")
    public String traceId;

    @SerializedName("correlation_id")
    public String correlationId;

    @SerializedName("error_uri")
    public String errorUri;
}
