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
package com.atlauncher.data.minecraft.loaders;

import com.atlauncher.annot.Json;
import com.google.gson.annotations.SerializedName;

@Json
public enum LoaderType {
    @SerializedName("fabric")
    FABRIC {
        @Override
        public String toString() {
            return "Fabric";
        }
    },

    @SerializedName("forge")
    FORGE {
        @Override
        public String toString() {
            return "Forge";
        }
    },

    @SerializedName("legacyfabric")
    LEGACY_FABRIC {
        @Override
        public String toString() {
            return "Legacy Fabric";
        }
    },

    @SerializedName("neoforge")
    NEOFORGE {
        @Override
        public String toString() {
            return "NeoForge";
        }
    },

    @SerializedName("paper")
    PAPER {
        @Override
        public String toString() {
            return "Paper";
        }
    },

    @SerializedName("purpur")
    PURPUR {
        @Override
        public String toString() {
            return "Purpur";
        }
    },

    @SerializedName("quilt")
    QUILT {
        @Override
        public String toString() {
            return "Quilt";
        }
    },
}
