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

import java.nio.file.Files;

import javax.swing.JOptionPane;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;

/**
 * @since 2023 / 11 / 18
 */
public class InstanceCloner {
    public static void clone(Instance instance){
        String clonedName = JOptionPane.showInputDialog(App.launcher.getParent(),
            GetText.tr("Enter a new name for this cloned instance."),
            GetText.tr("Cloning Instance"), JOptionPane.INFORMATION_MESSAGE);

        if (clonedName != null && clonedName.length() >= 1
            && InstanceManager.getInstanceByName(clonedName) == null
            && InstanceManager
            .getInstanceBySafeName(clonedName.replaceAll("[^A-Za-z0-9]", "")) == null
            && clonedName.replaceAll("[^A-Za-z0-9]", "").length() >= 1 && !Files.exists(
            FileSystem.INSTANCES.resolve(clonedName.replaceAll("[^A-Za-z0-9]", "")))) {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_clone", instance));

            final String newName = clonedName;
            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Cloning Instance"), 0,
                GetText.tr("Cloning Instance. Please wait..."), null, App.launcher.getParent());
            dialog.addThread(new Thread(() -> {
                InstanceManager.cloneInstance(instance, newName);
                dialog.close();
                App.TOASTER.pop(GetText.tr("Cloned Instance Successfully"));
            }));
            dialog.start();
        } else if (clonedName == null || clonedName.equals("")) {
            LogManager.error("Error Occurred While Cloning Instance! Dialog Closed/Cancelled!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                    .build())
                .setType(DialogManager.ERROR).show();
        } else if (clonedName.replaceAll("[^A-Za-z0-9]", "").length() == 0) {
            LogManager.error("Error Occurred While Cloning Instance! Invalid Name!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                    .build())
                .setType(DialogManager.ERROR).show();
        } else if (Files
            .exists(FileSystem.INSTANCES.resolve(clonedName.replaceAll("[^A-Za-z0-9]", "")))) {
            LogManager.error(
                "Error Occurred While Cloning Instance! Folder Already Exists Rename It And Try Again!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                    .build())
                .setType(DialogManager.ERROR).show();
        } else {
            LogManager.error(
                "Error Occurred While Cloning Instance! Instance With That Name Already Exists!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                    .build())
                .setType(DialogManager.ERROR).show();
        }
    }
}
