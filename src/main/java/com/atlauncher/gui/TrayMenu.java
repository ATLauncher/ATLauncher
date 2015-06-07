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
import com.atlauncher.annot.Subscribe;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.utils.HTMLUtils;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class TrayMenu extends JPopupMenu {
    private final JMenuItem killMCButton = new JMenuItem();
    private final JMenuItem tcButton = new JMenuItem();
    private final JMenuItem quitButton = new JMenuItem();

    public TrayMenu() {
        super();

        this.setMinecraftLaunched(false);

        this.killMCButton.setText(LanguageManager.localize("console.kill"));

        if (App.console.isVisible()) {
            this.tcButton.setText(LanguageManager.localize("console.hide"));
        } else {
            this.tcButton.setText(LanguageManager.localize("console.show"));
        }

        this.quitButton.setText(LanguageManager.localize("common.quit"));

        this.tcButton.setEnabled(false);

        this.add(this.killMCButton);
        this.add(this.tcButton);
        this.addSeparator();
        this.add(this.quitButton);

        EventHandler.EVENT_BUS.subscribe(this);

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
                            int ret = JOptionPane.showConfirmDialog(App.frame, HTMLUtils
                                    .centerParagraph(LanguageManager.localizeWithReplace("console" + "" +
                                    ".killsure", "<br/><br/>")), LanguageManager.localize("console" + "" +
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
                App.console.setVisible(!App.console.isVisible());
            }
        });

        this.quitButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }
                });
    }

    @Subscribe
    private void onConsoleClose(EventHandler.ConsoleCloseEvent e) {
        this.tcButton.setText(LanguageManager.localize("console.show"));
    }

    @Subscribe
    private void onConsoleOpen(EventHandler.ConsoleOpenEvent e) {
        this.tcButton.setText(LanguageManager.localize("console.hide"));
    }

    public void setMinecraftLaunched(boolean l) {
        this.killMCButton.setEnabled(l);
    }

    @Subscribe
    public void onRelocalization(EventHandler.RelocalizationEvent e) {
        this.killMCButton.setText(LanguageManager.localize("console.kill"));
        this.quitButton.setText(LanguageManager.localize("common.quit"));
        if (App.console.isVisible()) {
            this.tcButton.setText(LanguageManager.localize("console.hide"));
        } else {
            this.tcButton.setText(LanguageManager.localize("console.show"));
        }
    }
}
