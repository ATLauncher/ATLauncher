/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.json;

import com.atlauncher.App;
import com.atlauncher.annot.Json;
import com.atlauncher.data.Pack;
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
        String[] options = {App.settings.getLocalizedString("common.ok"), App.settings.getLocalizedString("common" +
                ".cancel")};
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
        return JOptionPane.showOptionDialog(App.settings.getParent(), ep, App.settings.getLocalizedString("common" +
                ".installing") + " " + pack.getName(), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                options, options[0]);
    }

    public String getUpdateMessage() {
        return this.update;
    }

    public boolean hasUpdateMessage() {
        return this.update != null;
    }

    public int showUpdateMessage(Pack pack) {
        String[] options = {App.settings.getLocalizedString("common.ok"), App.settings.getLocalizedString("common" +
                ".cancel")};
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
        return JOptionPane.showOptionDialog(App.settings.getParent(), ep, App.settings.getLocalizedString("common" +
                ".reinstalling") + " " + pack.getName(), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
    }
}
