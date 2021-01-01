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
package com.atlauncher.gui;

import java.awt.SystemTray;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.evnt.listener.ConsoleCloseListener;
import com.atlauncher.evnt.listener.ConsoleOpenListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.managers.DialogManager;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public final class TrayMenu extends JPopupMenu implements ConsoleCloseListener, ConsoleOpenListener {

    private final JMenuItem killMCButton = new JMenuItem();
    private final JMenuItem tcButton = new JMenuItem();
    private final JMenuItem quitButton = new JMenuItem();

    public TrayMenu() {
        super();

        this.setMinecraftLaunched(false);

        this.killMCButton.setText(GetText.tr("Kill Minecraft"));
        this.tcButton.setText(GetText.tr("Toggle console"));
        this.quitButton.setText(GetText.tr("Quit"));

        this.add(this.killMCButton);
        this.add(this.tcButton);
        this.addSeparator();
        this.add(this.quitButton);

        ConsoleCloseManager.addListener(this);
        ConsoleOpenManager.addListener(this);

        this.addActionListeners();
    }

    private void addActionListeners() {
        this.killMCButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            if (App.launcher.minecraftLaunched) {
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Kill Minecraft"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "Are you sure you want to kill the Minecraft process?<br/>Doing so can cause corruption of your saves"))
                                .build())
                        .setType(DialogManager.ERROR).show();

                if (ret == DialogManager.YES_OPTION) {
                    App.launcher.killMinecraft();
                }
            }
        }));
        this.tcButton.addActionListener(e -> App.console.setVisible(!App.console.isVisible()));
        this.quitButton.addActionListener(e -> {
            try {
                if (SystemTray.isSupported()) {
                    SystemTray.getSystemTray().remove(App.trayIcon);
                }
            } catch (Exception ignored) {
            }

            System.exit(0);
        });
    }

    public void setMinecraftLaunched(boolean l) {
        this.killMCButton.setEnabled(l);
    }

    @Override
    public void onConsoleClose() {
        this.tcButton.setText(GetText.tr("Show Console"));
    }

    @Override
    public void onConsoleOpen() {
        this.tcButton.setText(GetText.tr("Hide Console"));
    }
}
