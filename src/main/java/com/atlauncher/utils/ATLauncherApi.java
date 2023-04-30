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
package com.atlauncher.utils;

import java.awt.Window;
import java.util.concurrent.ExecutionException;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.constants.Constants;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.thread.PasteUpload;

public class ATLauncherApi {

    public static void uploadLog(Window parent, String contents) {
        String result;
        final ProgressDialog<String> dialog = new ProgressDialog<>(GetText.tr("Uploading Logs"), 0,
                GetText.tr("Uploading Logs"), "Aborting Uploading Logs", parent);

        dialog.addThread(new Thread(() -> {
            try {
                dialog.setReturnValue(App.TASKPOOL.submit(new PasteUpload(contents)).get());
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                dialog.setReturnValue(null);
            } catch (ExecutionException ex) {
                LogManager.logStackTrace("Exception while uploading paste", ex);
                dialog.setReturnValue(null);
            }

            dialog.close();
        }));

        dialog.start();
        result = dialog.getReturnValue();

        if (result != null && result.contains(Constants.PASTE_CHECK_URL)) {
            Analytics.sendEvent("UploadLog", "Launcher");
            App.TOASTER.pop("Log uploaded and link copied to clipboard");
            LogManager.info("Log uploaded and link copied to clipboard: " + result);
            OS.copyToClipboard(result);
        } else {
            App.TOASTER.popError("Log failed to upload!");
            LogManager.error("Log failed to upload: " + result);
        }
    }
}
