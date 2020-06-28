/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.LogManager;
import com.atlauncher.Network;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Runtime;
import com.atlauncher.data.Runtimes;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.Download;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;
import org.zeroturnaround.zip.ZipUtil;

import okhttp3.OkHttpClient;

@SuppressWarnings("serial")
public class RuntimeDownloaderToolPanel extends AbstractToolPanel implements ActionListener {
    private final JLabel TITLE_LABEL = new JLabel(GetText.tr("Runtime Downloader"));

    private final JLabel INFO_LABEL = new JLabel(new HTMLBuilder().center().split(60)
            .text(GetText.tr(
                    "Use this to automatically install and use a recommended version of Java to use with ATLauncher."))
            .build());

    public RuntimeDownloaderToolPanel() {
        TITLE_LABEL.setFont(BOLD_FONT);
        TOP_PANEL.add(TITLE_LABEL);
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(this);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        this.checkLaunchButtonEnabled();
    }

    private void checkLaunchButtonEnabled() {
        LAUNCH_BUTTON.setEnabled(!OS.isLinux());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Analytics.sendEvent("RuntimeDownloader", "Run", "Tool");

        final ProgressDialog dialog = new ProgressDialog(GetText.tr("Runtime Downloader"), 3,
                GetText.tr("Downloading. Please Wait!"), "Runtime Downloader Tool Cancelled!");

        dialog.addThread(new Thread(() -> {
            Runtimes runtimes = Download.build().cached()
                    .setUrl(String.format("%s/launcher/json/runtimes.json", Constants.DOWNLOAD_SERVER))
                    .asClass(Runtimes.class);
            dialog.doneTask();

            Runtime runtime = runtimes.getRuntimeForOS();

            if (runtime != null) {
                File runtimeFolder = FileSystem.RUNTIMES.resolve(runtime.version).toFile();
                File releaseFile = new File(runtimeFolder, "release");

                // no need to download/extract
                if (releaseFile.exists()) {
                    dialog.setReturnValue(runtimeFolder.getAbsolutePath());
                }

                if (!runtimeFolder.exists()) {
                    runtimeFolder.mkdirs();
                }

                String url = String.format("%s/%s", Constants.DOWNLOAD_SERVER, runtime.url);
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                File downloadFile = new File(runtimeFolder, fileName);
                File unpackedFile = new File(runtimeFolder, fileName.replace(".xz", ""));

                OkHttpClient httpClient = Network.createProgressClient(dialog);

                com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(url)
                        .hash(runtime.sha1).size(runtime.size).withHttpClient(httpClient)
                        .downloadTo(downloadFile.toPath());

                if (download.needToDownload()) {
                    dialog.setLabel(GetText.tr("Downloading"));
                    dialog.setTotalBytes(runtime.size);

                    try {
                        download.downloadFile();
                    } catch (IOException e1) {
                        LogManager.logStackTrace(e1);
                        dialog.setReturnValue(null);
                    }

                    dialog.clearDownloadedBytes();
                }

                dialog.doneTask();

                dialog.setLabel(GetText.tr("Extracting"));

                try {
                    Utils.unXZFile(downloadFile, unpackedFile);
                } catch (IOException e2) {
                    LogManager.logStackTrace(e2);
                    dialog.setReturnValue(null);
                }

                ZipUtil.unpack(unpackedFile, runtimeFolder);
                Utils.delete(unpackedFile);

                dialog.setReturnValue(runtimeFolder.getAbsolutePath());
            }

            dialog.close();
        }));

        dialog.start();

        if (dialog.getReturnValue() == null) {
            LogManager.error("Runtime downloaded failed to run!");
            DialogManager.okDialog().setTitle(GetText.tr("Runtime Downloader"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("An error occurred downloading the runtime. Please check the logs."))
                            .build())
                    .setType(DialogManager.ERROR).show();
        } else {
            LogManager.info("Runtime downloaded!");

            String path = (String) dialog.getReturnValue();

            App.settings.setJavaPath(path);
            App.settings.saveProperties();

            DialogManager.okDialog().setTitle(GetText.tr("Runtime Downloader"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("The recommended version of Java has been installed and set to be used."))
                            .build())
                    .setType(DialogManager.INFO).show();
        }
    }
}
