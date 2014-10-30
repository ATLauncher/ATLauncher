/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.annot.Json;
import com.atlauncher.exceptions.InvalidPack;

import java.util.List;

@Json
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