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
package com.atlauncher.data.minecraft;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.atlauncher.annot.Json;

@Json
public class VersionManifestVersion {
    public String id;
    public VersionManifestVersionType type;
    public String url;
    public String time;
    public long size;
    public String sha1;
    public String releaseTime;

    public static DateTime release_1_2_5 = ISODateTimeFormat.dateTimeParser()
            .parseDateTime("2012-03-29T22:00:00+00:00");
    public static DateTime release_18w48a = ISODateTimeFormat.dateTimeParser()
            .parseDateTime("2018-11-29T13:11:38+00:00");
    public static DateTime release_1_7 = ISODateTimeFormat.dateTimeParser().parseDateTime("2013-10-22T15:04:05+00:00");
    public static DateTime release_1_11_2 = ISODateTimeFormat.dateTimeParser()
            .parseDateTime("2016-12-21T09:29:12+00:00");
    public static DateTime release_1_12 = ISODateTimeFormat.dateTimeParser()
            .parseDateTime("2017-06-02T13:50:27+00:00");
    public static DateTime release_1_12_2 = ISODateTimeFormat.dateTimeParser()
            .parseDateTime("2017-09-18T08:39:46+00:00");
    public static DateTime release_1_13 = ISODateTimeFormat.dateTimeParser()
            .parseDateTime("2018-07-18T15:11:46+00:00");
    public static DateTime release_1_13_2 = ISODateTimeFormat.dateTimeParser()
            .parseDateTime("2018-10-22T11:41:07+00:00");
    public static DateTime release_1_16_3 = ISODateTimeFormat.dateTimeParser()
            .parseDateTime("2020-09-10T13:42:37+00:00");
    public static DateTime release_1_18_1 = ISODateTimeFormat.dateTimeParser()
            .parseDateTime("2021-12-10T08:23:00+00:00");

    private boolean isBeforeOrEqualDate(DateTime a, DateTime b) {
        return a.isBefore(b) || a.isEqual(b);
    }

    private boolean isAfterOrEqualDate(DateTime a, DateTime b) {
        return a.isAfter(b) || a.isEqual(b);
    }

    public boolean is1132OrOlder() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // check if the release is before or equal to 1.13.2 release time
        return isBeforeOrEqualDate(parsedReleaseTime, release_1_13_2);
    }

    public boolean hasServer() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // check if the release is after 1.2.5 release time
        return isAfterOrEqualDate(parsedReleaseTime, release_1_2_5);
    }

    public boolean hasInitSettings() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // check if the release is after 18w48a release time
        return isAfterOrEqualDate(parsedReleaseTime, release_18w48a);
    }

    public boolean isLog4ShellExploitable() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // check if the release is after 1.7 and before 1.18.1 release time
        return isAfterOrEqualDate(parsedReleaseTime, release_1_7) && parsedReleaseTime.isBefore(release_1_18_1);
    }

    public String getLog4JFile() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // 1.7 to 1.11.2
        if (isAfterOrEqualDate(parsedReleaseTime, release_1_7) && parsedReleaseTime.isBefore(release_1_11_2)) {
            return "/server-scripts/logging-configs/vanilla-1.7.xml";
        }

        return "/server-scripts/logging-configs/vanilla-1.12.xml";
    }

    public String getLog4JFileForge() {
        DateTime parsedReleaseTime = ISODateTimeFormat.dateTimeParser().parseDateTime(releaseTime);

        // 1.7 to 1.12
        if (isAfterOrEqualDate(parsedReleaseTime, release_1_7) && parsedReleaseTime.isBefore(release_1_12)) {
            return "/server-scripts/logging-configs/forge-1.7.xml";
        }

        // 1.12 to 1.12.2
        if (isAfterOrEqualDate(parsedReleaseTime, release_1_12)
                && isBeforeOrEqualDate(parsedReleaseTime, release_1_12_2)) {
            return "/server-scripts/logging-configs/forge-1.12.xml";
        }

        // 1.13 to 1.16.3
        if (isAfterOrEqualDate(parsedReleaseTime, release_1_13) && parsedReleaseTime.isBefore(release_1_16_3)) {
            return "/server-scripts/logging-configs/forge-1.13.xml";
        }

        return "/server-scripts/logging-configs/forge-1.16.4.xml";
    }
}
