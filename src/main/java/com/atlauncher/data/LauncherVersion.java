/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

import com.atlauncher.annot.Json;

@Json
public class LauncherVersion {
    private int reserved;
    private int major;
    private int minor;
    private int revision;
    private int build = 0;

    public LauncherVersion(int reserved, int major, int minor, int revision, int build) {
        this.reserved = reserved;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.build = build;
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

    public int getBuild() {
        return this.build;
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
                        return (toThis.getBuild() == 0 ? this.build != 0 : this.build < toThis.getBuild()); // Only
                        // update if the build is lower unless the version to update to is a 0 build which means it's
                        // official and should be updated to
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        if (this.build == 0) {
            return String.format("%d.%d.%d.%d", this.reserved, this.major, this.minor, this.revision);
        } else {
            return String.format("%d.%d.%d.%d Build %d", this.reserved, this.major, this.minor, this.revision, this
                    .build);
        }
    }

    public boolean isBeta() {
        return this.build != 0;
    }
}
