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

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.constants.Constants;
import com.atlauncher.events.OnSide;
import com.atlauncher.events.Side;
import com.atlauncher.events.console.ConsoleClosedEvent;
import com.atlauncher.events.console.ConsoleEvent;
import com.atlauncher.events.console.ConsoleOpenedEvent;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.atlauncher.gui.components.Console;
import com.atlauncher.gui.components.ConsoleBottomBar;
import com.atlauncher.utils.Utils;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LauncherConsole extends JFrame {
    private static final Logger LOG = LogManager.getLogger(LauncherConsole.class);
    private static final long serialVersionUID = -3538990021922025818L;
    public Console console;
    private final ConsoleBottomBar bottomBar;
    private JPopupMenu contextMenu; // Right click menu

    private JMenuItem copy;

    @Inject
    public LauncherConsole() {
        setTitle(Constants.LAUNCHER_NAME + " Console");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setIconImage(Utils.getImage("/assets/image/icon.png"));
        setLayout(new BorderLayout());

        setMinimumSize(new Dimension(650, 400));

        try {
            if (App.settings.rememberWindowSizePosition && App.settings.consoleSize != null
                && App.settings.consolePosition != null) {
                setBounds(App.settings.consolePosition.x, App.settings.consolePosition.y,
                    App.settings.consoleSize.width, App.settings.consoleSize.height);
            }
        } catch (Exception e) {
            LOG.error("Error setting custom remembered window size settings", e);
        }

        console = new Console();

        setupContextMenu(); // Setup the right click menu

        bottomBar = new ConsoleBottomBar();

        JScrollPane scrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        AppEventBus.register(this);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component) evt.getSource();

                if (App.settings.rememberWindowSizePosition) {
                    App.settings.consoleSize = c.getSize();
                    App.settings.save();
                }
            }

            public void componentMoved(ComponentEvent evt) {
                Component c = (Component) evt.getSource();

                if (App.settings.rememberWindowSizePosition) {
                    App.settings.consolePosition = c.getLocation();
                    App.settings.save();
                }
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            this.postConsoleOpenEvent();
        } else {
            this.postConsoleClosedEvent();
        }
    }

    private void postConsoleOpenEvent() {
        AppEventBus.post(ConsoleOpenedEvent.newInstance());
    }

    private void postConsoleClosedEvent() {
        AppEventBus.post(ConsoleClosedEvent.newInstance());
    }

    private void setupContextMenu() {
        contextMenu = new JPopupMenu();

        copy = new JMenuItem(GetText.tr("Copy"));
        copy.addActionListener(e -> {
            StringSelection text = new StringSelection(console.getSelectedText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(text, null);
        });
        contextMenu.add(copy);

        console.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (console.getSelectedText() != null) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        contextMenu.show(console, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    /**
     * Returns a string with the text currently in the console
     *
     * @return String Console Text
     */
    public String getLog() {
        return console.getText();
    }

    public void showKillMinecraft() {
        bottomBar.showKillMinecraft();
    }

    public void hideKillMinecraft() {
        bottomBar.hideKillMinecraft();
    }

    public void setupLanguage() {
        LOG.debug("Setting up language for console");
        copy.setText(GetText.tr("Copy"));
        bottomBar.setupLanguage();
        LOG.debug("Finished setting up language for console");
    }

    public void clearConsole() {
        console.setText(null);
    }

    @Subscribe
    @OnSide(Side.UI)
    public final void onLocalizationChanged(final LocalizationChangedEvent event) {
        this.copy.setText(GetText.tr("Copy"));
        this.bottomBar.setupLanguage();
    }
}
