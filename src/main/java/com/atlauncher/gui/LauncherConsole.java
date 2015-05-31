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
import com.atlauncher.data.Constants;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.Console;
import com.atlauncher.gui.components.ConsoleBottomBar;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Utils;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LauncherConsole extends JFrame implements RelocalizationListener {

    private static final long serialVersionUID = -3538990021922025818L;
    public Console console;
    private JScrollPane scrollPane;
    private ConsoleBottomBar bottomBar;
    private JPopupMenu contextMenu; // Right click menu

    private JMenuItem copy;

    public LauncherConsole() {
        this.setTitle(Constants.LAUNCHER_NAME + " Console " + Constants.VERSION);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.setIconImage(Utils.getImage("/assets/image/Icon.png"));
        this.setMinimumSize(new Dimension(600, 400));
        this.setLayout(new BorderLayout());

        console = new Console();
        console.setFont(App.THEME.getConsoleFont().deriveFont(Utils.getBaseFontSize()));
        console.setForeground(App.THEME.getConsoleTextColor());
        console.setSelectionColor(App.THEME.getSelectionColor());

        setupContextMenu(); // Setup the right click menu

        bottomBar = new ConsoleBottomBar();

        scrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
        RelocalizationManager.addListener(this);
    }

    @Override
    public void setVisible(boolean flag) {
        super.setVisible(flag);
        if (flag) {
            ConsoleOpenManager.post();
        } else {
            ConsoleCloseManager.post();
        }
    }

    private void setupContextMenu() {
        contextMenu = new JPopupMenu();

        copy = new JMenuItem("Copy");
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringSelection text = new StringSelection(console.getSelectedText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(text, null);
            }
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
        LogManager.debug("Setting up language for console");
        copy.setText(Language.INSTANCE.localize("common.copy"));
        bottomBar.setupLanguage();
        LogManager.debug("Finished setting up language for console");
    }

    public void clearConsole() {
        console.setText(null);
    }

    @Override
    public void onRelocalization() {
        copy.setText(Language.INSTANCE.localize("common.copy"));
        bottomBar.setupLanguage();
    }
}