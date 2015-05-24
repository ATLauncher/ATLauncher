/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher;

import com.atlauncher.data.Constants;

import java.nio.file.Path;

public final class FileSystemData {
    public static final Path INSTANCES_DATA = FileSystem.CONFIGS.resolve("instancesdata");
    public static final Path CHECKING_SERVERS = FileSystem.CONFIGS.resolve("checkingservers.json");
    public static final Path USER_DATA = FileSystem.CONFIGS.resolve("userdata");
    public static final Path PROPERTIES = FileSystem.CONFIGS.resolve(Constants.LAUNCHER_NAME + ".conf");
}