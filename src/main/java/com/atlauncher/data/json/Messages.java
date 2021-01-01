/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;

import com.atlauncher.annot.Json;
import com.atlauncher.data.Pack;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.OS;

import org.mini2Dx.gettext.GetText;

@Json
public class Messages {
    public String install;
    public String update;

    public boolean hasInstallMessage() {
        return this.install != null;
    }

    public String getInstallMessage() {
        return this.install;
    }

    public int showInstallMessage(Pack pack) {
        JEditorPane ep = new JEditorPane("text/html", "<html>" + this.install + "</html>");
        ep.setEditable(false);
        ep.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OS.openWebBrowser(e.getURL());
            }
        });
        return DialogManager.optionDialog().setTitle(GetText.tr("Installing")).setContent(ep)
                .setType(DialogManager.WARNING).addOption(GetText.tr("Ok"), true).addOption(GetText.tr("Cancel"))
                .show();
    }

    public String getUpdateMessage() {
        return this.update;
    }

    public boolean hasUpdateMessage() {
        return this.update != null;
    }

    public int showUpdateMessage(Pack pack) {
        JEditorPane ep = new JEditorPane("text/html", "<html>" + this.update + "</html>");
        ep.setEditable(false);
        ep.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OS.openWebBrowser(e.getURL());
            }
        });
        return DialogManager.optionDialog().setTitle(GetText.tr("Reinstalling") + " " + pack.getName()).setContent(ep)
                .setType(DialogManager.WARNING).addOption(GetText.tr("Ok"), true).addOption(GetText.tr("Cancel"))
                .show();
    }
}
