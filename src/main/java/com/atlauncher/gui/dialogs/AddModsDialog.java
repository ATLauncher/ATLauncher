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
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.Instance;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
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
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.ModrinthApi;
import com.formdev.flatlaf.icons.FlatSearchIcon;

@SuppressWarnings("serial")
public final class AddModsDialog extends JDialog {
    private static final Logger LOG = LogManager.getLogger(AddModsDialog.class);

    private final Instance instance;

    private boolean updating = false;

    private final JPanel contentPanel = new JPanel(new WrapLayout());
    private final JPanel topPanel = new JPanel(new BorderLayout());
    private final JTextField searchField = new JTextField(16);
    private final JLabel platformMessageLabel = new JLabel();
    private final JComboBox<ComboItem<ModPlatform>> hostComboBox = new JComboBox<ComboItem<ModPlatform>>();
    private final JComboBox<ComboItem<String>> sectionComboBox = new JComboBox<ComboItem<String>>();
    private final JComboBox<ComboItem<String>> sortComboBox = new JComboBox<ComboItem<String>>();

    // #. Fabric API is the name of a mod, so should be left untranslated
    private final JButton installFabricApiButton = new JButton(GetText.tr("Install Fabric API"));

    // #. Fabric/Fabric API is the name of a mod, so should be left untranslated
    private final JLabel fabricApiWarningLabel = new JLabel(
            "<html><p align=\"center\" style=\"color: "
                    + String.format("#%06x", 0xFFFFFF & UIManager.getColor("yellow").getRGB())
                    + "\">Before installing Fabric mods, you should install Fabric API first!</p></html>");

    // #. Quilt Standard Libraries is the name of a mod, so should be left
    private final JButton installQuiltStandardLibrariesButton = new JButton(
            GetText.tr("Install Quilt Standard Libraries"));

    // #. Quilt/Quilt Standard Libraries is the name of a mod, so should be left
    private final JLabel quiltStandardLibrariesWarningLabel = new JLabel(
            "<html><p align=\"center\" style=\"color: "
                    + String.format("#%06x", 0xFFFFFF & UIManager.getColor("yellow").getRGB())
                    + "\">Before installing Quilt mods, you should install Quilt Standard Libraries first!</p></html>");

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

        this.setPreferredSize(new Dimension(680, 500));
        this.setMinimumSize(new Dimension(680, 500));
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
        sectionComboBox.addItem(new ComboItem<>("Worlds", GetText.tr("Worlds")));
        sectionComboBox.setVisible(App.settings.defaultModPlatform == ModPlatform.CURSEFORGE);

        if (App.settings.defaultModPlatform == ModPlatform.CURSEFORGE) {
            sortComboBox.addItem(new ComboItem<>("Popularity", GetText.tr("Popularity")));
            sortComboBox.addItem(new ComboItem<>("Last Updated", GetText.tr("Last Updated")));
            sortComboBox.addItem(new ComboItem<>("Total Downloads", GetText.tr("Total Downloads")));
        } else {
            sortComboBox.addItem(new ComboItem<>("relevance", GetText.tr("Relevance")));
            sortComboBox.addItem(new ComboItem<>("newest", GetText.tr("Newest")));
            sortComboBox.addItem(new ComboItem<>("updated", GetText.tr("Last Updated")));
            sortComboBox.addItem(new ComboItem<>("downloads", GetText.tr("Total Downloads")));
        }

        setupComponents();

        this.loadDefaultMods();

        this.pack();
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
    }

    private void setupComponents() {
        Analytics.sendScreenView("Add Mods Dialog");

        this.topPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel searchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchButtonsPanel.add(this.hostComboBox);
        searchButtonsPanel.add(this.searchField);
        searchButtonsPanel.add(this.sectionComboBox);
        searchButtonsPanel.add(this.sortComboBox);

        this.installFabricApiButton.addActionListener(e -> {
            boolean isCurseForge = ((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                    .getValue() == ModPlatform.CURSEFORGE;
            if (isCurseForge) {
                final ProgressDialog<CurseForgeProject> curseForgeProjectLookupDialog = new ProgressDialog<>(
                        GetText.tr("Getting Fabric API Information"), 0, GetText.tr("Getting Fabric API Information"),
                        "Aborting Getting Fabric API Information");

                curseForgeProjectLookupDialog.addThread(new Thread(() -> {
                    curseForgeProjectLookupDialog
                            .setReturnValue(CurseForgeApi.getProjectById(Constants.CURSEFORGE_FABRIC_MOD_ID));

                    curseForgeProjectLookupDialog.close();
                }));

                curseForgeProjectLookupDialog.start();

                CurseForgeProject mod = curseForgeProjectLookupDialog.getReturnValue();

                if (mod == null) {
                    DialogManager.okDialog().setTitle(GetText.tr("Error Getting Fabric API Information"))
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "There was an error getting Fabric API information from CurseForge. Please try again later."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return;
                }

                Analytics.sendEvent("AddFabricApi", "CurseForgeMod");
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
                        GetText.tr("Getting Fabric API Information"), 0, GetText.tr("Getting Fabric API Information"),
                        "Aborting Getting Fabric API Information");

                modrinthProjectLookupDialog.addThread(new Thread(() -> {
                    modrinthProjectLookupDialog
                            .setReturnValue(ModrinthApi.getProject(Constants.MODRINTH_FABRIC_MOD_ID));

                    modrinthProjectLookupDialog.close();
                }));

                modrinthProjectLookupDialog.start();

                ModrinthProject mod = modrinthProjectLookupDialog.getReturnValue();

                if (mod == null) {
                    DialogManager.okDialog().setTitle(GetText.tr("Error Getting Fabric API Information"))
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "There was an error getting Fabric API information from Modrinth. Please try again later."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return;
                }

                Analytics.sendEvent("AddFabricApi", "ModrinthMod");
                new ModrinthVersionSelectorDialog(this, mod, instance);

                if (instance.launcher.mods.stream().anyMatch(
                        m -> (m.isFromCurseForge() && m.getCurseForgeModId() == Constants.CURSEFORGE_FABRIC_MOD_ID)
                                || (m.isFromModrinth()
                                        && m.modrinthProject.id.equalsIgnoreCase(Constants.MODRINTH_FABRIC_MOD_ID)))) {
                    fabricApiWarningLabel.setVisible(false);
                    installFabricApiButton.setVisible(false);
                }
            }
        });

        this.installQuiltStandardLibrariesButton.addActionListener(e -> {
            final ProgressDialog<ModrinthProject> modrinthProjectLookupDialog = new ProgressDialog<>(
                    GetText.tr("Getting Quilt Standard Libaries Information"), 0,
                    GetText.tr("Getting Quilt Standard Libaries Information"),
                    "Aborting Getting Quilt Standard Libaries Information");

            modrinthProjectLookupDialog.addThread(new Thread(() -> {
                modrinthProjectLookupDialog
                        .setReturnValue(ModrinthApi.getProject(Constants.MODRINTH_QSL_MOD_ID));

                modrinthProjectLookupDialog.close();
            }));

            modrinthProjectLookupDialog.start();

            ModrinthProject mod = modrinthProjectLookupDialog.getReturnValue();

            if (mod == null) {
                DialogManager.okDialog().setTitle(GetText.tr("Error Getting Quilt Standard Libaries Information"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "There was an error getting Quilt Standard Libaries information from Modrinth. Please try again later."))
                                .build())
                        .setType(DialogManager.ERROR).show();
                return;
            }

            Analytics.sendEvent("AddQuiltStandardLibraries", "ModrinthMod");
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

        platformMessageLabel.setForeground(UIManager.getColor("yellow"));
        bottomPanel.add(platformMessageLabel, BorderLayout.NORTH);
        bottomPanel.add(bottomButtonsPanel, BorderLayout.CENTER);

        this.add(mainPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        this.hostComboBox.addActionListener(e -> {
            updating = true;
            boolean isCurseForge = ((ComboItem<ModPlatform>) hostComboBox.getSelectedItem())
                    .getValue() == ModPlatform.CURSEFORGE;

            String platformMessage = null;

            sortComboBox.removeAllItems();
            if (isCurseForge) {
                platformMessage = ConfigManager.getConfigItem("platforms.curseforge.message", null);
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

            sectionComboBox.setVisible(isCurseForge);

            if (platformMessage != null) {
                platformMessageLabel.setText(new HTMLBuilder().center().text(platformMessage).build());
            }
            platformMessageLabel.setVisible(platformMessage != null);

            if (searchField.getText().isEmpty()) {
                loadDefaultMods();
            } else {
                searchForMods();
            }
            updating = false;
        });

        this.sectionComboBox.addActionListener(e -> {
            if (!updating) {
                if (searchField.getText().isEmpty()) {
                    loadDefaultMods();
                } else {
                    searchForMods();
                }
            }
        });

        this.sortComboBox.addActionListener(e -> {
            if (!updating) {
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

        Analytics.sendEvent(page, "Previous", "Navigation", "CurseForgeMod");

        getMods();
    }

    private void goToNextPage() {
        if (contentPanel.getComponentCount() != 0) {
            page += 1;
        }

        Analytics.sendEvent(page, "Next", "Navigation", "CurseForgeMod");

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
                            ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue()));
                } else if (((ComboItem<String>) sectionComboBox.getSelectedItem()).getValue().equals("Worlds")) {
                    setCurseForgeMods(CurseForgeApi.searchWorlds(versionToSearchFor, query, page,
                            ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue()));
                } else {
                    if (this.instance.launcher.loaderVersion.isFabric() || this.instance.launcher.loaderVersion
                            .isQuilt()) {
                        setCurseForgeMods(CurseForgeApi.searchModsForFabric(versionToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue()));
                    } else if (this.instance.launcher.loaderVersion.isForge()) {
                        setCurseForgeMods(CurseForgeApi.searchModsForForge(versionToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue()));
                    } else {
                        setCurseForgeMods(CurseForgeApi.searchMods(versionToSearchFor, query, page,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue()));
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
                        LOG.error("error", e);
                        versionsToSearchFor = null;
                    }
                } else if (App.settings.addModRestriction == AddModRestriction.NONE) {
                    versionsToSearchFor = null;
                }

                if (this.instance.launcher.loaderVersion.isFabric()) {
                    setModrinthMods(ModrinthApi.searchModsForFabric(versionsToSearchFor, query, page,
                            ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue()));
                } else if (this.instance.launcher.loaderVersion.isQuilt()) {
                    setModrinthMods(ModrinthApi.searchModsForQuiltOrFabric(versionsToSearchFor, query, page,
                            ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue()));
                } else if (this.instance.launcher.loaderVersion.isForge()) {
                    setModrinthMods(ModrinthApi.searchModsForForge(versionsToSearchFor, query, page,
                            ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue()));
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

        Analytics.sendEvent(query, "Search", "CurseForgeMod");

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

        prevButton.setEnabled(page > 0);
        nextButton.setEnabled(mods.size() == Constants.CURSEFORGE_PAGINATION_SIZE);

        if (mods.size() == 0) {
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new NoCurseModsPanel(!this.searchField.getText().isEmpty()), BorderLayout.CENTER);
        } else {
            contentPanel.setLayout(new WrapLayout());

            mods.forEach(mod -> {
                CurseForgeProject castMod = (CurseForgeProject) mod;

                contentPanel.add(new CurseForgeProjectCard(castMod, e -> {
                    Analytics.sendEvent(castMod.name, "Add", "CurseForgeMod");
                    new CurseForgeProjectFileSelectorDialog(this, castMod, instance);
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

        prevButton.setEnabled(page > 0);
        nextButton.setEnabled((searchResult.offset + searchResult.limit) < searchResult.totalHits);

        if (searchResult.hits.size() == 0) {
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new NoCurseModsPanel(!this.searchField.getText().isEmpty()), BorderLayout.CENTER);
        } else {
            contentPanel.setLayout(new WrapLayout());

            searchResult.hits.forEach(mod -> {
                ModrinthSearchHit castMod = (ModrinthSearchHit) mod;

                contentPanel.add(new ModrinthSearchHitCard(castMod, e -> {
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

                    Analytics.sendEvent(castMod.title, "Add", "ModrinthMod");
                    new ModrinthVersionSelectorDialog(this, modrinthMod, instance);
                }), gbc);

                gbc.gridy++;
            });
        }

        SwingUtilities.invokeLater(() -> jscrollPane.getVerticalScrollBar().setValue(0));

        revalidate();
        repaint();
    }
}
