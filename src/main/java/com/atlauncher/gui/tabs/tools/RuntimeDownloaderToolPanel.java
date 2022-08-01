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

import javax.swing.JButton;
import javax.swing.JLabel;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;

@SuppressWarnings("serial")
public class RuntimeDownloaderToolPanel extends AbstractToolPanel {

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

        viewModel.onCanDownloadRuntimeChanged(LAUNCH_BUTTON::setEnabled);
        viewModel.onCanRemoveDownloadChanged(REMOVE_BUTTON::setEnabled);
    }

    private void removeRuntime() {
        viewModel.removeRuntime(
                onFail -> {
                    DialogManager.okDialog().setTitle(GetText.tr("Runtime Downloader"))
                            .setContent(new HTMLBuilder().center()
                                    .text(GetText.tr("An error occurred removing the runtime. Please check the logs."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                },
                onSuccess -> {
                    DialogManager
                            .okDialog().setTitle(GetText.tr("Runtime Downloader")).setContent(new HTMLBuilder().center()
                                    .text(GetText.tr("Downloaded runtimes have been removed.")).build())
                            .setType(DialogManager.INFO).show();
                });
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
                            clear -> dialog.clearDownloadedBytes()));

            dialog.close();
        }));

        dialog.start();

        if (!dialog.getReturnValue()) {
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
    }
}
