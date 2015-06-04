/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.data.json;

import com.atlauncher.App;
import com.atlauncher.annot.Json;
import com.atlauncher.data.Pack;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.utils.Utils;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

@Json
public class Messages {
    private String install;
    private String update;

    public boolean hasInstallMessage() {
        return this.install != null;
    }

    public String getInstallMessage() {
        return this.install;
    }

    public int showInstallMessage(Pack pack) {
        String[] options = {LanguageManager.localize("common.ok"), LanguageManager.localize("common.cancel")};
        JEditorPane ep = new JEditorPane("text/html", "<html>" + this.install + "</html>");
        ep.setEditable(false);
        ep.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    Utils.openBrowser(e.getURL());
                }
            }
        });
        return JOptionPane.showOptionDialog(App.settings.getParent(), ep, LanguageManager.localize("common" + "" +
                        ".installing") + " " + pack.getName(), JOptionPane.DEFAULT_OPTION, JOptionPane
                .WARNING_MESSAGE, null, options, options[0]);
    }

    public String getUpdateMessage() {
        return this.update;
    }

    public boolean hasUpdateMessage() {
        return this.update != null;
    }

    public int showUpdateMessage(Pack pack) {
        String[] options = {LanguageManager.localize("common.ok"), LanguageManager.localize("common.cancel")};
        JEditorPane ep = new JEditorPane("text/html", "<html>" + this.update + "</html>");
        ep.setEditable(false);
        ep.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    Utils.openBrowser(e.getURL());
                }
            }
        });
        return JOptionPane.showOptionDialog(App.settings.getParent(), ep, LanguageManager.localize("common" + "" +
                        ".reinstalling") + " " + pack.getName(), JOptionPane.DEFAULT_OPTION, JOptionPane
                .WARNING_MESSAGE, null, options, options[0]);
    }
}
