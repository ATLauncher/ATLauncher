/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data.mojang.auth;

public class Agent {

    private final String name;
    private final int version;

    public Agent(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return this.name;
    }

    public int getVersion() {
        return this.version;
    }

}
