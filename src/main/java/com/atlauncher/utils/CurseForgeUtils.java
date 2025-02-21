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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.atlauncher.managers.ConfigManager;

/**
 * Various utility methods for working with CurseForge information.
 */
public class CurseForgeUtils {
    @Nullable
    public static String parseDescriptionForDiscordInvite(String description) {
        if (description == null) {
            return null;
        }

        List<String> customDiscordLinks = ConfigManager
                .getConfigItem("discordLinkMatching.customLinks", new ArrayList<>());
        Optional<String> foundDiscordLink = customDiscordLinks.stream().filter(link -> description.contains(link))
                .findFirst();
        if (foundDiscordLink.isPresent()) {
            return "https://" + foundDiscordLink.get();
        }

        // try parse out discord.gg links
        Pattern pattern = Pattern.compile("(https:\\/\\/discord\\.(?:gg|com\\/invite)\\/[a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(description);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }
}
