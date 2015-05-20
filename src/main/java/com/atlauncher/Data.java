package com.atlauncher;

import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.data.MinecraftServer;

import java.util.LinkedList;
import java.util.List;

public final class Data{
    public static final List<Instance> INSTANCES = new LinkedList<>();
    public static final List<Account> ACCOUNTS = new LinkedList<>();
    public static final List<MinecraftServer> CHECKING_SERVERS = new LinkedList<>();
}