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

public class LibrariesDeleterToolPanel extends AbstractToolPanel {

    public LibrariesDeleterToolPanel(ToolsViewModel viewModel) {
        super(GetText.tr("Libraries Deleter"));

        JLabel INFO_LABEL = new JLabel(new HTMLBuilder().center().split(70).text(GetText.tr(
                "This tool clears out all the libraries used by Minecraft. Instances may need to be reinstalled if launching them once doesn't download all libraries."))
                .build());
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(e -> {
            if (e.getSource() == LAUNCH_BUTTON) {
                viewModel.deleteLibraries();

                DialogManager.okDialog().setType(DialogManager.INFO).setTitle(GetText.tr("Success"))
                        .setContent(GetText.tr("Successfully deleted libraries.")).show();
            }
        });
    }
}
