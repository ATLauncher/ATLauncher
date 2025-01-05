/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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

import javax.swing.JLabel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.managers.DialogManager;

@SuppressWarnings("serial")
public class LogClearerToolPanel extends AbstractToolPanel {

    public LogClearerToolPanel(IToolsViewModel viewModel) {
        super(GetText.tr("Log Clearer"));

        JLabel INFO_LABEL = new JLabel(new HTMLBuilder().center().split(70).text(GetText.tr(
                "This tool clears out all logs created by the launcher (not included those made by instances) to free up space and old junk."))
                .build());
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(e -> {
            if (e.getSource() == LAUNCH_BUTTON) {
                viewModel.clearLogs();

                DialogManager.okDialog().setType(DialogManager.INFO).setTitle(GetText.tr("Success"))
                    .setContent(GetText.tr("Successfully cleared the logs.")).show();
            }
        });
    }
}
