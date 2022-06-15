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
package com.atlauncher.gui.tabs.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Network;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.Runtime;
import com.atlauncher.data.Runtimes;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.Download;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

import okhttp3.OkHttpClient;

@SuppressWarnings("serial")
public class RuntimeDownloaderToolPanel extends AbstractToolPanel {
    private static final Logger LOG = LogManager.getLogger(RuntimeDownloaderToolPanel.class);

    protected final JButton REMOVE_BUTTON = new JButton(GetText.tr("Remove"));
    private final IToolsViewModel viewModel;

    public RuntimeDownloaderToolPanel(IToolsViewModel viewModel) {
        super(GetText.tr("Runtime Downloader"));
        this.viewModel = viewModel;

        JLabel INFO_LABEL = new JLabel(new HTMLBuilder().center().split(70).text(GetText
                .tr("Use this to automatically install and use a recommended version of Java to use with ATLauncher."))
            .build());
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(e -> downloadRuntime());

        BOTTOM_PANEL.add(REMOVE_BUTTON);
        REMOVE_BUTTON.addActionListener(e -> removeRuntime());
        REMOVE_BUTTON.setFont(App.THEME.getNormalFont().deriveFont(16f));

        setButtonEnabledStates();
    }

    private void setButtonEnabledStates() {
        LAUNCH_BUTTON.setEnabled(!OS.isLinux());
        REMOVE_BUTTON.setEnabled(!OS.isLinux() && Java.hasInstalledRuntime());
    }

    private void removeRuntime() {
        viewModel.removeRuntime(
            onFail -> {
                DialogManager.okDialog().setTitle(GetText.tr("Runtime Downloader"))
                    .setContent(new HTMLBuilder().center()
                        .text(GetText.tr("An error occurred removing the runtime. Please check the logs.")).build())
                    .setType(DialogManager.ERROR).show();
            },
            onSuccess -> {
                DialogManager
                    .okDialog().setTitle(GetText.tr("Runtime Downloader")).setContent(new HTMLBuilder().center()
                        .text(GetText.tr("Downloaded runtimes have been removed.")).build())
                    .setType(DialogManager.INFO).show();
            }
        );
        setButtonEnabledStates();
    }

    private void downloadRuntime() {
        final ProgressDialog<Boolean> dialog = new ProgressDialog<>(GetText.tr("Runtime Downloader"), 3,
            GetText.tr("Downloading. Please Wait!"), "Runtime Downloader Tool Cancelled!");

        dialog.addThread(new Thread(() -> {
            dialog.setReturnValue(
                viewModel.downloadRuntime(
                    dialog,
                    taskComplete -> dialog.doneTask(),
                    dialog::setLabel,
                    clear -> dialog.clearDownloadedBytes()
                )
            );

            dialog.close();
        }));

        dialog.start();

        if (dialog.getReturnValue()) {
            DialogManager.okDialog().setTitle(GetText.tr("Runtime Downloader"))
                .setContent(new HTMLBuilder().center()
                    .text(GetText.tr("An error occurred downloading the runtime. Please check the logs."))
                    .build())
                .setType(DialogManager.ERROR).show();
        } else {
            DialogManager.okDialog().setTitle(GetText.tr("Runtime Downloader"))
                .setContent(new HTMLBuilder().center()
                    .text(GetText.tr("The recommended version of Java has been installed and set to be used."))
                    .build())
                .setType(DialogManager.INFO).show();
        }
        setButtonEnabledStates();
    }
}
