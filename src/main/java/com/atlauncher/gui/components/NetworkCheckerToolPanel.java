/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;

import com.atlauncher.App;
import com.atlauncher.data.Server;
import com.atlauncher.gui.ProgressDialog;
import com.atlauncher.utils.Utils;

public class NetworkCheckerToolPanel extends AbstractToolPanel implements ActionListener {

    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 4811953376698111667L;

    private final JLabel TITLE_LABEL = new JLabel(
            App.settings.getLocalizedString("tools.networkchecker"));

    private final JLabel INFO_LABEL = new JLabel("<html><p align=\"center\">"
            + Utils.splitMultilinedString(
                    App.settings.getLocalizedString("tools.networkchecker.info"), 60, "<br>")
            + "</p></html>");

    public NetworkCheckerToolPanel() {
        TITLE_LABEL.setFont(BOLD_FONT);
        TOP_PANEL.add(TITLE_LABEL);
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(this);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Launched NetworkCheckerTool");
        String[] options = { App.settings.getLocalizedString("common.yes"),
                App.settings.getLocalizedString("common.no") };
        int ret = JOptionPane.showOptionDialog(
                App.settings.getParent(),
                "<html><p align=\"center\">"
                        + Utils.splitMultilinedString(App.settings.getLocalizedString(
                                "tools.networkcheckerpopup",
                                (App.settings.getServers().size() * 20) + "MB.<br/><br/>"), 75,
                                "<br>") + "</p></html>", App.settings
                        .getLocalizedString("tools.networkchecker"), JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        if (ret == 0) {
            final ProgressDialog dialog = new ProgressDialog(
                    App.settings.getLocalizedString("tools.networkchecker"), App.settings
                            .getServers().size() + 1,
                    App.settings.getLocalizedString("tools.networkchecker.running"),
                    "Network Checker Tool Cancelled!");
            dialog.addThread(new Thread() {
                @Override
                public void run() {
                    dialog.setTotalTasksToDo(App.settings.getServers().size() + 1);
                    StringBuilder results = new StringBuilder();
                    for (Server server : App.settings.getServers()) {
                        results.append("Ping results to " + server.getHost() + " was "
                                + Utils.pingAddress(server.getHost()) + "\n");
                        dialog.doneTask();
                    }
                    results.append("Tracert to www.creeperrepo.net was "
                            + Utils.traceRoute("www.creeperrepo.net"));
                    dialog.doneTask();
                    dialog.setReturnValue(results.toString());
                    dialog.close();
                }
            });
            dialog.start();
            System.out.println("Done");
            System.out.println(dialog.getReturnValue());
        }
    }
}