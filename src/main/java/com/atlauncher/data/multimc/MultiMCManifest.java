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
package com.atlauncher.data.multimc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultiMCManifest {
    public List<MultiMCComponent> components = new ArrayList<>();
    public int formatVersion;

    public transient MultiMCInstanceConfig config;

    public List<MultiMCComponent> getNonStandardComponents() {
        return components.stream().filter(
            c -> (c.disabled == null || !c.disabled) && !c.uid.equalsIgnoreCase(
                "net.minecraft") && !c.uid.equalsIgnoreCase(
                "org.quiltmc.quilt-loader") && !c.uid.equalsIgnoreCase(
                "net.fabricmc.fabric-loader") && !c.uid.equalsIgnoreCase(
                "net.neoforged") && !c.uid.equalsIgnoreCase(
                "net.minecraftforge") && !c.uid.equalsIgnoreCase(
                "com.mumfrey.liteloader") && !c.uid.equalsIgnoreCase(
                "net.fabricmc.intermediary") && !c.uid.equalsIgnoreCase(
                "org.lwjgl3")).collect(Collectors.toList());
    }
}
