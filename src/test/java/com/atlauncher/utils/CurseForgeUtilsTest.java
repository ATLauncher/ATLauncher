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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.atlauncher.managers.ConfigManager;

public class CurseForgeUtilsTest {
    @Test
    public void testParseDescriptionForDiscordInvite() {
        // Test with no discord links
        assertNull(CurseForgeUtils.parseDescriptionForDiscordInvite("This is a test description"));

        // Test with discord links
        assertEquals("https://discord.gg/example", CurseForgeUtils
                .parseDescriptionForDiscordInvite("This is a test description https://discord.gg/example"));
        assertEquals("https://discord.com/invite/example", CurseForgeUtils
                .parseDescriptionForDiscordInvite("This is a test description https://discord.com/invite/example"));

        // Test with multiple discord links only gets the first one
        assertEquals("https://discord.gg/example", CurseForgeUtils.parseDescriptionForDiscordInvite(
                "This is a test description https://discord.gg/example https://discord.gg/example2"));
        assertEquals("https://discord.com/invite/example", CurseForgeUtils.parseDescriptionForDiscordInvite(
                "This is a test description https://discord.com/invite/example https://discord.com/invite/example2"));

        // Test with custom links

        try (MockedStatic<ConfigManager> utilities = mockStatic(ConfigManager.class)) {
            utilities
                    .when(() -> ConfigManager.getConfigItem("discordLinkMatching.customLinks", new ArrayList<String>()))
                    .thenReturn(Arrays.asList("example.com/discord"));

            assertEquals("https://example.com/discord", CurseForgeUtils.parseDescriptionForDiscordInvite(
                    "This is a test description https://example.com/discord"));

            utilities
                    .when(() -> ConfigManager.getConfigItem("discordLinkMatching.customLinks", new ArrayList<String>()))
                    .thenReturn(Arrays.asList("example.com/link/discord"));

            assertNull(CurseForgeUtils.parseDescriptionForDiscordInvite(
                    "This is a test description https://example.com/discord"));
        }
    }
}
