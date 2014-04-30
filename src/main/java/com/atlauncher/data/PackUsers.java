/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

import java.util.List;

import com.atlauncher.App;
import com.atlauncher.exceptions.InvalidPack;

public class PackUsers {

    private int pack;
    private List<String> testers;
    private List<String> allowedPlayers;

    public void addUsers() {
        Pack pack = null;
        try {
            pack = App.settings.getPackByID(this.pack);
        } catch (InvalidPack e) {
            App.settings.logStackTrace(e);
            return;
        }
        if (this.testers != null) {
            pack.addTesters(testers);
        }
        if (this.allowedPlayers != null) {
            pack.addAllowedPlayers(allowedPlayers);
        }
    }
}
