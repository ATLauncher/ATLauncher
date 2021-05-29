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
import com.google.gson.annotations.SerializedName;

@Json
public enum VersionManifestVersionType {
    @SerializedName("snapshot")
    SNAPSHOT {
        public String getValue() {
            return "snapshot";
        }

        public String toString() {
            return "Snapshot";
        }
    },

    @SerializedName("release")
    RELEASE {
        public String getValue() {
            return "release";
        }

        public String toString() {
            return "Release";
        }
    },

    @SerializedName("old_beta")
    OLD_BETA {
        public String getValue() {
            return "old_beta";
        }

        public String toString() {
            return "Beta";
        }
    },

    @SerializedName("old_alpha")
    OLD_ALPHA {
        public String getValue() {
            return "old_alpha";
        }

        public String toString() {
            return "Alpha";
        }
    };

    public String getValue() {
        return getValue();
    }
}
