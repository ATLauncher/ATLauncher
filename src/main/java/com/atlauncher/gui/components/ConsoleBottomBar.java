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
package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.events.LocalizationEvent;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.thread.PasteUpload;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("serial")
public class ConsoleBottomBar extends BottomBar {
    private static final Logger LOG = LogManager.getLogger(ConsoleBottomBar.class);

    private final JButton clearButton = new JButton(GetText.tr("Clear"));
    private final JButton copyLogButton = new JButton(GetText.tr("Copy Log"));
    private final JButton uploadLogButton = new JButton(GetText.tr("Upload Log"));
    private final JButton killMinecraftButton = new JButton(GetText.tr("Kill Minecraft"));

    public ConsoleBottomBar() {
        this.addActionListeners(); // Setup Action Listeners

        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 13));
        leftSide.add(this.clearButton);
        leftSide.add(this.copyLogButton);
        leftSide.add(this.uploadLogButton);
        leftSide.add(this.killMinecraftButton);

        this.killMinecraftButton.setVisible(false);

        this.add(leftSide, BorderLayout.WEST);

        AppEventBus.register(this);
    }

    /**
     * Sets up the action listeners on the buttons
     */
    private void addActionListeners() {
        clearButton.addActionListener(e -> {
            App.console.clearConsole();
            LOG.info("Console Cleared");
        });
        copyLogButton.addActionListener(e -> {
            //TODO: Analytics.sendEvent("CopyLog", "Launcher");
            App.TOASTER.pop("Copied Log to clipboard");
            LOG.info("Copied Log to clipboard");
            StringSelection text = new StringSelection(App.console.getLog());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(text, null);
        });
        uploadLogButton.addActionListener(e -> {
            String result;
            final ProgressDialog<String> dialog = new ProgressDialog<>(GetText.tr("Uploading Logs"), 0,
                GetText.tr("Uploading Logs"), "Aborting Uploading Logs", App.console);

            dialog.addThread(new Thread(() -> {
                try {
                    dialog.setReturnValue(App.TASKPOOL.submit(new PasteUpload()).get());
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    dialog.setReturnValue(null);
                } catch (ExecutionException ex) {
                    LOG.error("Exception while uploading paste", ex);
                    dialog.setReturnValue(null);
                }

                dialog.close();
            }));

            dialog.start();
            result = dialog.getReturnValue();

            if (result != null && result.contains(Constants.PASTE_CHECK_URL)) {
                //TODO: Analytics.sendEvent("UploadLog", "Launcher");
                App.TOASTER.pop("Log uploaded and link copied to clipboard");
                LOG.info("Log uploaded and link copied to clipboard: {}", result);
                StringSelection text = new StringSelection(result);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(text, null);
            } else {
                App.TOASTER.popError("Log failed to upload!");
                LOG.error("Log failed to upload: {}", result);
            }
        });
        killMinecraftButton.addActionListener(arg0 -> {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Kill Minecraft") + "?")
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "Are you sure you want to kill the Minecraft process?<br/><br/>Doing so can cause corruption of your saves."))
                    .build())
                .setType(DialogManager.QUESTION).show();
            if (ret == DialogManager.YES_OPTION) {
                //TODO: Analytics.sendEvent("KillMinecraft", "Launcher");
                App.launcher.killMinecraft();
                killMinecraftButton.setVisible(false);
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
        clearButton.setText(GetText.tr("Clear"));
        copyLogButton.setText(GetText.tr("Copy Log"));
        uploadLogButton.setText(GetText.tr("Upload Log"));
        killMinecraftButton.setText(GetText.tr("Kill Minecraft"));
    }

    @Subscribe
    public final void onLocalizationChanged(final LocalizationEvent.LocalizationChangedEvent event) {
        this.setupLanguage();
    }
}
