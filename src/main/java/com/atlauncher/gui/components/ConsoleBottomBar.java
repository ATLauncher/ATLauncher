/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.thread.PasteUpload;

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

public class ConsoleBottomBar extends BottomBar implements RelocalizationListener {

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

        RelocalizationManager.addListener(this);
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
                int ret = JOptionPane.showConfirmDialog(App.settings.getParent(), "<html><p align=\"center\">" + App
                        .settings.getLocalizedString("console.killsure", "<br/><br/>") + "</p></html>",
                        Language.INSTANCE.localize("console.kill"), JOptionPane.YES_NO_OPTION);
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

    @Override
    public void onRelocalization() {
        clearButton.setText(Language.INSTANCE.localize("console.clear"));
        copyLogButton.setText(Language.INSTANCE.localize("console.copy"));
        uploadLogButton.setText(Language.INSTANCE.localize("console.upload"));
        killMinecraftButton.setText(Language.INSTANCE.localize("console.kill"));
    }
}
