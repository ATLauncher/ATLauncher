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
package com.atlauncher.data.json;

import com.atlauncher.annot.Json;

@Json
public class Java {
    private final int min = 0;
    private final int max = 0;

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public boolean conforms() {
        int javaVersion = com.atlauncher.utils.Java.getMinecraftJavaVersionNumber();
        boolean conforms = true;

        if (this.min != 0 && javaVersion < this.min) {
            conforms = false;
        }

        if (this.max != 0 && javaVersion > this.max) {
            conforms = false;
        }

        return conforms;
    }

    public String getVersionString() {
        if (this.min != 0 && this.max != 0 && this.min == this.max) {
            return "Java " + this.min;
        }

        if (this.min != 0 && this.max != 0 && (this.min + 1) == this.max) {
            return "Java " + this.min + " or " + this.max;
        }

        String string = "";

        if (this.min != 0 && this.max != 0) {
            string += "";
        }

        if (this.min != 0) {
            string += "Java " + this.min + (this.max == 0 ? " minimum" : "");
        }

        if (this.max != 0) {
            string += (string.length() != 0 ? " up to, and including, " : "Nothing newer than ") + "Java " + this.max;
        }

        return string;
    }
}
