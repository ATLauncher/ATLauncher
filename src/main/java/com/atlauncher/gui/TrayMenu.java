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

import java.awt.SystemTray;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.evnt.listener.ConsoleCloseListener;
import com.atlauncher.evnt.listener.ConsoleOpenListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.strings.Noun;
import com.atlauncher.strings.Sentence;
import com.atlauncher.strings.Verb;
import com.atlauncher.utils.OS;

@SuppressWarnings("serial")
public final class TrayMenu extends JPopupMenu implements ConsoleCloseListener, ConsoleOpenListener {

    private final JMenuItem killMinecraftButton = new JMenuItem(Sentence.ACT_KILL_X.insert(Noun.MINECRAFT).toString());
    private final JMenuItem toggleConsoleButton = new JMenuItem(Sentence.ACT_TOGGLE_X.insert(Noun.CONSOLE).toString());
    private final JMenuItem openLauncherFolderButton = new JMenuItem(Sentence.BASE_ABC.capitalize()
        .insert(Verb.OPEN, Verb.FUTURE)
        .insert(Noun.LAUNCHER)
        .insert(Noun.DIRECTORY)
        .toString()
    );
    private final JMenuItem quitButton = new JMenuItem(Verb.CLOSE.toString(Verb.FUTURE));

    public TrayMenu() {
        super();

        this.setMinecraftLaunched(false);

        this.add(this.killMinecraftButton);
        this.add(this.toggleConsoleButton);
        this.addSeparator();
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
                int ret = DialogManager.yesNoDialog().setTitle(Sentence.ACT_KILL_X.insert(Noun.MINECRAFT))
                        .setContent(new HTMLBuilder().center().text(Sentence.MSG_KILL_MINECRAFT).build())
                        .setType(DialogManager.ERROR).show();

                if (ret == DialogManager.YES_OPTION) {
                    App.launcher.killMinecraft();
                }
            }
        }));
        this.toggleConsoleButton.addActionListener(e -> App.console.setVisible(!App.console.isVisible()));
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
        this.toggleConsoleButton.setText(Sentence.BASE_AB.capitalize()
            .insert(Verb.OPEN, Verb.FUTURE)
            .insert(Noun.CONSOLE)
            .toString()
        );
    }

    @Override
    public void onConsoleOpen() {
        this.toggleConsoleButton.setText(Sentence.BASE_AB.capitalize()
            .insert(Verb.CLOSE, Verb.FUTURE)
            .insert(Noun.CONSOLE)
            .toString()
        );
    }
}
