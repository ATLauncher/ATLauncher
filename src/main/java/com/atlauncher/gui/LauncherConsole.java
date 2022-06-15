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
import com.atlauncher.data.Settings;
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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Singleton
public final class LauncherConsole extends JFrame {
    private static final Logger LOG = LogManager.getLogger(LauncherConsole.class);
    private static final long serialVersionUID = -3538990021922025818L;
    private final Console console;
    private final ConsoleBottomBar bottomBar;
    private final boolean remember;
    private JPopupMenu contextMenu; // Right click menu

    private JMenuItem copy;

    @Inject
    private LauncherConsole(final Settings settings,
                            final Console console,
                            final ConsoleBottomBar bottomBar) {
        this.console = console;
        this.bottomBar = bottomBar;

        setTitle(Constants.LAUNCHER_NAME + " Console");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setIconImage(Utils.getImage("/assets/image/icon.png"));
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(650, 400));

        this.remember = settings.rememberWindowSizePosition;
        if(this.remember)
            this.setCustomBounds(settings.consolePosition, settings.consoleSize);

        setupContextMenu(); // Setup the right click menu

        JScrollPane scrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        AppEventBus.register(this);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component) evt.getSource();
                if (remember) { //TODO: update settings
                    App.settings.consoleSize = c.getSize();
                    App.settings.save();
                }
            }

            public void componentMoved(ComponentEvent evt) {
                Component c = (Component) evt.getSource();
                if (remember) { //TODO: update settings
                    App.settings.consolePosition = c.getLocation();
                    App.settings.save();
                }
            }
        });
    }

    public Console getConsole(){
        return this.console;
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

    private void setCustomBounds(@Nullable final Point pos,
                                 @Nullable final Dimension size){
        if (size != null && pos != null)
            setBounds(pos.x, pos.y, size.width, size.height);
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
