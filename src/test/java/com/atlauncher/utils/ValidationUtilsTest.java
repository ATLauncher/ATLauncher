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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class ValidationUtilsTest {
    @Test
    public void testIsValidMinecraftServerAddress() {
        // Test localhost
        assertTrue(ValidationUtils.isValidMinecraftServerAddress("localhost"));
        assertTrue(ValidationUtils.isValidMinecraftServerAddress("localhost:25565"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("localhost:255651"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("localhost:"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("localhost:invalid-port"));

        // Test server address that start with a domain
        assertTrue(ValidationUtils.isValidMinecraftServerAddress("play.server.net"));
        assertTrue(ValidationUtils.isValidMinecraftServerAddress("play.server.net:25565"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("play.server.net:255651"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("play.server.net:"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("play.server.net:invalid-port"));

        // Test server address that start with an ip address
        assertTrue(ValidationUtils.isValidMinecraftServerAddress("203.0.113.123"));
        assertTrue(ValidationUtils.isValidMinecraftServerAddress("203.0.113.123:25565"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("203.0.113.123:255651"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("203.0.113.123:"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("203.0.113.123:invalid-port"));

        // Test server address that start with a word
        assertTrue(ValidationUtils.isValidMinecraftServerAddress("play"));
        assertTrue(ValidationUtils.isValidMinecraftServerAddress("play:25565"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("play:255651"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("play:"));
        assertFalse(ValidationUtils.isValidMinecraftServerAddress("play:invalid-port"));
    }
}
