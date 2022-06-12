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
package com.atlauncher.gui.tabs;

import com.atlauncher.constants.Constants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;

/**
 * 14 / 04 / 2022
 *
 * The about tab displays to the user some basic information in regard to
 * the current state of ATLauncher, and some other basic diagnostic information
 * to let users more easily report errors.
 */
public class AboutTab extends JPanel implements Tab, RelocalizationListener {
    private final JPanel info;
    private final JTextPane textInfo;


    private final JButton copyButton;

    public AboutTab() {
        setLayout(new BorderLayout());

        textInfo = new JTextPane();
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.LAUNCHER_NAME);
        sb.append("\n");
        sb.append("Version:\t").append(Constants.VERSION.toString());
        sb.append("\n");
        sb.append("OS:\t").append(System.getProperty("os.name"));
        sb.append("\n");
        sb.append("Java:\t");
        sb.append(String.format("Java %d (%s)", Java.getLauncherJavaVersionNumber(), Java.getLauncherJavaVersion()));
        textInfo.setText(sb.toString());
        textInfo.setEditable(false);

        info = new JPanel();
        info.setLayout(new BorderLayout());
        info.add(textInfo, BorderLayout.WEST);

        copyButton = new JButton();
        copyButton.setText(GetText.tr("Copy"));
        copyButton.addActionListener(e -> {
                OS.copyToClipboard(sb.toString());
            }
        );
        info.add(copyButton, BorderLayout.EAST);

        this.add(info, BorderLayout.NORTH);

        RelocalizationManager.addListener(this);
    }

    @Override
    public void onRelocalization() {
        // TODO Request Ryan explain this to me
    }

    @Override
    public String getTitle() {
        return GetText.tr("About");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "About";
    }
}
