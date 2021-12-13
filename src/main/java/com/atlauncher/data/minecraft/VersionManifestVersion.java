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

    public boolean isLog4ShellExploitable() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // check if the release is after 1.7 and before 1.18.1 release time
        return parsedReleaseTime.isAfter(ISODateTimeFormat.dateTimeParser().parseDateTime("2013-10-21T15:04:05+00:00"))
                && parsedReleaseTime
                        .isBefore(ISODateTimeFormat.dateTimeParser().parseDateTime("2021-12-09T08:23:00+00:00"));
    }

    public String getLog4JFile() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // 1.7 to 1.11.2
        if (parsedReleaseTime.isAfter(ISODateTimeFormat.dateTimeParser().parseDateTime("2013-10-21T15:04:05+00:00"))
                && parsedReleaseTime
                        .isBefore(ISODateTimeFormat.dateTimeParser().parseDateTime("2016-12-20T09:29:12+00:00"))) {
            return "/server-scripts/logging-configs/vanilla-1.7.xml";
        }

        return "/server-scripts/logging-configs/vanilla-1.12.xml";
    }

    public String getLog4JFileForge() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // 1.7 to 1.12
        if (parsedReleaseTime.isAfter(ISODateTimeFormat.dateTimeParser().parseDateTime("2013-10-21T15:04:05+00:00"))
                && parsedReleaseTime
                        .isBefore(ISODateTimeFormat.dateTimeParser().parseDateTime("2017-06-01T13:50:27+00:00"))) {
            return "/server-scripts/logging-configs/forge-1.7.xml";
        }

        // 1.12 to 1.12.2
        if (parsedReleaseTime.isAfter(ISODateTimeFormat.dateTimeParser().parseDateTime("2017-06-01T13:50:27+00:00"))
                && parsedReleaseTime
                        .isBefore(ISODateTimeFormat.dateTimeParser().parseDateTime("2017-09-17T08:39:46+00:00"))) {
            return "/server-scripts/logging-configs/forge-1.12.xml";
        }

        // 1.13 to 1.16.3
        if (parsedReleaseTime.isAfter(ISODateTimeFormat.dateTimeParser().parseDateTime("2018-07-17T15:11:46+00:00"))
                && parsedReleaseTime
                        .isBefore(ISODateTimeFormat.dateTimeParser().parseDateTime("2020-09-11T13:42:37+00:00"))) {
            return "/server-scripts/logging-configs/forge-1.13.xml";
        }

        return "/server-scripts/logging-configs/vanilla-1.16.4.xml";
    }
}
