package com.atlauncher;

import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.data.MinecraftServer;
import com.atlauncher.data.MinecraftVersion;
import com.atlauncher.data.News;
import com.atlauncher.data.Pack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class keeps all the data used by the launcher including the packs on the launcher, users instances and more.
 */
public final class Data {
    public static final List<Instance> INSTANCES = new LinkedList<>();
    public static final List<Account> ACCOUNTS = new LinkedList<>();
    public static final List<News> NEWS = new LinkedList<>();
    public static final List<Pack> PACKS = new LinkedList<>();
    public static final List<MinecraftServer> CHECKING_SERVERS = new LinkedList<>();

    public static final Map<String, MinecraftVersion> MINECRAFT_VERSIONS = new HashMap<>();
}