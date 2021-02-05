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
package com.atlauncher.gui.tabs.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JLabel;

import com.atlauncher.FileSystem;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class DownloadClearerToolPanel extends AbstractToolPanel implements ActionListener {

    public DownloadClearerToolPanel() {
        super(GetText.tr("Download Clearer"));

        JLabel INFO_LABEL = new JLabel(new HTMLBuilder().center().split(70).text(GetText.tr(
                "This tool clears out all the downloads done by the launcher. This will not affect any instances, but means new pack installs may take longer as it needs to redownload mods."))
                .build());
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == LAUNCH_BUTTON) {
            Analytics.sendEvent("DownloadClearer", "Run", "Tool");

            for (File file : FileSystem.DOWNLOADS.toFile().listFiles()) {
                Utils.delete(file);
            }

            for (File file : FileSystem.FAILED_DOWNLOADS.toFile().listFiles()) {
                Utils.delete(file);
            }

            DialogManager.okDialog().setType(DialogManager.INFO).setTitle(GetText.tr("Success"))
                    .setContent(GetText.tr("Successfully cleared the downloads.")).show();
        }
    }
}
