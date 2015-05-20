package com.atlauncher;

import com.atlauncher.data.Constants;

import java.nio.file.Path;

public final class FileSystemData{
    public static final Path INSTANCES_DATA = FileSystem.CONFIGS.resolve("instancesdata");
    public static final Path CHECKING_SERVERS = FileSystem.CONFIGS.resolve("checkingservers.json");
    public static final Path USER_DATA = FileSystem.CONFIGS.resolve("userdata");
    public static final Path PROPERTIES = FileSystem.CONFIGS.resolve(Constants.LAUNCHER_NAME + ".conf");
}