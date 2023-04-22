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
package com.atlauncher.gui.card;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.curseforge.CurseForgeAttachment;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.gui.borders.IconTitledBorder;
import com.atlauncher.gui.components.BackgroundImageLabel;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;

public class ModUpdatesChooserCard extends JPanel {
    final private Instance instance;
    final private DisableableMod mod;

    final private CurseForgeProject curseForgeProject;
    final private List<CurseForgeFile> curseForgeVersions;

    final private ModrinthProject modrinthProject;
    final private List<ModrinthVersion> modrinthVersions;

    public ModUpdatesChooserCard(Instance instance, DisableableMod mod, Pair<Object, Object> updateData) {
        super();

        this.instance = instance;
        this.mod = mod;

        if (updateData.left() instanceof CurseForgeProject) {
            this.modrinthProject = null;
            this.modrinthVersions = null;
            this.curseForgeProject = (CurseForgeProject) updateData.left();
            this.curseForgeVersions = (List<CurseForgeFile>) updateData.right();
        } else {
            this.modrinthProject = (ModrinthProject) updateData.left();
            this.modrinthVersions = (List<ModrinthVersion>) updateData.right();
            this.curseForgeProject = null;
            this.curseForgeVersions = null;
        }

        setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10),
                new IconTitledBorder(mod.getNameFromFile(instance), App.THEME.getBoldFont().deriveFont(15f),
                        Utils.getIconImage(App.THEME.getResourcePath("image/modpack-platform",
                                this.curseForgeProject != null ? "curseforge" : "modrinth")))));
        setPreferredSize(new Dimension(300, 250));
        setMinimumSize(new Dimension(300, 250));
        setMaximumSize(new Dimension(300, 250));
        setLayout(new BorderLayout());

        setupComponents();
    }

    private void setupComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;

        // Current Version
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel currentVersionLabel = new JLabel(GetText.tr("Current Version") + ": ");
        currentVersionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        currentVersionLabel.setFont(
                currentVersionLabel.getFont().deriveFont(currentVersionLabel.getFont().getStyle() | Font.BOLD));
        mainPanel.add(currentVersionLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel currentVersion = new JLabel(String.format("%s (%s)", mod.getVersionFromFile(instance), mod.file));
        currentVersion.setToolTipText(currentVersion.getText());
        currentVersion.setBorder(new EmptyBorder(0, 10, 0, 10));
        mainPanel.add(currentVersion, gbc);

        // Updated Version Selector
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel updatedVersionLabel = new JLabel(GetText.tr("Updated Version") + ": ");
        updatedVersionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        updatedVersionLabel.setFont(
                updatedVersionLabel.getFont().deriveFont(updatedVersionLabel.getFont().getStyle() | Font.BOLD));
        mainPanel.add(updatedVersionLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JPanel updatedVersionPanel = new JPanel();
        updatedVersionPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
        updatedVersionPanel.setLayout(new BoxLayout(updatedVersionPanel, BoxLayout.X_AXIS));

        JComboBox<ComboItem<String>> updatedVersionComboBox = new JComboBox<>();
        updatedVersionComboBox.addItem(new ComboItem<String>("347HlKZS", "iris-mc1.19.4-1.6.1.jar"));
        updatedVersionComboBox.addItem(new ComboItem<String>("rKiitlRl", "iris-mc1.19.4-1.6.0.jar"));
        updatedVersionComboBox.addItem(new ComboItem<String>("mVGu4Ze2", "iris-mc1.19.4-1.5.2.jar"));
        updatedVersionPanel.add(updatedVersionComboBox);
        mainPanel.add(updatedVersionPanel, gbc);
        add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        bottomPanel.setLayout(new FlowLayout());

        JButton changelogButton = new JButton(GetText.tr("Changelog"));
        changelogButton.addActionListener((e) -> {
            // OS.openWebBrowser(websiteUrl);
        });
        bottomPanel.add(changelogButton);

        String websiteUrl = getWebsiteUrl();
        JButton websiteButton = new JButton(GetText.tr("Website"));
        websiteButton.setVisible(websiteUrl != null);
        websiteButton.addActionListener((e) -> {
            OS.openWebBrowser(websiteUrl);
        });
        bottomPanel.add(websiteButton);
        add(bottomPanel, BorderLayout.SOUTH);

        String iconUrl = getIconUrl();
        if (iconUrl != null) {
            BackgroundImageLabel iconLabel = new BackgroundImageLabel(iconUrl, 50, 50);
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(iconLabel, BorderLayout.NORTH);
        } else {
            JLabel emptyLabel = new JLabel();
            emptyLabel.setMinimumSize(new Dimension(50, 50));
            emptyLabel.setMaximumSize(new Dimension(50, 50));
            emptyLabel.setPreferredSize(new Dimension(50, 50));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(emptyLabel, BorderLayout.NORTH);
        }
    }

    private String getIconUrl() {
        if (modrinthProject != null) {
            return modrinthProject.iconUrl;
        }

        if (curseForgeProject != null) {
            Optional<CurseForgeAttachment> logo = curseForgeProject.getLogo();

            if (logo.isPresent()) {
                return logo.get().thumbnailUrl;
            }
        }

        return null;
    }

    private String getWebsiteUrl() {
        if (modrinthProject != null) {
            return modrinthProject.getWebsiteUrl();
        }

        if (curseForgeProject != null) {
            return curseForgeProject.getWebsiteUrl();
        }

        return null;
    }
}
