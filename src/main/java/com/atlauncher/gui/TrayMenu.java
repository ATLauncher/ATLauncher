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
package com.atlauncher.gui;

import java.awt.Frame;
import java.awt.SystemTray;
import java.awt.Window;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.evnt.listener.ConsoleCloseListener;
import com.atlauncher.evnt.listener.ConsoleOpenListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.OS;

@SuppressWarnings("serial")
public final class TrayMenu extends JPopupMenu implements ConsoleCloseListener, ConsoleOpenListener {

    private final JMenuItem killMinecraftButton = new JMenuItem(GetText.tr("Kill Minecraft"));
    private final JMenuItem toggleConsoleButton = new JMenuItem(GetText.tr("Toggle Console"));
    private final JMenuItem killOpenDialogsButton = new JMenuItem(GetText.tr("Kill Open Dialogs"));
    private final JMenuItem openLauncherFolderButton = new JMenuItem(GetText.tr("Open Launcher Folder"));
    private final JMenuItem quitButton = new JMenuItem(GetText.tr("Quit"));

    public TrayMenu() {
        super();

        this.setMinecraftLaunched(false);

        this.add(this.killMinecraftButton);
        this.add(this.toggleConsoleButton);
        this.addSeparator();
        this.add(this.killOpenDialogsButton);
        this.add(this.openLauncherFolderButton);
        this.addSeparator();
        this.add(this.quitButton);

        ConsoleCloseManager.addListener(this);
        ConsoleOpenManager.addListener(this);

        this.addActionListeners();
    }

    private void addActionListeners() {
        this.killMinecraftButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
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
        this.toggleConsoleButton.addActionListener(e -> App.console.setVisible(!App.console.isVisible()));
        this.killOpenDialogsButton.addActionListener(e -> {
            for (Frame frame : Frame.getFrames()) {
                for (Window window : frame.getOwnedWindows()) {
                    if (window.getName().startsWith("dialog")) {
                        window.setVisible(false);
                        window.dispose();
                    }
                }
            }
        });
        this.openLauncherFolderButton.addActionListener(e -> {
            OS.openFileExplorer(FileSystem.BASE_DIR);
        });
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

    public void setMinecraftLaunched(boolean launched) {
        this.killMinecraftButton.setVisible(launched);
    }

    @Override
    public void onConsoleClose() {
        this.toggleConsoleButton.setText(GetText.tr("Show Console"));
    }

    @Override
    public void onConsoleOpen() {
        this.toggleConsoleButton.setText(GetText.tr("Hide Console"));
    }
}
