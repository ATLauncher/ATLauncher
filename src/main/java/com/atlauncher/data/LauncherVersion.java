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

    public LauncherVersion(int reserved, int major, int minor, int revision) {
        this.reserved = reserved;
        this.major = major;
        this.minor = minor;
        this.revision = revision;
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
                        return false; // Same version so doesn't need to update
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d.%d", this.reserved, this.major, this.minor, this.revision);
    }
}
