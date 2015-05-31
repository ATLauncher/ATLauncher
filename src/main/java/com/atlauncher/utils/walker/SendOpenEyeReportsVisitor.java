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
package com.atlauncher.utils.walker;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public final class SendOpenEyeReportsVisitor extends SimpleFileVisitor<Path> {
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        LogManager.info("OpenEye: Sending pending crash report located at " + file);

        OpenEyeReportResponse response = Utils.sendOpenEyePendingReport(file);

        if (response == null) {
            // Pending report was never sent due to an issue. Won't delete the file in case
            // it's
            // a temporary issue and can be sent again later.
            LogManager.error("OpenEye: Couldn't send pending crash report!");
        } else {
            // OpenEye returned a response to the report, display that to user if needed.
            LogManager.info("OpenEye: Pending crash report sent! URL: " + response.getURL());
            if (response.hasNote()) {
                String[] options = {Language.INSTANCE.localize("common.opencrashreport"), Language.INSTANCE.localize
                        ("common.ok")};
                int ret = JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph(Language
                        .INSTANCE.localizeWithReplace("instance.openeyereport1", "<br/><br/>") +
                                response.getNoteDisplay() + Language.INSTANCE.localize("instance" + "" +
                                ".openeyereport2")), Language.INSTANCE.localize("instance.aboutyourcrash"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[1]);
                if (ret == 0) {
                    Utils.openBrowser(response.getURL());
                }
            }
        }

        FileUtils.delete(file); // Delete the pending report since we've sent it

        return FileVisitResult.CONTINUE;
    }
}