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

import java.util.Locale;

import com.atlauncher.annot.Json;
import com.atlauncher.utils.Hashing;
import com.google.common.hash.HashCode;

@Json
public class LauncherVersion {
    private final int reserved;
    private final int major;
    private final int minor;
    private final int revision;
    private final String stream;
    private final HashCode sha1Revision;

    public LauncherVersion(int reserved, int major, int minor, int revision) {
        this(reserved, major, minor, revision, "Release", Hashing.EMPTY_HASH_CODE);
    }

    public LauncherVersion(int reserved, int major, int minor, int revision, String stream, HashCode sha1Revision) {
        this.reserved = reserved;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.stream = stream;
        this.sha1Revision = sha1Revision;
    }

    public int getReserved() {
        return this.reserved;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getRevision() {
        return this.revision;
    }

    public String getStream() {
        return this.stream;
    }

    public HashCode getSha1Revision() {
        return this.sha1Revision;
    }

    public boolean isReleaseStream() {
        return this.stream.equals("Release");
    }

    public boolean needsUpdate(LauncherVersion toThis) {
        if (this.reserved > toThis.getReserved()) {
            return false;
        } else if (this.reserved < toThis.getReserved()) {
            return true;
        } else {
            if (this.major > toThis.getMajor()) {
                return false;
            } else if (this.major < toThis.getMajor()) {
                return true;
            } else {
                if (this.minor > toThis.getMinor()) {
                    return false;
                } else if (this.minor < toThis.getMinor()) {
                    return true;
                } else {
                    if (this.revision > toThis.getRevision()) {
                        return false;
                    } else if (this.revision < toThis.getRevision()) {
                        return true;
                    } else {
                        // if versions are the same, update if current version is not a release stream
                        // but new version is in release stream
                        return !this.isReleaseStream() && toThis.isReleaseStream();
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        if (this.isReleaseStream()) {
            return String.format("%d.%d.%d.%d [%s]", this.reserved, this.major, this.minor, this.revision,
                    this.sha1Revision);
        }

        return String.format("%d.%d.%d.%d %s [%s]", this.reserved, this.major, this.minor, this.revision, this.stream,
                this.sha1Revision);
    }

    public String toStringForLogging() {
        if (this.isReleaseStream()) {
            return String.format(Locale.ENGLISH, "%d.%d.%d.%d", this.reserved, this.major, this.minor,
                    this.revision);
        }

        return String.format(Locale.ENGLISH, "%d.%d.%d.%d %s", this.reserved, this.major, this.minor,
                this.revision, this.stream);
    }

    public String toStringForUserAgent() {
        if (this.isReleaseStream()) {
            return String.format(Locale.ENGLISH, "%d.%d.%d.%d [%s]", this.reserved, this.major, this.minor,
                    this.revision, this.sha1Revision);
        }

        return String.format(Locale.ENGLISH, "%d.%d.%d.%d.%s [%s]", this.reserved, this.major, this.minor,
                this.revision,
                this.stream, this.sha1Revision);
    }
}
