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
package com.atlauncher.data;

import java.util.Arrays;

import org.mini2Dx.gettext.GetText;

/**
 * The types of the Quick Play options
 * The values are from <a href="https://www.minecraft.net/en-us/article/minecraft-snapshot-23w14a">Minecraft QuickPlay</a>
 */
public enum QuickPlayOption {
    disabled(GetText.tr("Disabled"), null, true),
    singlePlayer(GetText.tr("Single Player"), "--quickPlaySingleplayer", false),
    multiPlayer(GetText.tr("Multiplayer"), "--quickPlayMultiplayer", false),
    realm(GetText.tr("Minecraft Realm"), "--quickPlayRealm", false);

    public final String label;
    public final String argumentRuleValue;
    /**
     * For the minecraft versions that doesn't have Quick plat feature, is this option available on older versions?
     */
    public final boolean compatibleOnOlderVersions;

    /**
     * Get only the compatible options for the current minecraft version regardless if it
     * supports the quick play feature or not, for example joining a minecraft server is supported on older
     * versions but with different way to use it (not using quick play feature)
     */
    public static QuickPlayOption[] compatibleValues(Instance instance) {
        return Arrays.stream(values())
            .filter(quickPlayOption -> {
                if (instance.isQuickPlaySupported(quickPlayOption)) {
                    return true;
                }
                return quickPlayOption.compatibleOnOlderVersions;
            })
            .toArray(QuickPlayOption[]::new);
    }

    QuickPlayOption(String label, String argumentRuleValue, boolean compatibleOnOlderVersions) {
        this.label = label;
        this.argumentRuleValue = argumentRuleValue;
        this.compatibleOnOlderVersions = compatibleOnOlderVersions;
    }
}
