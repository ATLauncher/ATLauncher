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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.atlauncher.LogManager;
import com.atlauncher.data.Language;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

public class RelaunchInDebugModePanel extends AbstractToolPanel implements ActionListener {
    private final JLabel TITLE_LABEL = new JLabel(Language.INSTANCE.localize("tools.launchindebugmode"));

    private final JLabel INFO_LABEL = new JLabel(HTMLUtils.centerParagraph(
            Utils.splitMultilinedString(Language.INSTANCE.localize("tools.launchindebugmode.info"), 60, "<br>")));

    public RelaunchInDebugModePanel() {
        TITLE_LABEL.setFont(BOLD_FONT);
        TOP_PANEL.add(TITLE_LABEL);
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(this);
        LAUNCH_BUTTON.setEnabled(!LogManager.showDebug);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == LAUNCH_BUTTON) {
            OS.relaunchInDebugMode();
        }
    }
}
