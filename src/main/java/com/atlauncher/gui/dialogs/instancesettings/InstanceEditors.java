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
package com.atlauncher.gui.dialogs.instancesettings;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.Utils;

/**
 * @since 2023 / 11 / 18
 */
public class InstanceEditors {

    public static void startChangeDescription(Instance instance){
        JTextArea textArea = new JTextArea(instance.launcher.description);
        textArea.setColumns(30);
        textArea.setRows(10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(300, 150);

        int ret = JOptionPane.showConfirmDialog(App.launcher.getParent(), new JScrollPane(textArea),
            GetText.tr("Changing Description"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (ret == 0) {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_description_change", instance));
            instance.launcher.description = textArea.getText();
            InstanceManager.saveInstance(instance);
        }
    }

    public static void startChangeImage(Instance instance) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
        int ret = chooser.showOpenDialog(App.launcher.getParent());
        if (ret == JFileChooser.APPROVE_OPTION) {
            File img = chooser.getSelectedFile();
            if (img.getAbsolutePath().endsWith(".png")) {
                Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_image_change", instance));
                try {
                    Utils.safeCopy(img, instance.getRoot().resolve("instance.png").toFile());
                    InstanceManager.saveInstance(instance);
                } catch (IOException ex) {
                    LogManager.logStackTrace("Failed to set instance image", ex);
                }
            }
        }
    }
}
