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

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Instance;
import com.atlauncher.data.installables.Installable;
import com.atlauncher.data.installables.VanillaInstallable;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.fabric.FabricLoader;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader;
import com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader;
import com.atlauncher.data.minecraft.loaders.neoforge.NeoForgeLoader;
import com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ComboItem;
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

    public static LoaderVersion showLoaderVersionSelector(Instance instance) {
        LoaderType loaderType = instance.launcher.loaderVersion.getLoaderType();

        ProgressDialog<List<LoaderVersion>> progressDialog = new ProgressDialog<>(
            // #. {0} is the loader (Forge/Fabric/Quilt)
            GetText.tr("Checking For {0} Versions", loaderType), 0,
            // #. {0} is the loader (Forge/Fabric/Quilt)
            GetText.tr("Checking For {0} Versions", loaderType));
        progressDialog.addThread(new Thread(() -> {
            if (loaderType == LoaderType.FABRIC) {
                progressDialog.setReturnValue(FabricLoader.getChoosableVersions(instance.id));
            } else if (loaderType == LoaderType.FORGE) {
                progressDialog.setReturnValue(ForgeLoader.getChoosableVersions(instance.id));
            } else if (loaderType == LoaderType.LEGACY_FABRIC) {
                progressDialog.setReturnValue(LegacyFabricLoader.getChoosableVersions(instance.id));
            } else if (loaderType == LoaderType.NEOFORGE) {
                progressDialog.setReturnValue(NeoForgeLoader.getChoosableVersions(instance.id));
            } else if (loaderType == LoaderType.QUILT) {
                progressDialog.setReturnValue(QuiltLoader.getChoosableVersions(instance.id));
            }

            progressDialog.doneTask();
            progressDialog.close();
        }));
        progressDialog.start();

        List<LoaderVersion> loaderVersions = progressDialog.getReturnValue();

        if (loaderVersions == null || loaderVersions.size() == 0) {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("No Versions Available For {0}", loaderType))
                .setContent(new HTMLBuilder().center()
                    // #. {0} is the loader (Forge/Fabric/Quilt)
                    .text(GetText.tr("{0} has not been installed/updated as there are no versions available.",
                        loaderType))
                    .build())
                .setType(DialogManager.ERROR).show();
            return null;
        }

        JComboBox<ComboItem<LoaderVersion>> loaderVersionsDropDown = new JComboBox<>();

        int loaderVersionLength = 0;

        // ensures that font width is taken into account
        for (LoaderVersion version : loaderVersions) {
            loaderVersionLength = Math.max(loaderVersionLength, loaderVersionsDropDown
                .getFontMetrics(App.THEME.getNormalFont()).stringWidth(version.toStringWithCurrent(instance)) + 25);
        }

        loaderVersions.forEach(version -> loaderVersionsDropDown
            .addItem(new ComboItem<LoaderVersion>(version, version.toStringWithCurrent(instance))));

        if (loaderType == LoaderType.FORGE) {
            Optional<LoaderVersion> recommendedVersion = loaderVersions.stream().filter(lv -> lv.recommended)
                .findFirst();

            if (recommendedVersion.isPresent()) {
                loaderVersionsDropDown.setSelectedIndex(loaderVersions.indexOf(recommendedVersion.get()));
            }
        }

        if (instance.launcher.loaderVersion != null) {
            String loaderVersionString = instance.launcher.loaderVersion.version;

            for (int i = 0; i < loaderVersionsDropDown.getItemCount(); i++) {
                LoaderVersion loaderVersion = ((ComboItem<LoaderVersion>) loaderVersionsDropDown.getItemAt(i))
                    .getValue();

                if (loaderVersion.version.equals(loaderVersionString)) {
                    loaderVersionsDropDown.setSelectedIndex(i);
                    break;
                }
            }
        }

        // ensures that the dropdown is at least 200 px wide
        loaderVersionLength = Math.max(200, loaderVersionLength);

        // ensures that there is a maximum width of 400 px to prevent overflow
        loaderVersionLength = Math.min(400, loaderVersionLength);

        loaderVersionsDropDown.setPreferredSize(new Dimension(loaderVersionLength, 23));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Box box = Box.createHorizontalBox();
        // #. {0} is the loader (Forge/Fabric/Quilt)
        box.add(new JLabel(GetText.tr("Select {0} Version To Install", loaderType)));
        box.add(Box.createHorizontalGlue());

        panel.add(box);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loaderVersionsDropDown);
        panel.add(Box.createVerticalStrut(20));

        int ret = JOptionPane.showConfirmDialog(App.launcher.getParent(), panel,
            // #. {0} is the loader (Forge/Fabric/Quilt)
            instance.launcher.loaderVersion == null ? GetText.tr("Installing {0}", loaderType)
                // #. {0} is the loader (Forge/Fabric/Quilt)
                : GetText.tr("Changing {0} Version", loaderType),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (ret != 0) {
            return null;
        }

        return ((ComboItem<LoaderVersion>) loaderVersionsDropDown.getSelectedItem()).getValue();
    }

    public static void changeLoaderVersion(Instance instance){
        Analytics.trackEvent(
            AnalyticsEvent.forInstanceLoaderEvent("instance_change_loader_version", instance, instance.launcher.loaderVersion));

        LoaderVersion loaderVersion = showLoaderVersionSelector(instance);

        if (loaderVersion == null) {
            return;
        }

        boolean success = false;

        try {
            Installable installable = new VanillaInstallable(MinecraftManager.getMinecraftVersion(instance.id), loaderVersion,
                instance.launcher.description);
            installable.instance = instance;
            installable.instanceName = instance.launcher.name;
            installable.isReinstall = true;
            installable.changingLoader = true;
            installable.isServer = false;
            installable.saveMods = true;

            success = installable.startInstall();
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }

        if (success) {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Installed", instance.launcher.loaderVersion.getLoaderType()))
                .setContent(
                    new HTMLBuilder().center()
                        // #. {0} is the loader (Forge/Fabric/Quilt) {1} is the version
                        .text(GetText.tr("{0} {1} has been installed.",
                            instance.launcher.loaderVersion.getLoaderType(), loaderVersion.version))
                        .build())
                .setType(DialogManager.INFO).show();
        } else {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Not Installed", instance.launcher.loaderVersion.getLoaderType()))
                .setContent(new HTMLBuilder().center()
                    // #. {0} is the loader (Forge/Fabric/Quilt)
                    .text(GetText.tr("{0} has not been installed. Check the console for more information.",
                        instance.launcher.loaderVersion.getLoaderType()))
                    .build())
                .setType(DialogManager.ERROR).show();
        }
    }
}
