package com.atlauncher.data.mojang.api;

public class NameHistory {
    private String name;
    private long changedToAt;

    public String getName() {
        return this.name;
    }

    public long getChangedToAt() {
        return this.changedToAt;
    }

    public boolean isAUsernameChange() {
        return this.changedToAt == 0;
    }
}
