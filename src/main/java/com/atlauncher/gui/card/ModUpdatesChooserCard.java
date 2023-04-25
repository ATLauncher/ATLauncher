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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
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
import com.atlauncher.gui.dialogs.HtmlDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.Markdown;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;

public class ModUpdatesChooserCard extends JPanel {
    final private Window parentWindow;
    public final DisableableMod mod;
    final private ModUpdateCheckBoxCheckedCallback onCheckBoxChecked;
    private final boolean reinstall;

    final private CurseForgeProject curseForgeProject;
    final private List<CurseForgeFile> curseForgeVersions;

    final private ModrinthProject modrinthProject;
    final private List<ModrinthVersion> modrinthVersions;
    private JButton changelogButton = new JButton(GetText.tr("Changelog"));
    private JComboBox<ComboItem<Object>> updatedVersionComboBox = new JComboBox<>();
    private JCheckBox updateCheckBox = new JCheckBox();

    public ModUpdatesChooserCard(Window parentWindow, Instance instance, DisableableMod mod,
            Pair<Object, Object> updateData, boolean reinstall, ModUpdateCheckBoxCheckedCallback onCheckBoxChecked) {
        super();

        this.parentWindow = parentWindow;
        this.mod = mod;
        this.reinstall = reinstall;
        this.onCheckBoxChecked = onCheckBoxChecked;

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

        setOpaque(true);
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
        JPanel topPanel = new JPanel();
        topPanel.setLayout(null);

        updateCheckBox.setText(reinstall ? GetText.tr("Install?") : GetText.tr("Update?"));
        updateCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
        updateCheckBox.setHorizontalAlignment(SwingConstants.RIGHT);
        updateCheckBox.setSelected(true);
        updateCheckBox.setBounds(200, 0, 70, 21);
        updateCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCheckBoxChecked.checkBoxStateChanged(updateCheckBox.isSelected());
            }
        });

        topPanel.setPreferredSize(new Dimension(50, 50));

        topPanel.add(updateCheckBox);

        String iconUrl = getIconUrl();
        if (iconUrl != null) {
            BackgroundImageLabel iconLabel = new BackgroundImageLabel(iconUrl, 50, 50);
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setBounds(110, 0, 50, 50);
            topPanel.add(iconLabel);
        } else {
            JLabel emptyLabel = new JLabel();
            emptyLabel.setMinimumSize(new Dimension(50, 50));
            emptyLabel.setMaximumSize(new Dimension(50, 50));
            emptyLabel.setPreferredSize(new Dimension(50, 50));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setBounds(110, 0, 50, 50);
            topPanel.add(emptyLabel);
        }

        add(topPanel, BorderLayout.NORTH);

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
        JLabel currentVersion = new JLabel(
                curseForgeProject != null ? mod.curseForgeFile.displayName : mod.modrinthVersion.name);
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

        if (curseForgeVersions != null) {
            for (CurseForgeFile curseForgeFile : curseForgeVersions) {
                updatedVersionComboBox.addItem(new ComboItem<>(curseForgeFile, curseForgeFile.displayName));
            }
        } else if (modrinthVersions != null) {
            for (ModrinthVersion modrinthVersion : modrinthVersions) {
                updatedVersionComboBox.addItem(new ComboItem<>(modrinthVersion, modrinthVersion.name));
            }
        }

        updatedVersionComboBox.addActionListener(e -> {
            Object selectedVersion = ((ComboItem<Object>) updatedVersionComboBox.getSelectedItem()).getValue();

            if (selectedVersion instanceof CurseForgeFile) {
                changelogButton.setEnabled(true);
                changelogButton.setToolTipText(null);
            } else if (selectedVersion instanceof ModrinthVersion) {
                ModrinthVersion version = (ModrinthVersion) selectedVersion;
                changelogButton.setEnabled(version.changelog != null && !version.changelog.isEmpty());
                changelogButton.setToolTipText(version.changelog != null && !version.changelog.isEmpty() ? null
                        : GetText.tr("No Changelog Available"));
            }
        });

        if (updatedVersionComboBox.getItemCount() != 0) {
            updatedVersionComboBox.setSelectedIndex(0);
        }
        updatedVersionComboBox.setPreferredSize(new Dimension(230, 23));
        updatedVersionPanel.add(updatedVersionComboBox);
        mainPanel.add(updatedVersionPanel, gbc);
        add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        bottomPanel.setLayout(new FlowLayout());

        changelogButton.addActionListener((e) -> {
            Object selectedVersion = ((ComboItem<Object>) updatedVersionComboBox.getSelectedItem()).getValue();

            if (selectedVersion instanceof CurseForgeFile) {
                CurseForgeFile version = (CurseForgeFile) selectedVersion;

                ProgressDialog<String> progressDialog = new ProgressDialog<>(GetText.tr("Getting Changelog"), 1,
                        GetText.tr("Getting Changelog"));
                progressDialog.addThread(new Thread(() -> {
                    progressDialog
                            .setReturnValue(CurseForgeApi.getChangelogForProjectFile(curseForgeProject.id, version.id));
                    progressDialog.doneTask();
                    progressDialog.close();
                }));
                progressDialog.start();

                if (progressDialog.getReturnValue() == null) {
                    int ret = DialogManager.okDialog()
                            .setTitle(GetText.tr("Failed To Get Changelog"))
                            .setContent(new HTMLBuilder().text(GetText.tr(
                                    "Failed to get changelog for this version.<br/>You can open the files page on CurseForge to view the changelog."))
                                    .center().build())
                            .addOption(GetText.tr("Open On CurseForge"))
                            .setType(DialogManager.ERROR).show();

                    if (ret == 1) {
                        OS.openWebBrowser(
                                String.format("%s/files/%d/changelog", curseForgeProject.getWebsiteUrl(), version.id));
                    }
                } else {
                    new HtmlDialog(
                            parentWindow,
                            GetText.tr("Changelog For {0} Version {1}", curseForgeProject.name,
                                    version.displayName),
                            progressDialog.getReturnValue());
                }
            } else if (selectedVersion instanceof ModrinthVersion) {
                ModrinthVersion version = (ModrinthVersion) selectedVersion;
                new HtmlDialog(
                        parentWindow,
                        GetText.tr("Changelog For {0} Version {1}", modrinthProject.title, version.name),
                        Markdown.render(version.changelog));
            }
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

    public boolean isUpdating() {
        return updateCheckBox.isSelected();
    }

    public Object getCurrentVersion() {
        return curseForgeProject != null ? mod.curseForgeFile : mod.modrinthVersion;
    }

    public Object getVersionUpdatingTo() {
        return ((ComboItem<Object>) updatedVersionComboBox.getSelectedItem()).getValue();
    }

    public boolean isCurseForgeMod() {
        return curseForgeProject != null;
    }

    public boolean isModrinthMod() {
        return modrinthProject != null;
    }

    public Object getModProject() {
        return curseForgeProject != null ? curseForgeProject : modrinthProject;
    }
}
