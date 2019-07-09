/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.atlauncher.FileSystem;
import com.atlauncher.LogManager;
import com.atlauncher.data.Language;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;

public class CacheClearerToolPanel extends AbstractToolPanel implements ActionListener {
    private static final long serialVersionUID = -1603022084532724719L;

    private final JLabel TITLE_LABEL = new JLabel(Language.INSTANCE.localize("tools.cacheclearer"));

    private final JLabel INFO_LABEL = new JLabel(HTMLUtils.centerParagraph(
            Utils.splitMultilinedString(Language.INSTANCE.localize("tools.cacheclearer.info"), 60, "<br>")));

    public CacheClearerToolPanel() {
        TITLE_LABEL.setFont(BOLD_FONT);
        TOP_PANEL.add(TITLE_LABEL);
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(this);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == LAUNCH_BUTTON) {
            Analytics.sendEvent("CacheClearer", "Run", "Tool");
            try {
                Files.walk(FileSystem.CACHE).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

                DialogManager.okDialog().setType(DialogManager.INFO)
                        .setTitle(Language.INSTANCE.localize("common.success"))
                        .setContent(Language.INSTANCE.localize("tools.cacheclearer.success")).show();
            } catch (IOException e1) {
                LogManager.error("Error clearing cache: " + e1.getMessage());

                DialogManager.okDialog().setType(DialogManager.ERROR)
                        .setTitle(Language.INSTANCE.localize("common.error"))
                        .setContent(Language.INSTANCE.localize("tools.cacheclearer.error")).show();
            }
        }
    }
}
