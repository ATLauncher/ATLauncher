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
package com.atlauncher.data.minecraft;

import com.atlauncher.annot.Json;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

@Json
public class VersionManifestVersion {
    public String id;
    public VersionManifestVersionType type;
    public String url;
    public String time;
    public String releaseTime;

    public boolean hasServer() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // check if the release is after 1.2.5 release time
        return parsedReleaseTime.isAfter(ISODateTimeFormat.dateTimeParser().parseDateTime("2012-03-28T22:00:00+00:00"));
    }

    public boolean hasInitSettings() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // check if the release is after 18w48a release time
        return parsedReleaseTime.isAfter(ISODateTimeFormat.dateTimeParser().parseDateTime("2018-11-28T13:11:38+00:00"));
    }
}
