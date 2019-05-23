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
package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.Language;
import com.atlauncher.data.Server;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class NetworkCheckerToolPanel extends AbstractToolPanel implements ActionListener, SettingsListener {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 4811953376698111667L;

    private final JLabel TITLE_LABEL = new JLabel(Language.INSTANCE.localize("tools.networkchecker"));

    private final JLabel INFO_LABEL = new JLabel(HTMLUtils.centerParagraph(Utils.splitMultilinedString(Language
            .INSTANCE.localize("tools.networkchecker.info"), 60, "<br>")));

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
        String[] options = {Language.INSTANCE.localize("common.yes"), Language.INSTANCE.localize("common" + ".no")};
        int ret = JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph(Utils
                .splitMultilinedString(Language.INSTANCE.localizeWithReplace("tools.networkcheckerpopup", App
                        .settings.getServers().size() * 20 + " MB.<br/><br/>"), 75, "<br>")), Language.INSTANCE
                .localize("tools.networkchecker"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                options, options[0]);
        if (ret == 0) {
            final ProgressDialog dialog = new ProgressDialog(Language.INSTANCE.localize("tools.networkchecker"), App
                    .settings.getServers().size(), Language.INSTANCE.localize("tools.networkchecker" + "" +
                    ".running"), "Network Checker Tool Cancelled!");
            dialog.addThread(new Thread() {
                @Override
                public void run() {
                    dialog.setTotalTasksToDo(App.settings.getServers().size() * 5);
                    StringBuilder results = new StringBuilder();

                    // Ping Test
                    for (Server server : App.settings.getServers()) {
                        if (server.getHost().contains(":")) {
                            dialog.doneTask();
                            continue;
                        }

                        results.append("Ping results to " + server.getHost() + " was " + Utils.pingAddress(server
                                .getHost()) + "\n\n----------------\n\n");
                        dialog.doneTask();

                        results.append("Tracert to " + server.getHost() + " was " + Utils.traceRoute(server.getHost()));
                        dialog.doneTask();
                    }

                    // Response Code Test
                    for (Server server : App.settings.getServers()) {
                        Downloadable download = new Downloadable(server.getFileURL("launcher/json/hashes.json"), false);
                        results.append(String.format("Response code to %s was %d\n\n----------------\n\n", server
                                .getHost(), download.getResponseCode()));
                        dialog.doneTask();
                    }

                    // Ping Pong Test
                    for (Server server : App.settings.getServers()) {
                        Downloadable download = new Downloadable(server.getFileURL("ping"), false);
                        results.append(String.format("Response to ping on %s was %s\n\n----------------\n\n", server
                                .getHost(), download.getContents()));
                        dialog.doneTask();
                    }

                    // Speed Test
                    for (Server server : App.settings.getServers()) {
                        File file = new File(App.settings.getTempDir(), "20MB.test");
                        if (file.exists()) {
                            Utils.delete(file);
                        }
                        long started = System.currentTimeMillis();

                        Downloadable download = new Downloadable(server.getFileURL("20MB.test"), file);
                        download.download(false);

                        long timeTaken = System.currentTimeMillis() - started;
                        float bps = file.length() / (timeTaken / 1000);
                        float kbps = bps / 1024;
                        float mbps = kbps / 1024;
                        String speed = (mbps < 1 ? (kbps < 1 ? String.format("%.2f B/s", bps) : String.format("%.2f "
                                + "KB/s", kbps)) : String.format("%.2f MB/s", mbps));
                        results.append(String.format("Download speed to %s was %s, " +
                                "" + "taking %.2f seconds to download 20MB\n\n----------------\n\n", server.getHost()
                                , speed, (timeTaken / 1000.0)));
                        dialog.doneTask();
                    }

                    String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Network Test Log", results.toString
                            ());
                    if (result.contains(Constants.PASTE_CHECK_URL)) {
                        LogManager.info("Network Test has finished running, you can view the results at " + result);
                    } else {
                        LogManager.error("Network Test failed to submit to " + Constants.LAUNCHER_NAME + "!");
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
                LogManager.info("Network Test ran and submitted to " + Constants.LAUNCHER_NAME + "!");
                String[] options2 = {Language.INSTANCE.localize("common.ok")};
                JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph(Language.INSTANCE
                        .localizeWithReplace("tools.networkheckercomplete", "<br/><br/>")), Language.INSTANCE
                        .localize("tools" + ".networkchecker"), JOptionPane.DEFAULT_OPTION, JOptionPane
                        .INFORMATION_MESSAGE, null, options2, options2[0]);
            }
        }
    }

    @Override
    public void onSettingsSaved() {
        this.checkLaunchButtonEnabled();
    }
}
