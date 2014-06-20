/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.json;

import com.atlauncher.annot.Json;

@Json
public class Action {
    private String mod;
    private ServerAction action;
    private ActionType type;
    private ActionAfter after;
    private String saveAs;
    private boolean client;
    private boolean server;

    public String getMod() {
        return this.mod;
    }

    public ServerAction getAction() {
        return this.action;
    }

    public ActionType getType() {
        return this.type;
    }

    public ActionAfter getAfter() {
        return this.after;
    }

    public String getSaveAs() {
        return this.saveAs;
    }

    public boolean isForClient() {
        return this.client;
    }

    public boolean isForServer() {
        return this.server;
    }
}