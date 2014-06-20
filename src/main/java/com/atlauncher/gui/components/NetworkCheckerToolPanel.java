/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.Server;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.utils.Utils;

public class NetworkCheckerToolPanel extends AbstractToolPanel implements ActionListener,
        SettingsListener {
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
        SettingsManager.addListener(this);
        this.checkLaunchButtonEnabled();
    }

    private void checkLaunchButtonEnabled() {
        LAUNCH_BUTTON.setEnabled(App.settings.enableLogs());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String[] options = { App.settings.getLocalizedString("common.yes"),
                App.settings.getLocalizedString("common.no") };
        int ret = JOptionPane.showOptionDialog(
                App.settings.getParent(),
                "<html><p align=\"center\">"
                        + Utils.splitMultilinedString(App.settings.getLocalizedString(
                                "tools.networkcheckerpopup", App.settings.getServers().size() * 20
                                        + " MB.<br/><br/>"), 75, "<br>") + "</p></html>",
                App.settings.getLocalizedString("tools.networkchecker"),
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        if (ret == 0) {
            final ProgressDialog dialog = new ProgressDialog(
                    App.settings.getLocalizedString("tools.networkchecker"), App.settings
                            .getServers().size() + 1,
                    App.settings.getLocalizedString("tools.networkchecker.running"),
                    "Network Checker Tool Cancelled!");
            dialog.addThread(new Thread() {
                @Override
                public void run() {
                    dialog.setTotalTasksToDo((App.settings.getServers().size() * 4) + 1);
                    StringBuilder results = new StringBuilder();

                    // Ping Test
                    for (Server server : App.settings.getServers()) {
                        results.append("Ping results to " + server.getHost() + " was "
                                + Utils.pingAddress(server.getHost()) + "\n\n----------------\n\n");
                        dialog.doneTask();
                    }

                    // Traceroute Test
                    results.append("Tracert to www.creeperrepo.net was "
                            + Utils.traceRoute("www.creeperrepo.net"));
                    dialog.doneTask();

                    // Response Code Test
                    for (Server server : App.settings.getServers()) {
                        Downloadable download = new Downloadable(server
                                .getFileURL("launcher/json/hashes.json"), false);
                        results.append(String.format(
                                "Response code to %s was %d\n\n----------------\n\n",
                                server.getHost(), download.getResponseCode()));
                        dialog.doneTask();
                    }

                    // Ping Pong Test
                    for (Server server : App.settings.getServers()) {
                        Downloadable download = new Downloadable(server.getFileURL("ping"), false);
                        results.append(String.format(
                                "Response to ping on %s was %s\n\n----------------\n\n",
                                server.getHost(), download.getContents()));
                        dialog.doneTask();
                    }

                    // Speed Test
                    for (Server server : App.settings.getServers()) {
                        File file = new File(App.settings.getTempDir(), "20MB.test");
                        if (file.exists()) {
                            Utils.delete(file);
                        }
                        long started = System.currentTimeMillis();

                        Downloadable download = new Downloadable(server.getFileURL("20MB.test"),
                                file);
                        download.download(false);

                        long timeTaken = System.currentTimeMillis() - started;
                        float bps = file.length() / (timeTaken / 1000);
                        float kbps = bps / 1024;
                        float mbps = kbps / 1024;
                        String speed = (mbps < 1 ? (kbps < 1 ? String.format("%.2f B/s", bps)
                                : String.format("%.2f KB/s", kbps)) : String.format("%.2f MB/s",
                                mbps));
                        results.append(String
                                .format("Download speed to %s was %s, taking %.2f seconds to download 20MB\n\n----------------\n\n",
                                        server.getHost(), speed, (timeTaken / 1000.0)));
                        dialog.doneTask();
                    }

                    String result = Utils.uploadPaste("ATLauncher Network Test Log",
                            results.toString());
                    if (result.contains(Constants.PASTE_CHECK_URL)) {
                        try {
                            Map<String, String> data = new HashMap<String, String>();
                            data.put("log", result);
                            Utils.sendAPICall("networktest/", data);
                        } catch (IOException e1) {
                            App.settings.logStackTrace(
                                    "Network Test failed to submit to ATLauncher!", e1);
                            dialog.setReturnValue(false);
                        }
                    } else {
                        LogManager.error("Network Test failed to submit to ATLauncher!");
                        dialog.setReturnValue(false);
                    }

                    dialog.doneTask();
                    dialog.setReturnValue(true);
                    dialog.close();
                }
            });
            dialog.start();
            if (dialog.getReturnValue() == null || !(Boolean) dialog.getReturnValue()) {
                LogManager.error("Network Test failed to run!");
            } else {
                LogManager.info("Network Test ran and submitted to ATLauncher!");
                String[] options2 = { App.settings.getLocalizedString("common.ok") };
                JOptionPane.showOptionDialog(
                        App.settings.getParent(),
                        "<html><p align=\"center\">"
                                + App.settings.getLocalizedString("tools.networkheckercomplete",
                                        "<br/><br/>") + "</p></html>",
                        App.settings.getLocalizedString("tools.networkchecker"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                        options2, options2[0]);
            }
        }
    }

    @Override
    public void onSettingsSaved() {
        this.checkLaunchButtonEnabled();
    }
}