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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;

import org.apache.commons.text.WordUtils;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.data.curseforge.CurseForgeCategoryForGame;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.modrinth.ModrinthCategory;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthSearchHit;
import com.atlauncher.data.modrinth.ModrinthSearchResult;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.card.CurseForgeProjectCard;
import com.atlauncher.gui.card.ModrinthSearchHitCard;
import com.atlauncher.gui.layouts.WrapLayout;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.gui.panels.NoCurseModsPanel;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.OS;
import com.formdev.flatlaf.icons.FlatSearchIcon;

@SuppressWarnings("serial")
public final class AddModsDialog extends JDialog {
    private final Instance instance;

    private boolean updating = false;

    private final JPanel contentPanel = new JPanel(new WrapLayout());
    private final JPanel topPanel = new JPanel(new BorderLayout());
    private final JTextField searchField = new JTextField(16);
    private final JLabel platformMessageLabel = new JLabel();
    private final JComboBox<ComboItem<ModPlatform>> hostComboBox = new JComboBox<ComboItem<ModPlatform>>();
    private final JComboBox<ComboItem<String>> sectionComboBox = new JComboBox<ComboItem<String>>();
    private final JComboBox<ComboItem<String>> sortComboBox = new JComboBox<ComboItem<String>>();
    private final JComboBox<ComboItem<String>> categoriesComboBox = new JComboBox<ComboItem<String>>();

    // #. {0} is the loader api (Fabric API/QSL)
    private final JButton installFabricApiButton = new JButton(GetText.tr("Install {0}", "Fabric API"));

    private final JLabel fabricApiWarningLabel = new JLabel(
            "<html><p align=\"center\" style=\"color: "
                    + String.format("#%06x", 0xFFFFFF & UIManager.getColor("yellow").getRGB())
                    // #. {0} is the loader (Fabric/Quilt), {1} is the loader api (Fabric API/QSL)
                    + "\">" + GetText.tr("Before installing {0} mods, you should install {1} first!",
                            "Fabric", "Fabric API")
                    + "</p></html>");

    // #. {0} is the loader api (Fabric API/QSL)
    private final JButton installLegacyFabricApiButton = new JButton(GetText.tr("Install {0}", "Legacy Fabric API"));

    private final JLabel legacyFabricApiWarningLabel = new JLabel(
            "<html><p align=\"center\" style=\"color: "
                    + String.format("#%06x", 0xFFFFFF & UIManager.getColor("yellow").getRGB())
                    // #. {0} is the loader (Fabric/Quilt), {1} is the loader api (Fabric API/QSL)
                    + "\">" + GetText.tr("Before installing {0} mods, you should install {1} first!",
                            "Legacy Fabric", "Legacy Fabric API")
                    + "</p></html>");

    private final JEditorPane legacyFabricModrinthWarningLabel = new JEditorPane("text/html",
            "<html><p align=\"center\" style=\"color: "
                    + String.format("#%06x", 0xFFFFFF & UIManager.getColor("yellow").getRGB())
                    + "\">"
                    + GetText.tr(
                            "Modrinth doesn't support filtering accurately for Legacy Fabric, so mods shown may not be installable. Consider using CurseForge or visit <a href=\"https://legacyfabric.net/mods.html\">https://legacyfabric.net/mods.html</a> for a list of compatable mods.")
                    + "</p></html>");

    private final JButton installQuiltStandardLibrariesButton = new JButton(
            // #. {0} is the loader api (Fabric API/QSL)
            GetText.tr("Install {0}", "Quilt Standard Libraries"));

    private final JLabel quiltStandardLibrariesWarningLabel = new JLabel(
            "<html><p align=\"center\" style=\"color: "
                    + String.format("#%06x", 0xFFFFFF & UIManager.getColor("yellow").getRGB())
                    + "\">"
                    // #. {0} is the loader (Fabric/Quilt), {1} is the loader api (Fabric API/QSL)
                    + GetText.tr("Before installing {0} mods, you should install {1} first!",
                            "Quilt", "Quilt Standard Libraries")
                    + "</p></html>");

    private JScrollPane jscrollPane;
    private JButton nextButton;
    private JButton prevButton;
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private int page = 0;

    public AddModsDialog(Instance instance) {
        this(App.launcher.getParent(), instance);
    }

    public AddModsDialog(Window parent, Instance instance) {
        // #. {0} is the name of the mod we're installing
        super(parent, GetText.tr("Adding Mods For {0}", instance.launcher.name), ModalityType.DOCUMENT_MODAL);
        this.instance = instance;

        this.setPreferredSize(new Dimension(800, 500));
        this.setMinimumSize(new Dimension(800, 500));
        this.setResizable(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        if (ConfigManager.getConfigItem("platforms.curseforge.modsEnabled", true) == true) {
            hostComboBox.addItem(new ComboItem<>(ModPlatform.CURSEFORGE, "CurseForge"));
        }

        if (ConfigManager.getConfigItem("platforms.modrinth.modsEnabled", true) == true) {
            hostComboBox.addItem(new ComboItem<>(ModPlatform.MODRINTH, "Modrinth"));
        }

        hostComboBox.setSelectedIndex(App.settings.defaultModPlatform == ModPlatform.CURSEFORGE ? 0 : 1);

        searchField.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        searchField.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            searchField.setText("");
            searchForMods();
        });

        String platformMessage = ConfigManager.getConfigItem(String.format("platforms.%s.message",
                App.settings.defaultModPlatform == ModPlatform.CURSEFORGE ? "curseforge" : "modrinth"), null);
        if (platformMessage != null) {
            platformMessageLabel.setText(new HTMLBuilder().center().text(platformMessage).build());
        }
        platformMessageLabel.setVisible(platformMessage != null);

        if (instance.launcher.loaderVersion != null) {
            sectionComboBox.addItem(new ComboItem<>("Mods", GetText.tr("Mods")));
        }
        sectionComboBox.addItem(new ComboItem<>("Resource Packs", GetText.tr("Resource Packs")));
        sectionComboBox.addItem(new ComboItem<>("Shaders", GetText.tr("Shaders")));

        if (App.settings.defaultModPlatform == ModPlatform.CURSEFORGE) {
            sectionComboBox.addItem(new ComboItem<>("Worlds", GetText.tr("Worlds")));

            sortComboBox.addItem(new ComboItem<>("Popularity", GetText.tr("Popularity")));
            sortComboBox.addItem(new ComboItem<>("Last Updated", GetText.tr("Last Updated")));
            sortComboBox.addItem(new ComboItem<>("Total Downloads", GetText.tr("Total Downloads")));
        } else {
            sortComboBox.addItem(new ComboItem<>("relevance", GetText.tr("Relevance")));
            sortComboBox.addItem(new ComboItem<>("newest", GetText.tr("Newest")));
            sortComboBox.addItem(new ComboItem<>("updated", GetText.tr("Last Updated")));
            sortComboBox.addItem(new ComboItem<>("downloads", GetText.tr("Total Downloads")));
        }

        addCategories();

        setupComponents();

        this.loadDefaultMods();

        this.pack();
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
    }

    private void setupComponents() {
        Analytics.sendScreenView("Add Mods Dialog");

        this.topPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel searchButtonsPanel = new JPanel();

        searchButtonsPanel.setLayout(new BoxLayout(searchButtonsPanel, BoxLayout.X_AXIS));
        searchButtonsPanel.add(this.hostComboBox);
        searchButtonsPanel.add(Box.createHorizontalStrut(5));
        searchButtonsPanel.add(this.sectionComboBox);
        searchButtonsPanel.add(Box.createHorizontalStrut(5));
        searchButtonsPanel.add(this.sortComboBox);
        searchButtonsPanel.add(Box.createHorizontalStrut(5));
        searchButtonsPanel.add(this.categoriesComboBox);
        searchButtonsPanel.add(Box.createHorizontalStrut(20));
        searchButtonsPanel.add(this.searchField);

        this.installFabricApiButton.addActionListener(e -> {
            boolean isCurseForge = ((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                    .getValue() == ModPlatform.CURSEFORGE;
            if (isCurseForge) {
                final ProgressDialog<CurseForgeProject> curseForgeProjectLookupDialog = new ProgressDialog<>(
                        // #. {0} is the loader api were getting info from (Fabric/Quilt)
                        GetText.tr("Getting {0} Information", "Fabric API"), 0,
                        // #. {0} is the loader api were getting info from (Fabric/Quilt)
                        GetText.tr("Getting {0} Information", "Fabric API"),
                        "Aborting Getting Fabric API Information");

                curseForgeProjectLookupDialog.addThread(new Thread(() -> {
                    curseForgeProjectLookupDialog
                            .setReturnValue(CurseForgeApi.getProjectById(Constants.CURSEFORGE_FABRIC_MOD_ID));

                    curseForgeProjectLookupDialog.close();
                }));

                curseForgeProjectLookupDialog.start();

                CurseForgeProject mod = curseForgeProjectLookupDialog.getReturnValue();

                if (mod == null) {
                    // #. {0} is the loader api were getting info from (Fabric/Quilt)
                    DialogManager.okDialog().setTitle(GetText.tr("Error Getting {0} Information"))
                            // #. {0} is the loader (Fabric/Quilt) {1} is the platform (CurseForge/Modrinth)
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "There was an error getting {0} information from {1}. Please try again later.",
                                    "CurseForge"))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return;
                }

                Analytics.trackEvent(AnalyticsEvent.forAddMod("Fabric API", "CurseForge"));
                new CurseForgeProjectFileSelectorDialog(this, mod, instance);

                if (instance.launcher.mods.stream().anyMatch(
                        m -> (m.isFromCurseForge() && m.getCurseForgeModId() == Constants.CURSEFORGE_FABRIC_MOD_ID)
                                || (m.isFromModrinth()
                                        && m.modrinthProject.id.equalsIgnoreCase(Constants.MODRINTH_FABRIC_MOD_ID)))) {
                    fabricApiWarningLabel.setVisible(false);
                    installFabricApiButton.setVisible(false);
                }
            } else {
                final ProgressDialog<ModrinthProject> modrinthProjectLookupDialog = new ProgressDialog<>(
                        // #. {0} is the loader api were getting info from (Fabric/Quilt)
                        GetText.tr("Getting {0} Information", "Fabric API"), 0,
                        // #. {0} is the loader api were getting info from (Fabric/Quilt)
                        GetText.tr("Getting {0} Information", "Fabric API"),
                        "Aborting Getting Fabric API Information");

                modrinthProjectLookupDialog.addThread(new Thread(() -> {
                    modrinthProjectLookupDialog
                            .setReturnValue(ModrinthApi.getProject(Constants.MODRINTH_FABRIC_MOD_ID));

                    modrinthProjectLookupDialog.close();
                }));

                modrinthProjectLookupDialog.start();

                ModrinthProject mod = modrinthProjectLookupDialog.getReturnValue();

                if (mod == null) {
                    // #. {0} is the loader api were getting info from (Fabric/Quilt)
                    DialogManager.okDialog().setTitle(GetText.tr("Error Getting {0} Information"))
                            // #. {0} is the loader (Fabric/Quilt) {1} is the platform (CurseForge/Modrinth)
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "There was an error getting {0} information from {1}. Please try again later.",
                                    "Modrinth"))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return;
                }

                Analytics.trackEvent(AnalyticsEvent.forAddMod("Fabric API", "Modrinth"));
                new ModrinthVersionSelectorDialog(this, mod, instance);

                if (instance.launcher.mods.stream().anyMatch(
                        m -> (m.isFromCurseForge() && m.getCurseForgeModId() == Constants.CURSEFORGE_FABRIC_MOD_ID)
                                || (m.isFromModrinth()
                                        && m.modrinthProject.id.equalsIgnoreCase(Constants.MODRINTH_FABRIC_MOD_ID)))) {
                    fabricApiWarningLabel.setVisible(false);
                    installFabricApiButton.setVisible(false);
                }
            }

            if (searchField.getText().isEmpty()) {
                loadDefaultMods();
            } else {
                searchForMods();
            }
        });

        this.installLegacyFabricApiButton.addActionListener(e -> {
            boolean isCurseForge = ((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                    .getValue() == ModPlatform.CURSEFORGE;
            if (isCurseForge) {
                final ProgressDialog<CurseForgeProject> curseForgeProjectLookupDialog = new ProgressDialog<>(
                        // #. {0} is the loader api were getting info from (Fabric/Quilt)
                        GetText.tr("Getting {0} Information", "Legacy Fabric API"), 0,
                        // #. {0} is the loader api were getting info from (Fabric/Quilt)
                        GetText.tr("Getting {0} Information", "Legacy Fabric API"),
                        "Aborting Getting Legacy Fabric API Information");

                curseForgeProjectLookupDialog.addThread(new Thread(() -> {
                    curseForgeProjectLookupDialog
                            .setReturnValue(CurseForgeApi.getProjectById(Constants.CURSEFORGE_LEGACY_FABRIC_MOD_ID));

                    curseForgeProjectLookupDialog.close();
                }));

                curseForgeProjectLookupDialog.start();

                CurseForgeProject mod = curseForgeProjectLookupDialog.getReturnValue();

                if (mod == null) {
                    // #. {0} is the loader api were getting info from (Fabric/Quilt)
                    DialogManager.okDialog().setTitle(GetText.tr("Error Getting {0} Information"))
                            // #. {0} is the loader (Fabric/Quilt) {1} is the platform (CurseForge/Modrinth)
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "There was an error getting {0} information from {1}. Please try again later.",
                                    "CurseForge"))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return;
                }

                Analytics.trackEvent(AnalyticsEvent.forAddMod("Legacy Fabric API", "CurseForge"));
                new CurseForgeProjectFileSelectorDialog(this, mod, instance);

                if (instance.launcher.mods.stream().anyMatch(
                        m -> (m.isFromCurseForge()
                                && m.getCurseForgeModId() == Constants.CURSEFORGE_LEGACY_FABRIC_MOD_ID)
                                || (m.isFromModrinth()
                                        && m.modrinthProject.id
                                                .equalsIgnoreCase(Constants.MODRINTH_LEGACY_FABRIC_MOD_ID)))) {
                    legacyFabricApiWarningLabel.setVisible(false);
                    installLegacyFabricApiButton.setVisible(false);
                }
            } else {
                final ProgressDialog<ModrinthProject> modrinthProjectLookupDialog = new ProgressDialog<>(
                        // #. {0} is the loader api were getting info from (Fabric/Quilt)
                        GetText.tr("Getting {0} Information", "Legacy Fabric API"), 0,
                        // #. {0} is the loader api were getting info from (Fabric/Quilt)
                        GetText.tr("Getting {0} Information", "Legacy Fabric API"),
                        "Aborting Getting Legacy Fabric API Information");

                modrinthProjectLookupDialog.addThread(new Thread(() -> {
                    modrinthProjectLookupDialog
                            .setReturnValue(ModrinthApi.getProject(Constants.MODRINTH_LEGACY_FABRIC_MOD_ID));

                    modrinthProjectLookupDialog.close();
                }));

                modrinthProjectLookupDialog.start();

                ModrinthProject mod = modrinthProjectLookupDialog.getReturnValue();

                if (mod == null) {
                    // #. {0} is the loader api were getting info from (Fabric/Quilt)
                    DialogManager.okDialog().setTitle(GetText.tr("Error Getting {0} Information"))
                            // #. {0} is the loader (Fabric/Quilt) {1} is the platform (CurseForge/Modrinth)
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "There was an error getting {0} information from {1}. Please try again later.",
                                    "Modrinth"))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return;
                }

                Analytics.trackEvent(AnalyticsEvent.forAddMod("Legacy Fabric API", "Modrinth"));
                new ModrinthVersionSelectorDialog(this, mod, instance);

                if (instance.launcher.mods.stream().anyMatch(
                        m -> (m.isFromCurseForge()
                                && m.getCurseForgeModId() == Constants.CURSEFORGE_LEGACY_FABRIC_MOD_ID)
                                || (m.isFromModrinth()
                                        && m.modrinthProject.id
                                                .equalsIgnoreCase(Constants.MODRINTH_LEGACY_FABRIC_MOD_ID)))) {
                    legacyFabricApiWarningLabel.setVisible(false);
                    installLegacyFabricApiButton.setVisible(false);
                }
            }

            if (searchField.getText().isEmpty()) {
                loadDefaultMods();
            } else {
                searchForMods();
            }
        });

        this.installQuiltStandardLibrariesButton.addActionListener(e -> {
            final ProgressDialog<ModrinthProject> modrinthProjectLookupDialog = new ProgressDialog<>(
                    // #. {0} is the loader api were getting info from (Fabric/Quilt)
                    GetText.tr("Getting {0} Information", "Quilt Standard Libaries"), 0,
                    // #. {0} is the loader api were getting info from (Fabric/Quilt)
                    GetText.tr("Getting {0} Information", "Quilt Standard Libaries"),
                    "Aborting Getting Quilt Standard Libaries Information");

            modrinthProjectLookupDialog.addThread(new Thread(() -> {
                modrinthProjectLookupDialog
                        .setReturnValue(ModrinthApi.getProject(Constants.MODRINTH_QSL_MOD_ID));

                modrinthProjectLookupDialog.close();
            }));

            modrinthProjectLookupDialog.start();

            ModrinthProject mod = modrinthProjectLookupDialog.getReturnValue();

            if (mod == null) {
                DialogManager.okDialog()
                        // #. {0} is the loader api were getting info from (Fabric/Quilt)
                        .setTitle(GetText.tr("Error Getting {0} Information", "Quilt Standard Libaries"))
                        // #. {0} is the loader (Fabric/Quilt) {1} is the platform (CurseForge/Modrinth)
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "There was an error getting {0} information from {1}. Please try again later.",
                                "Quilt Standard Libaries", "Modrinth"))
                                .build())
                        .setType(DialogManager.ERROR).show();
                return;
            }

            Analytics.trackEvent(AnalyticsEvent.forAddMod("Quilt Standard Libraries", "Modrinth"));
            new ModrinthVersionSelectorDialog(this, mod, instance);

            if (instance.launcher.mods.stream().anyMatch(
                    m -> m.isFromModrinth()
                            && m.modrinthProject.id.equalsIgnoreCase(Constants.MODRINTH_QSL_MOD_ID))) {
                quiltStandardLibrariesWarningLabel.setVisible(false);
                installQuiltStandardLibrariesButton.setVisible(false);
            }
        });

        LoaderVersion loaderVersion = this.instance.launcher.loaderVersion;

        if (loaderVersion != null && loaderVersion.isFabric() && instance.launcher.mods.stream()
                .noneMatch(m -> (m.isFromCurseForge() && m.getCurseForgeModId() == Constants.CURSEFORGE_FABRIC_MOD_ID)
                        || m.isFromModrinth()
                                && m.modrinthProject.id.equalsIgnoreCase(Constants.MODRINTH_FABRIC_MOD_ID))) {
            this.topPanel.add(fabricApiWarningLabel, BorderLayout.CENTER);
            this.topPanel.add(installFabricApiButton, BorderLayout.EAST);
        }

        if (loaderVersion != null && loaderVersion.isLegacyFabric() && instance.launcher.mods.stream()
                .noneMatch(m -> (m.isFromCurseForge()
                        && m.getCurseForgeModId() == Constants.CURSEFORGE_LEGACY_FABRIC_MOD_ID)
                        || m.isFromModrinth()
                                && m.modrinthProject.id.equalsIgnoreCase(Constants.MODRINTH_LEGACY_FABRIC_MOD_ID))) {
            this.topPanel.add(legacyFabricApiWarningLabel, BorderLayout.CENTER);
            this.topPanel.add(installLegacyFabricApiButton, BorderLayout.EAST);
        }

        if (loaderVersion != null && loaderVersion.isLegacyFabric()) {
            this.topPanel.add(legacyFabricModrinthWarningLabel, BorderLayout.SOUTH);
        }

        if (loaderVersion != null && loaderVersion.isQuilt() && instance.launcher.mods.stream()
                .noneMatch(m -> m.isFromModrinth()
                        && m.modrinthProject.id.equalsIgnoreCase(Constants.MODRINTH_QSL_MOD_ID))) {
            this.topPanel.add(quiltStandardLibrariesWarningLabel, BorderLayout.CENTER);
            this.topPanel.add(installQuiltStandardLibrariesButton, BorderLayout.EAST);
        }

        this.topPanel.add(searchButtonsPanel, BorderLayout.NORTH);

        this.jscrollPane = new JScrollPane(this.contentPanel) {
            {
                this.getVerticalScrollBar().setUnitIncrement(16);
            }
        };

        this.jscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(this.topPanel, BorderLayout.NORTH);
        mainPanel.add(this.jscrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel bottomButtonsPanel = new JPanel(new FlowLayout());

        prevButton = new JButton("<<");
        prevButton.setEnabled(false);
        prevButton.addActionListener(e -> goToPreviousPage());

        nextButton = new JButton(">>");
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> goToNextPage());

        bottomButtonsPanel.add(prevButton);
        bottomButtonsPanel.add(nextButton);

        legacyFabricModrinthWarningLabel.setVisible(((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                .getValue() == ModPlatform.MODRINTH);
        legacyFabricModrinthWarningLabel.setEditable(false);
        legacyFabricModrinthWarningLabel.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OS.openWebBrowser(e.getURL());
            }
        });

        platformMessageLabel.setForeground(UIManager.getColor("yellow"));
        bottomPanel.add(platformMessageLabel, BorderLayout.NORTH);
        bottomPanel.add(bottomButtonsPanel, BorderLayout.CENTER);

        this.add(mainPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        this.hostComboBox.addActionListener(e -> {
            updating = true;
            page = 0;
            boolean isCurseForge = ((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                    .getValue() == ModPlatform.CURSEFORGE;

            boolean resourcePacksSelected = ((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue()
                    .equals("Resource Packs");
            boolean shadersSelected = ((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue()
                    .equals("Shaders");

            String platformMessage = null;

            sortComboBox.removeAllItems();
            sectionComboBox.removeAllItems();

            if (instance.launcher.loaderVersion != null) {
                sectionComboBox.addItem(new ComboItem<>("Mods", GetText.tr("Mods")));
            }

            sectionComboBox.addItem(new ComboItem<>("Resource Packs", GetText.tr("Resource Packs")));
            if (resourcePacksSelected) {
                sectionComboBox.setSelectedIndex(sectionComboBox.getItemCount() - 1);
            }

            sectionComboBox.addItem(new ComboItem<>("Shaders", GetText.tr("Shaders")));
            if (shadersSelected) {
                sectionComboBox.setSelectedIndex(sectionComboBox.getItemCount() - 1);
            }

            if (isCurseForge) {
                platformMessage = ConfigManager.getConfigItem("platforms.curseforge.message", null);
                sectionComboBox.addItem(new ComboItem<>("Worlds", GetText.tr("Worlds")));
                sortComboBox.addItem(new ComboItem<>("Popularity", GetText.tr("Popularity")));
                sortComboBox.addItem(new ComboItem<>("Last Updated", GetText.tr("Last Updated")));
                sortComboBox.addItem(new ComboItem<>("Total Downloads", GetText.tr("Total Downloads")));
            } else {
                platformMessage = ConfigManager.getConfigItem("platforms.modrinth.message", null);
                sortComboBox.addItem(new ComboItem<>("relevance", GetText.tr("Relevance")));
                sortComboBox.addItem(new ComboItem<>("newest", GetText.tr("Newest")));
                sortComboBox.addItem(new ComboItem<>("updated", GetText.tr("Last Updated")));
                sortComboBox.addItem(new ComboItem<>("downloads", GetText.tr("Total Downloads")));
            }

            addCategories();

            if (platformMessage != null) {
                platformMessageLabel.setText(new HTMLBuilder().center().text(platformMessage).build());
            }
            platformMessageLabel.setVisible(platformMessage != null);
            legacyFabricModrinthWarningLabel
                    .setVisible(loaderVersion != null && loaderVersion.isLegacyFabric() && !isCurseForge);

            if (searchField.getText().isEmpty()) {
                loadDefaultMods();
            } else {
                searchForMods();
            }
            updating = false;
        });

        this.sectionComboBox.addActionListener(e -> {
            if (!updating) {
                page = 0;

                addCategories();

                if (searchField.getText().isEmpty()) {
                    loadDefaultMods();
                } else {
                    searchForMods();
                }
            }
        });

        this.sortComboBox.addActionListener(e -> {
            if (!updating) {
                page = 0;

                if (searchField.getText().isEmpty()) {
                    loadDefaultMods();
                } else {
                    searchForMods();
                }
            }
        });

        this.categoriesComboBox.addActionListener(e -> {
            if (!updating) {
                page = 0;

                if (searchField.getText().isEmpty()) {
                    loadDefaultMods();
                } else {
                    searchForMods();
                }
            }
        });

        this.searchField.addActionListener(e -> searchForMods());
    }

    private void setLoading(boolean loading) {
        if (loading) {
            contentPanel.removeAll();
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new LoadingPanel(), BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    private void goToPreviousPage() {
        if (page > 0) {
            page -= 1;
        }

        boolean isCurseForge = ((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                .getValue() == ModPlatform.CURSEFORGE;
        Analytics.trackEvent(
                AnalyticsEvent.forSearchEventPlatform("add_mods", searchField.getText(), page + 1,
                        isCurseForge ? "CurseForge" : "Modrinth"));

        getMods();
    }

    private void goToNextPage() {
        if (contentPanel.getComponentCount() != 0) {
            page += 1;
        }

        boolean isCurseForge = ((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                .getValue() == ModPlatform.CURSEFORGE;
        Analytics.trackEvent(
                AnalyticsEvent.forSearchEventPlatform("add_mods", searchField.getText(), page + 1,
                        isCurseForge ? "CurseForge" : "Modrinth"));

        getMods();
    }

    @SuppressWarnings("unchecked")
    private void getMods() {
        setLoading(true);
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);

        String query = searchField.getText();
        boolean isCurseForge = ((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                .getValue() == ModPlatform.CURSEFORGE;

        new Thread(() -> {
            if (isCurseForge) {
                String versionToSearchFor = App.settings.addModRestriction == AddModRestriction.STRICT
                        ? this.instance.id
                        : null;

                if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Resource Packs")) {
                    setCurseForgeMods(CurseForgeApi.searchResourcePacks(query, page,
                            ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                            ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                    : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                } else if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Shaders")) {
                    setCurseForgeMods(CurseForgeApi.searchShaderPacks(query, page,
                            ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                            ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                    : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                } else if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Worlds")) {
                    setCurseForgeMods(CurseForgeApi.searchWorlds(versionToSearchFor, query, page,
                            ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                            ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                    : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                } else {
                    if (this.instance.launcher.loaderVersion.isFabric()
                            || this.instance.launcher.loaderVersion.isLegacyFabric()) {
                        setCurseForgeMods(CurseForgeApi.searchModsForFabric(versionToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                                ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                        : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                    } else if (this.instance.launcher.loaderVersion.isQuilt()) {
                        setCurseForgeMods(CurseForgeApi.searchModsForQuilt(versionToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                                ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                        : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                    } else if (this.instance.launcher.loaderVersion.isForge()) {
                        setCurseForgeMods(CurseForgeApi.searchModsForForge(versionToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                                ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                        : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                    } else if (this.instance.launcher.loaderVersion.isNeoForge()) {
                        setCurseForgeMods(CurseForgeApi.searchModsForNeoForge(versionToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                                ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                        : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                    } else {
                        setCurseForgeMods(CurseForgeApi.searchMods(versionToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                                ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                        : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                    }
                }
            } else {
                List<String> versionsToSearchFor = new ArrayList<>();

                if (App.settings.addModRestriction == AddModRestriction.STRICT) {
                    versionsToSearchFor.add(this.instance.id);
                } else if (App.settings.addModRestriction == AddModRestriction.LAX) {
                    try {
                        versionsToSearchFor.addAll(MinecraftManager.getMajorMinecraftVersions(this.instance.id).stream()
                                .map(mv -> mv.id).collect(Collectors.toList()));
                    } catch (InvalidMinecraftVersion e) {
                        LogManager.logStackTrace(e);
                        versionsToSearchFor = null;
                    }
                } else if (App.settings.addModRestriction == AddModRestriction.NONE) {
                    versionsToSearchFor = null;
                }

                if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Resource Packs")) {
                    setModrinthMods(ModrinthApi.searchResourcePacks(versionsToSearchFor, query, page,
                            ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                            ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                    : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                } else if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Shaders")) {
                    setModrinthMods(ModrinthApi.searchShaders(versionsToSearchFor, query, page,
                            ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                            ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                    : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                } else {
                    if (this.instance.launcher.loaderVersion.isFabric()
                            || this.instance.launcher.loaderVersion.isLegacyFabric()) {
                        setModrinthMods(ModrinthApi.searchModsForFabric(versionsToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                                ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                        : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                    } else if (this.instance.launcher.loaderVersion.isQuilt()) {
                        setModrinthMods(ModrinthApi.searchModsForQuiltOrFabric(versionsToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                                ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                        : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                    } else if (this.instance.launcher.loaderVersion.isForge()) {
                        setModrinthMods(ModrinthApi.searchModsForForge(versionsToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                                ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                        : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                    } else if (this.instance.launcher.loaderVersion.isNeoForge()) {
                        setModrinthMods(ModrinthApi.searchModsForNeoForge(versionsToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue(),
                                ((ComboItem<String>) categoriesComboBox.getSelectedItem()) == null ? null
                                        : ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue()));
                    }
                }
            }

            setLoading(false);
        }).start();
    }

    private void loadDefaultMods() {
        getMods();
    }

    private void searchForMods() {
        String query = searchField.getText();

        page = 0;

        boolean isCurseForge = ((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                .getValue() == ModPlatform.CURSEFORGE;
        Analytics.trackEvent(
                AnalyticsEvent.forSearchEventPlatform("add_mods", query, page + 1,
                        isCurseForge ? "CurseForge" : "Modrinth"));

        getMods();
    }

    private void setCurseForgeMods(List<CurseForgeProject> mods) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.set(2, 2, 2, 2);

        contentPanel.removeAll();

        if (mods == null || mods.size() == 0) {
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new NoCurseModsPanel(!this.searchField.getText().isEmpty()), BorderLayout.CENTER);
        } else {
            prevButton.setEnabled(page > 0);
            nextButton.setEnabled(mods.size() == Constants.CURSEFORGE_PAGINATION_SIZE);

            contentPanel.setLayout(new WrapLayout());

            mods.forEach(mod -> {
                CurseForgeProject castMod = (CurseForgeProject) mod;

                contentPanel.add(new CurseForgeProjectCard(castMod, instance, e -> {
                    Analytics.trackEvent(AnalyticsEvent.forAddMod(castMod));
                    new CurseForgeProjectFileSelectorDialog(this, castMod, instance);
                }, e -> {
                    Analytics.trackEvent(AnalyticsEvent.forRemoveMod(castMod));

                    Optional<DisableableMod> foundMod = instance.launcher.mods.stream()
                            .filter(dm -> dm.isFromCurseForge() && dm.curseForgeProjectId == castMod.id)
                            .findFirst();

                    if (foundMod.isPresent()) {
                        instance.removeMod(foundMod.get());

                        if (castMod.id == Constants.CURSEFORGE_FABRIC_MOD_ID) {
                            fabricApiWarningLabel.setVisible(true);
                            installFabricApiButton.setVisible(true);
                        }

                        if (castMod.id == Constants.CURSEFORGE_LEGACY_FABRIC_MOD_ID) {
                            legacyFabricApiWarningLabel.setVisible(true);
                            installLegacyFabricApiButton.setVisible(true);
                        }
                    }
                }), gbc);

                gbc.gridy++;
            });
        }

        SwingUtilities.invokeLater(() -> jscrollPane.getVerticalScrollBar().setValue(0));

        revalidate();
        repaint();
    }

    private void setModrinthMods(ModrinthSearchResult searchResult) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.set(2, 2, 2, 2);

        contentPanel.removeAll();

        if (searchResult == null || searchResult.hits.size() == 0) {
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new NoCurseModsPanel(!this.searchField.getText().isEmpty()), BorderLayout.CENTER);
        } else {
            prevButton.setEnabled(page > 0);
            nextButton.setEnabled((searchResult.offset + searchResult.limit) < searchResult.totalHits);

            contentPanel.setLayout(new WrapLayout());

            searchResult.hits.forEach(mod -> {
                ModrinthSearchHit castMod = (ModrinthSearchHit) mod;

                contentPanel.add(new ModrinthSearchHitCard(castMod, instance, e -> {
                    final ProgressDialog<ModrinthProject> modrinthProjectLookupDialog = new ProgressDialog<>(
                            GetText.tr("Getting Mod Information"), 0, GetText.tr("Getting Mod Information"),
                            "Aborting Getting Mod Information");

                    modrinthProjectLookupDialog.addThread(new Thread(() -> {
                        modrinthProjectLookupDialog.setReturnValue(ModrinthApi.getProject(castMod.projectId));

                        modrinthProjectLookupDialog.close();
                    }));

                    modrinthProjectLookupDialog.start();

                    ModrinthProject modrinthMod = modrinthProjectLookupDialog.getReturnValue();

                    if (modrinthMod == null) {
                        DialogManager.okDialog().setTitle(GetText.tr("Error Getting Mod Information"))
                                .setContent(new HTMLBuilder().center().text(GetText.tr(
                                        "There was an error getting mod information from Modrinth. Please try again later."))
                                        .build())
                                .setType(DialogManager.ERROR).show();
                        return;
                    }

                    Analytics.trackEvent(AnalyticsEvent.forAddMod(castMod));
                    new ModrinthVersionSelectorDialog(this, modrinthMod, instance);
                }, e -> {
                    Analytics.trackEvent(AnalyticsEvent.forRemoveMod(castMod));

                    Optional<DisableableMod> foundMod = instance.launcher.mods.stream()
                            .filter(dm -> dm.isFromModrinth() && dm.modrinthProject.id.equals(castMod.projectId))
                            .findFirst();

                    if (foundMod.isPresent()) {
                        instance.removeMod(foundMod.get());

                        if (castMod.projectId.equals(Constants.MODRINTH_FABRIC_MOD_ID)) {
                            fabricApiWarningLabel.setVisible(true);
                            installFabricApiButton.setVisible(true);
                        }

                        if (castMod.projectId.equals(Constants.MODRINTH_LEGACY_FABRIC_MOD_ID)) {
                            legacyFabricApiWarningLabel.setVisible(true);
                            installLegacyFabricApiButton.setVisible(true);
                        }

                        if (castMod.projectId.equals(Constants.MODRINTH_QSL_MOD_ID)) {
                            quiltStandardLibrariesWarningLabel.setVisible(true);
                            installQuiltStandardLibrariesButton.setVisible(true);
                        }
                    }
                }), gbc);

                gbc.gridy++;
            });
        }

        SwingUtilities.invokeLater(() -> jscrollPane.getVerticalScrollBar().setValue(0));

        revalidate();
        repaint();
    }

    private void addCategories() {
        updating = true;
        categoriesComboBox.removeAllItems();

        categoriesComboBox.addItem(new ComboItem<>(null, GetText.tr("All Categories")));

        boolean isCurseForge = ((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                .getValue() == ModPlatform.CURSEFORGE;

        if (isCurseForge) {
            List<CurseForgeCategoryForGame> categories = new ArrayList<>();

            if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Resource Packs")) {
                categories.addAll(CurseForgeApi.getCategoriesForResourcePacks());
            } else if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Shaders")) {
                categories.addAll(CurseForgeApi.getCategoriesForShaderPacks());
            } else if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Worlds")) {
                categories.addAll(CurseForgeApi.getCategoriesForWorlds());
            } else {
                categories.addAll(CurseForgeApi.getCategoriesForMods());
            }

            categories.forEach(
                    c -> categoriesComboBox.addItem(new ComboItem<>(String.valueOf(c.id), c.name)));
        } else {
            List<ModrinthCategory> categories = new ArrayList<>();

            if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Resource Packs")) {
                categories.addAll(ModrinthApi.getCategoriesForResourcePacks());
            } else if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Shaders")) {
                categories.addAll(ModrinthApi.getCategoriesForShaders());
            } else {
                categories.addAll(ModrinthApi.getCategoriesForMods());
            }

            categories.forEach(
                    c -> categoriesComboBox.addItem(new ComboItem<>(c.name, WordUtils.capitalizeFully(c.name))));
        }
        updating = false;
    }
}
