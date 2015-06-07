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

import com.atlauncher.annot.Subscribe;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.dialogs.ServerListForCheckerDialog;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerCheckerToolPanel extends AbstractToolPanel implements ActionListener {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 1964636496849129267L;

    private final JLabel TITLE_LABEL = new JLabel(LanguageManager.localize("tools.serverchecker"));

    private final JLabel INFO_LABEL = new JLabel(HTMLUtils.centerParagraph(Utils.splitMultilinedString
            (LanguageManager.localize("tools.serverchecker.info"), 60, "<br>")));

    public ServerCheckerToolPanel() {
        TITLE_LABEL.setFont(BOLD_FONT);
        TOP_PANEL.add(TITLE_LABEL);
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(this);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        EventHandler.EVENT_BUS.subscribe(this);
        this.checkLaunchButtonEnabled();
    }

    private void checkLaunchButtonEnabled() {
        LAUNCH_BUTTON.setEnabled(SettingsManager.enableServerChecker());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == LAUNCH_BUTTON) {
            new ServerListForCheckerDialog();
        }
    }

    @Subscribe
    public void onSettingsSaved(EventHandler.SettingsChangeEvent e) {
        this.checkLaunchButtonEnabled();
    }
}