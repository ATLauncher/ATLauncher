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
package com.atlauncher.gui;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.listener.ConsoleCloseListener;
import com.atlauncher.evnt.listener.ConsoleOpenListener;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.evnt.manager.RelocalizationManager;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class TrayMenu extends JPopupMenu implements RelocalizationListener, ConsoleCloseListener,
        ConsoleOpenListener {

    private final JMenuItem killMCButton = new JMenuItem();
    private final JMenuItem tcButton = new JMenuItem();
    private final JMenuItem quitButton = new JMenuItem();

    public TrayMenu() {
        super();

        this.setMinecraftLaunched(false);

        this.killMCButton.setText("Kill Minecraft");
        this.tcButton.setText("Toggle Console");
        this.quitButton.setText("Quit");

        this.tcButton.setEnabled(false);

        this.add(this.killMCButton);
        this.add(this.tcButton);
        this.addSeparator();
        this.add(this.quitButton);

        ConsoleCloseManager.addListener(this);
        ConsoleOpenManager.addListener(this);
        RelocalizationManager.addListener(this);

        this.addActionListeners();
    }

    private void addActionListeners() {
        this.killMCButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (App.settings.isMinecraftLaunched()) {
                            int ret = JOptionPane.showConfirmDialog(App.settings.getParent(), "<html><p " +
                                    "align=\"center\">" + Language.INSTANCE.localizeWithReplace("console" + "" +
                                    ".killsure", "<br/><br/>") + "</p></html>", Language.INSTANCE.localize("console" +
                                    ".kill"), JOptionPane.YES_NO_OPTION);

                            if (ret == JOptionPane.YES_OPTION) {
                                App.settings.killMinecraft();
                            }
                        }
                    }
                });
            }
        });
        this.tcButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                App.settings.getConsole().setVisible(!App.settings.getConsole().isVisible());
            }
        });
        this.quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    public void localize() {
        this.tcButton.setEnabled(true);
        this.onRelocalization();
    }

    public void setMinecraftLaunched(boolean l) {
        this.killMCButton.setEnabled(l);
    }

    @Override
    public void onConsoleClose() {
        this.tcButton.setText(Language.INSTANCE.localize("console.show"));
    }

    @Override
    public void onConsoleOpen() {
        this.tcButton.setText(Language.INSTANCE.localize("console.hide"));
    }

    @Override
    public void onRelocalization() {
        this.killMCButton.setText(Language.INSTANCE.localize("console.kill"));
        this.quitButton.setText(Language.INSTANCE.localize("common.quit"));
        if (App.settings.getConsole().isVisible()) {
            this.tcButton.setText(Language.INSTANCE.localize("console.hide"));
        } else {
            this.tcButton.setText(Language.INSTANCE.localize("console.show"));
        }
    }
}
