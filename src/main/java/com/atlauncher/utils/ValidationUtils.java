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
package com.atlauncher.utils;

import java.util.regex.Pattern;

/**
 * A general class that contains general validations for the user input
 */
public class ValidationUtils {
    /**
     * Example of valid input:
     * localhost,
     * localhost:25565
     * play.server.net
     * play.server.net:25565
     *
     * @return true if the entered server address is valid, otherwise false
     */
    public static boolean isValidMinecraftServerAddress(String serverAddress) {
        Pattern pattern = Pattern.compile("^([A-Za-z0-9.-]+)(:([0-9]{1,5}))?$"); // Less strict pattern
        return pattern.matcher(serverAddress).matches();
    }
}
