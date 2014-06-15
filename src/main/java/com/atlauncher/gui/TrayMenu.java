/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.gui;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.ConsoleCloseEvent;
import com.atlauncher.evnt.ConsoleOpenEvent;
import com.atlauncher.evnt.listener.ConsoleCloseListener;
import com.atlauncher.evnt.listener.ConsoleOpenListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;

/**
 * TODO: Rewrite
 */
public final class TrayMenu extends PopupMenu {
	
	public TrayMenu() {
        super();

        this.setMinecraftLaunched(false); // Default Kill MC item to be disabled

        // Setup default labels until proper localization is able to be done
        this.KILLMC_BUTTON.setLabel("Kill Minecraft");
        this.TC_BUTTON.setLabel("Toggle Console");
        this.TC_BUTTON.setEnabled(false);
        this.QUIT_BUTTON.setLabel("Quit");

        this.add(this.KILLMC_BUTTON);
        this.add(this.TC_BUTTON);
        this.addSeparator();
        this.add(this.QUIT_BUTTON);
    }

    public void localize() {
        // Resetup TC Button
        this.TC_BUTTON.setEnabled(true);
        if (App.settings.isConsoleVisible()) {
            this.TC_BUTTON.setLabel(App.settings.getLocalizedString("console.hide"));
        } else {
            this.TC_BUTTON.setLabel(App.settings.getLocalizedString("console.show"));
        }

        ConsoleCloseManager.addListener(new ConsoleCloseListener() {
            @Override
            public void onConsoleClose(ConsoleCloseEvent event) {
                TC_BUTTON.setLabel(Language.INSTANCE.localize("console.show"));
            }
        });
        ConsoleOpenManager.addListener(new ConsoleOpenListener() {
            @Override
            public void onConsoleOpen(ConsoleOpenEvent event) {
                TC_BUTTON.setLabel(Language.INSTANCE.localize("console.hide"));
            }
        });

        // Do localization
        this.KILLMC_BUTTON.setLabel(App.settings.getLocalizedString("console.kill"));
        this.QUIT_BUTTON.setLabel(App.settings.getLocalizedString("common.quit"));
    }

    private final MenuItem KILLMC_BUTTON = new MenuItem() {
        {
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (App.settings.isMinecraftLaunched()) {
                                int ret = JOptionPane.showConfirmDialog(
                                        App.settings.getParent(),
                                        "<html><p align=\"center\">"
                                                + App.settings.getLocalizedString(
                                                        "console.killsure", "<br/><br/>")
                                                + "</p></html>", App.settings
                                                .getLocalizedString("console.kill"),
                                        JOptionPane.YES_OPTION);

                                if (ret == JOptionPane.YES_OPTION) {
                                    App.settings.killMinecraft();
                                }
                            }
                        }
                    });
                }
            });
        }
    };

    private final MenuItem TC_BUTTON = new MenuItem() {
        {
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    App.settings.getConsole().setVisible(!App.settings.getConsole().isVisible());
                }
            });
        }
    };

    private final MenuItem QUIT_BUTTON = new MenuItem() {
        {
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    System.exit(0);
                }
            });
        }
    };

    public void setConsoleVisible(boolean visible) {
        if (visible) {
            if (App.settings.isLanguageLoaded()) {
                this.TC_BUTTON.setLabel(App.settings.getLocalizedString("console.hide"));
            } else {
                this.TC_BUTTON.setLabel("Hide Console");
            }
        } else {
            if (App.settings.isLanguageLoaded()) {
                this.TC_BUTTON.setLabel(App.settings.getLocalizedString("console.show"));
            } else {
                this.TC_BUTTON.setLabel("Show Console");
            }
        }
    }

    public void setMinecraftLaunched(boolean launched) {
        this.KILLMC_BUTTON.setEnabled(launched);
    }

}
