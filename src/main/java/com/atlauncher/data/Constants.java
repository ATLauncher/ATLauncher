package com.atlauncher.data;

import com.atlauncher.data.version.LauncherVersion;

public class Constants {
	public static final LauncherVersion VERSION = new LauncherVersion(3, 3, 0, 0, 0);
	   public static final String LAUNCHER_NAME = "ATLauncher";
	   public static final String API_BASE_URL = "https://api.atlauncher.com/v1/launcher/";
	   public static final String PASTE_CHECK_URL = "http://paste.atlauncher.com";
	   public static final String PASTE_API_URL = "http://paste.atlauncher.com/api/create";
	   public static final Server[] SERVERS = new Server[]{new Server("Auto", "download.nodecdn.net/containers/atl", true, false), new Server("Backup Server", "anne.nodeservers.net:8080/containers/atl", false, false), new Server("EU - Amsterdam 1", "bob.nodeservers.net/containers/atl", true, false), new Server("EU - Amsterdam 2", "emma.nodeservers.net/containers/atl", true, false), new Server("EU - Amsterdam 3", "lisa.nodeservers.net/containers/atl", true, false), new Server("US East - Ashburn 1", "anne.nodeservers.net/containers/atl", true, false), new Server("US East - Ashburn 2", "bruce.nodeservers.net/containers/atl", true, false), new Server("US East - Ashburn 3", "dave.nodeservers.net/containers/atl", true, false), new Server("US West - Phoenix 1", "adam.nodeservers.net/containers/atl", true, false), new Server("Master Server (Testing Only)", "master.atlcdn.net", false, true)};
}
