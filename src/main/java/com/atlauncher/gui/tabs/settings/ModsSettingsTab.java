/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.ComboItem;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class ModsSettingsTab extends AbstractSettingsTab {
    private final JComboBox<ComboItem<ModPlatform>> defaultModPlatform;
    private final JComboBox<ComboItem<AddModRestriction>> addModRestriction;
    private final JCheckBox enableAddedModsByDefault;
    private final JCheckBox dontCheckModsOnCurseForge;

    public ModsSettingsTab() {
        // Default mod platform

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover defaultModPlatformLabel = new JLabelWithHover(GetText.tr("Default Mod Platform") + ":",
                HELP_ICON, GetText.tr("The default mod platform to use when adding mods to instances."));

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

        // Dont check mods on CurseForge

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover dontCheckModsOnCurseForgeLabel = new JLabelWithHover(
                GetText.tr("Don't Check Mods On CurseForge?"), HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "When installing packs or adding mods manually to instances, we check for the file on CurseForge to show more information about the mod as well as make updating easier. Disbaling this will mean you won't be able to update manually added mods from within the launcher but may solve some issues installing packs due to running out of memory."))
                        .build());
        add(dontCheckModsOnCurseForgeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        dontCheckModsOnCurseForge = new JCheckBox();
        dontCheckModsOnCurseForge.setSelected(App.settings.dontCheckModsOnCurseForge);
        add(dontCheckModsOnCurseForge, gbc);
    }

    @SuppressWarnings("unchecked")
    public void save() {
        App.settings.defaultModPlatform = ((ComboItem<ModPlatform>) defaultModPlatform.getSelectedItem()).getValue();
        App.settings.addModRestriction = ((ComboItem<AddModRestriction>) addModRestriction.getSelectedItem())
                .getValue();
        App.settings.enableAddedModsByDefault = enableAddedModsByDefault.isSelected();
        App.settings.dontCheckModsOnCurseForge = dontCheckModsOnCurseForge.isSelected();
    }

    @Override
    public String getTitle() {
        return GetText.tr("Mods");
    }
}
