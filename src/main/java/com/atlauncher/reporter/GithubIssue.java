package com.atlauncher.reporter;

import com.atlauncher.data.Settings;

@SuppressWarnings("unused")
public final class GithubIssue {

    private final String title;
    private final String body;
    private final String[] labels;

    public GithubIssue(String title, String body) {
        this.title = title;
        this.body = body;
        this.labels = new String[] { "Bug(s)" };
    }

    @Override
    public String toString() {
        return Settings.gson.toJson(this);
    }

}