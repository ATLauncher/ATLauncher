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

import com.atlauncher.annot.Json;
import com.google.gson.annotations.SerializedName;

@Json
public enum VersionManifestVersionType {
    @SerializedName("experiment")
    EXPERIMENT {
        @Override
        public String getValue() {
            return "experiment";
        }

        @Override
        public String toString() {
            return "Experiment";
        }
    },

    @SerializedName("snapshot")
    SNAPSHOT {
        @Override
        public String getValue() {
            return "snapshot";
        }

        @Override
        public String toString() {
            return "Snapshot";
        }
    },

    @SerializedName("release")
    RELEASE {
        @Override
        public String getValue() {
            return "release";
        }

        @Override
        public String toString() {
            return "Release";
        }
    },

    @SerializedName("old_beta")
    OLD_BETA {
        @Override
        public String getValue() {
            return "old_beta";
        }

        @Override
        public String toString() {
            return "Beta";
        }
    },

    @SerializedName("old_alpha")
    OLD_ALPHA {
        @Override
        public String getValue() {
            return "old_alpha";
        }

        @Override
        public String toString() {
            return "Alpha";
        }
    };

    public String getValue() {
        return "release";
    }
}
