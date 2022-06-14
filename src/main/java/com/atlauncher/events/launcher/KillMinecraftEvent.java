package com.atlauncher.events.launcher;

import com.atlauncher.events.AnalyticsActions;

public final class KillMinecraftEvent extends LauncherEvent {
    KillMinecraftEvent(){
        super(AnalyticsActions.KILL_MINECRAFT);
    }

    public static KillMinecraftEvent of(){
        return new KillMinecraftEvent();
    }
}