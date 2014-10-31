/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.atlauncher.data.json;

import com.atlauncher.App;
import com.atlauncher.annot.Json;
import com.atlauncher.data.Language;
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
        String[] options = {Language.INSTANCE.localize("common.ok"), Language.INSTANCE.localize("common.cancel")};
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
        return JOptionPane.showOptionDialog(App.settings.getParent(), ep, Language.INSTANCE.localize("common" +
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
        String[] options = {Language.INSTANCE.localize("common.ok"), Language.INSTANCE.localize("common.cancel")};
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
        return JOptionPane.showOptionDialog(App.settings.getParent(), ep, Language.INSTANCE.localize("common" +
                ".reinstalling") + " " + pack.getName(), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
    }
}
