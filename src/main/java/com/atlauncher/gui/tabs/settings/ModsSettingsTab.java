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
package com.atlauncher.gui.tabs.settings;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.InstanceExportFormat;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.ComboItem;

@SuppressWarnings("serial")
public class ModsSettingsTab extends AbstractSettingsTab {
    private final JComboBox<ComboItem<ModPlatform>> defaultModPlatform;
    private final JComboBox<ComboItem<AddModRestriction>> addModRestriction;
    private final JCheckBox enableAddedModsByDefault;
    private final JCheckBox showFabricModsWhenSinytraInstalled;
    private final JCheckBox allowCurseForgeAlphaBetaFiles;
    private final JCheckBox dontCheckModsOnCurseForge;
    private final JCheckBox dontCheckModsOnModrinth;
    private final JComboBox<ComboItem<InstanceExportFormat>> defaultExportFormat;

    public ModsSettingsTab() {
        // Default mod platform

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover defaultModPlatformLabel = new JLabelWithHover(GetText.tr("Default Mod Platform") + ":",
                HELP_ICON, GetText.tr(
                        "The default mod platform to use when adding mods to instances, as well as the platform to use when updating/reinstalling mods on multiple platforms."));

        add(defaultModPlatformLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        defaultModPlatform = new JComboBox<>();
        defaultModPlatform.addItem(new ComboItem<>(ModPlatform.CURSEFORGE, "CurseForge"));
        defaultModPlatform.addItem(new ComboItem<>(ModPlatform.MODRINTH, "Modrinth"));

        for (int i = 0; i < defaultModPlatform.getItemCount(); i++) {
            ComboItem<ModPlatform> item = defaultModPlatform.getItemAt(i);

            if (item.getValue() == App.settings.defaultModPlatform) {
                defaultModPlatform.setSelectedIndex(i);
                break;
            }
        }

        add(defaultModPlatform, gbc);

        // Add Mod Restrictions

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover addModRestrictionsLabel = new JLabelWithHover(GetText.tr("Add Mod Restrictions") + ":",
                HELP_ICON, GetText.tr("What restrictions should be in place when adding mods from a mod platform."));

        add(addModRestrictionsLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        addModRestriction = new JComboBox<>();
        addModRestriction.addItem(
                new ComboItem<>(AddModRestriction.STRICT, GetText.tr("Only show mods for current Minecraft version")));
        addModRestriction.addItem(new ComboItem<>(AddModRestriction.LAX,
                GetText.tr("Show mods for the current major Minecraft version (eg: 1.16.x)")));
        addModRestriction
                .addItem(new ComboItem<>(AddModRestriction.NONE, GetText.tr("Show mods for all Minecraft versions")));

        for (int i = 0; i < addModRestriction.getItemCount(); i++) {
            ComboItem<AddModRestriction> item = addModRestriction.getItemAt(i);

            if (item.getValue() == App.settings.addModRestriction) {
                addModRestriction.setSelectedIndex(i);
                break;
            }
        }

        add(addModRestriction, gbc);

        // Enable added mods by default

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableAddedModsByDefaultLabel = new JLabelWithHover(GetText.tr("Enable Added Mods By Default?"),
                HELP_ICON, new HTMLBuilder().center().split(100)
                        .text(GetText.tr("When adding mods manually, should they be enabled automatically?")).build());
        add(enableAddedModsByDefaultLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableAddedModsByDefault = new JCheckBox();
        enableAddedModsByDefault.setSelected(App.settings.enableAddedModsByDefault);
        add(enableAddedModsByDefault, gbc);

        // Show Fabric Mods When Sinytra Installed

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover showFabricModsWhenSinytraInstalledLabel = new JLabelWithHover(GetText.tr("Show Fabric Mods When Sinytra Installed?"),
                HELP_ICON, new HTMLBuilder().center().split(100)
                        .text(GetText.tr("When Sinytra Connector is installed, should Fabric mods be shown?")).build());
        add(showFabricModsWhenSinytraInstalledLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        showFabricModsWhenSinytraInstalled = new JCheckBox();
        showFabricModsWhenSinytraInstalled.setSelected(App.settings.showFabricModsWhenSinytraInstalled);
        add(showFabricModsWhenSinytraInstalled, gbc);

        // Allow CurseForge Alpha/Beta CurseForge files

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover allowCurseForgeAlphaBetaFilesLabel = new JLabelWithHover(
                GetText.tr("Allow CurseForge Alpha/Beta Files?"),
                HELP_ICON, new HTMLBuilder().center().split(100)
                        .text(GetText.tr(
                                "This will enable using Alpha/Beta files from CurseForge by default when installing modpacks as well as updating to Alpha/Beta versions from stable release versions."))
                        .build());
        add(allowCurseForgeAlphaBetaFilesLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        allowCurseForgeAlphaBetaFiles = new JCheckBox();
        allowCurseForgeAlphaBetaFiles.setSelected(App.settings.allowCurseForgeAlphaBetaFiles);
        add(allowCurseForgeAlphaBetaFiles, gbc);

        // Dont check mods on CurseForge

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover dontCheckModsOnCurseForgeLabel = new JLabelWithHover(
                // #. {0} is the platform (e.g. CurseForge/Modrinth)
                GetText.tr("Don't Check Mods On {0}?", "CurseForge"), HELP_ICON,
                // #. {0} is the platform (e.g. CurseForge/Modrinth)
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "When installing packs or adding mods manually to instances, we check for the file on {0} to show more information about the mod as well as make updating easier. Disabling this will mean you won't be able to update manually added mods from within the launcher.",
                        "CurseForge"))
                        .build());
        add(dontCheckModsOnCurseForgeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        dontCheckModsOnCurseForge = new JCheckBox();
        dontCheckModsOnCurseForge.setSelected(App.settings.dontCheckModsOnCurseForge);
        add(dontCheckModsOnCurseForge, gbc);

        // Dont check mods on Modrinth

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover dontCheckModsOnModrinthLabel = new JLabelWithHover(
                // #. {0} is the platform (e.g. CurseForge/Modrinth)
                GetText.tr("Don't Check Mods On {0}?", "Modrinth"), HELP_ICON,
                // #. {0} is the platform (e.g. CurseForge/Modrinth)
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "When installing packs or adding mods manually to instances, we check for the file on {0} to show more information about the mod as well as make updating easier. Disabling this will mean you won't be able to update manually added mods from within the launcher.",
                        "Modrinth"))
                        .build());
        add(dontCheckModsOnModrinthLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        dontCheckModsOnModrinth = new JCheckBox();
        dontCheckModsOnModrinth.setSelected(App.settings.dontCheckModsOnModrinth);
        add(dontCheckModsOnModrinth, gbc);

        // Default export format

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover defaultExportFormatLabel = new JLabelWithHover(GetText.tr("Default Export Format") + ":",
                HELP_ICON, GetText.tr(
                        "The default format to export instances to. Can also be changed at time of export."));

        add(defaultExportFormatLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        defaultExportFormat = new JComboBox<>();
        defaultExportFormat.addItem(new ComboItem<>(InstanceExportFormat.CURSEFORGE, "CurseForge"));
        defaultExportFormat.addItem(new ComboItem<>(InstanceExportFormat.MODRINTH, "Modrinth"));
        defaultExportFormat
                .addItem(new ComboItem<>(InstanceExportFormat.CURSEFORGE_AND_MODRINTH, "CurseForge & Modrinth"));
        defaultExportFormat.addItem(new ComboItem<>(InstanceExportFormat.MULTIMC, "MultiMC"));

        for (int i = 0; i < defaultExportFormat.getItemCount(); i++) {
            ComboItem<InstanceExportFormat> item = defaultExportFormat.getItemAt(i);

            if (item.getValue() == App.settings.defaultExportFormat) {
                defaultExportFormat.setSelectedIndex(i);
                break;
            }
        }

        add(defaultExportFormat, gbc);
    }

    public boolean needToCheckForExternalPackUpdates() {
        return App.settings.allowCurseForgeAlphaBetaFiles != allowCurseForgeAlphaBetaFiles.isSelected();
    }

    @SuppressWarnings("unchecked")
    public void save() {
        App.settings.defaultModPlatform = ((ComboItem<ModPlatform>) defaultModPlatform.getSelectedItem()).getValue();
        App.settings.addModRestriction = ((ComboItem<AddModRestriction>) addModRestriction.getSelectedItem())
                .getValue();
        App.settings.enableAddedModsByDefault = enableAddedModsByDefault.isSelected();
        App.settings.showFabricModsWhenSinytraInstalled = showFabricModsWhenSinytraInstalled.isSelected();
        App.settings.allowCurseForgeAlphaBetaFiles = allowCurseForgeAlphaBetaFiles.isSelected();
        App.settings.dontCheckModsOnCurseForge = dontCheckModsOnCurseForge.isSelected();
        App.settings.dontCheckModsOnModrinth = dontCheckModsOnModrinth.isSelected();
        App.settings.defaultExportFormat = ((ComboItem<InstanceExportFormat>) defaultExportFormat.getSelectedItem())
                .getValue();
    }

    @Override
    public String getTitle() {
        return GetText.tr("Mods");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Mods";
    }
}
