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
package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.annot.Subscribe;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.managers.LogManager;
import com.atlauncher.thread.PasteUpload;
import com.atlauncher.utils.HTMLUtils;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConsoleBottomBar extends BottomBar {

    private final JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));

    private final JButton clearButton = new JButton("Clear");
    private final JButton copyLogButton = new JButton("Copy Log");
    private final JButton uploadLogButton = new JButton("Upload Log");
    private final JButton killMinecraftButton = new JButton("Kill Minecraft");

    public ConsoleBottomBar() {
        this.addActionListeners(); // Setup Action Listeners

        this.leftSide.add(this.clearButton);
        this.leftSide.add(this.copyLogButton);
        this.leftSide.add(this.uploadLogButton);
        this.leftSide.add(this.killMinecraftButton);

        this.killMinecraftButton.setVisible(false);

        this.add(this.leftSide, BorderLayout.WEST);

        EventHandler.EVENT_BUS.subscribe(this);
    }

    /**
     * Sets up the action listeners on the buttons
     */
    private void addActionListeners() {
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.clearConsole();
                LogManager.info("Console Cleared");
            }
        });
        copyLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.TOASTER.pop("Copied Log to clipboard");
                LogManager.info("Copied Log to clipboard");
                StringSelection text = new StringSelection(App.settings.getLog());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(text, null);
            }
        });
        uploadLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String result = App.TASKPOOL.submit(new PasteUpload()).get();
                    if (result.contains(Constants.PASTE_CHECK_URL)) {
                        App.TOASTER.pop("Log uploaded and link copied to clipboard");
                        LogManager.info("Log uploaded and link copied to clipboard: " + result);
                        StringSelection text = new StringSelection(result);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(text, null);
                    } else {
                        App.TOASTER.popError("Log failed to upload!");
                        LogManager.error("Log failed to upload: " + result);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
        });
        killMinecraftButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int ret = JOptionPane.showConfirmDialog(App.settings.getParent(), HTMLUtils.centerParagraph(Language
                        .INSTANCE.localizeWithReplace("console.killsure", "<br/><br/>")), Language.INSTANCE.localize
                        ("console.kill"), JOptionPane.YES_NO_OPTION);
                if (ret == JOptionPane.YES_OPTION) {
                    App.settings.killMinecraft();
                    killMinecraftButton.setVisible(false);
                }
            }
        });
    }

    public void showKillMinecraft() {
        killMinecraftButton.setVisible(true);
    }

    public void hideKillMinecraft() {
        killMinecraftButton.setVisible(false);
    }

    public void setupLanguage() {
        clearButton.setText(Language.INSTANCE.localize("console.clear"));
        copyLogButton.setText(Language.INSTANCE.localize("console.copy"));
        uploadLogButton.setText(Language.INSTANCE.localize("console.upload"));
        killMinecraftButton.setText(Language.INSTANCE.localize("console.kill"));
    }

    @Subscribe
    public void onRelocalization(EventHandler.RelocalizationEvent e) {
        clearButton.setText(Language.INSTANCE.localize("console.clear"));
        copyLogButton.setText(Language.INSTANCE.localize("console.copy"));
        uploadLogButton.setText(Language.INSTANCE.localize("console.upload"));
        killMinecraftButton.setText(Language.INSTANCE.localize("console.kill"));
    }
}
