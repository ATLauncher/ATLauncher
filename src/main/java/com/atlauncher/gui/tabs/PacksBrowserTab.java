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
package com.atlauncher.gui.tabs;

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.events.LocalizationEvent;
import com.atlauncher.events.OnSide;
import com.atlauncher.events.Side;
import com.atlauncher.events.ThemeEvent;
import com.atlauncher.gui.panels.packbrowser.ATLauncherFeaturedPacksPanel;
import com.atlauncher.gui.panels.packbrowser.ATLauncherPacksPanel;
import com.atlauncher.gui.panels.packbrowser.CurseForgePacksPanel;
import com.atlauncher.gui.panels.packbrowser.FTBPacksPanel;
import com.atlauncher.gui.panels.packbrowser.ModrinthPacksPanel;
import com.atlauncher.gui.panels.packbrowser.PackBrowserPlatformPanel;
import com.atlauncher.gui.panels.packbrowser.PacksBrowserTabTitlePanel;
import com.atlauncher.gui.panels.packbrowser.TechnicPacksPanel;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.Utils;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public final class PacksBrowserTab extends JPanel implements Tab {
    private final JPanel actionsPanel = new JPanel();

    private final JPanel minecraftVersionPanel = new JPanel();
    private final JLabel minecraftVersionLabel = new JLabel(GetText.tr("Minecraft:"));
    private final JComboBox<ComboItem<String>> minecraftVersionComboBox = new JComboBox<>();

    private final JPanel categoriesPanel = new JPanel();
    private final JLabel categoriesLabel = new JLabel(GetText.tr("Category:"));
    private final JComboBox<ComboItem<String>> categoriesComboBox = new JComboBox<>();

    private final JPanel sortPanel = new JPanel();
    private final JLabel sortLabel = new JLabel(GetText.tr("Sort:"));
    private final JComboBox<ComboItem<String>> sortComboBox = new JComboBox<>();
    private boolean sortDescending = true;
    private final JButton ascendingSortButton = new JButton(Utils.getIconImage(App.THEME.getIconPath("ascending")));
    private final JButton descendingSortButton = new JButton(Utils.getIconImage(App.THEME.getIconPath("descending")));

    private final JPanel spacer = new JPanel();
    private final JTextField searchField = new JTextField(16);
    private final JButton addManuallyButton = new JButton(GetText.tr("Add Manually"));

    private final JPanel platformMessageJPanel = new JPanel(new BorderLayout());
    private final JLabel platformMessageJLabel = new JLabel();

    private final JTabbedPane platformTabbedPane = new JTabbedPane();
    private final PackBrowserPlatformPanel atlauncherPacksPanel = new ATLauncherPacksPanel();
    private final PackBrowserPlatformPanel atlauncherFeaturedPacksPanel = new ATLauncherFeaturedPacksPanel();
    private final PackBrowserPlatformPanel curseForgePacksPanel = new CurseForgePacksPanel();
    private final PackBrowserPlatformPanel ftbPacksPanel = new FTBPacksPanel();
    private final PackBrowserPlatformPanel modrinthPacksPanel = new ModrinthPacksPanel();
    private final PackBrowserPlatformPanel technicPacksPanel = new TechnicPacksPanel();

    private JScrollPane scrollPane;
    private final JPanel contentPanel = new JPanel();

    private boolean loading = false;
    private int page = 1;

    public PacksBrowserTab() {
        super(new BorderLayout());
        this.setName("packsBrowserPanel");
        this.initComponents();
        AppEventBus.register(this);
    }

    private void initComponents() {
        actionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel
            .setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));

        minecraftVersionPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
        minecraftVersionPanel.add(minecraftVersionLabel);
        minecraftVersionPanel.add(minecraftVersionComboBox);
        actionsPanel.add(minecraftVersionPanel);

        minecraftVersionComboBox.addActionListener(e -> {
            if (!loading) {
                loading = true;
                page = 1;

                // disable the tabs
                platformTabbedPane.setEnabled(false);

                load(true);
            }
        });

        categoriesPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
        categoriesPanel.add(categoriesLabel);
        categoriesPanel.add(categoriesComboBox);
        actionsPanel.add(categoriesPanel);

        categoriesComboBox.addActionListener(e -> {
            if (!loading) {
                loading = true;
                page = 1;

                // disable the tabs
                platformTabbedPane.setEnabled(false);

                load(true);
            }
        });

        sortPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 0));
        sortPanel.add(sortLabel);
        sortPanel.add(sortComboBox);
        sortPanel.add(ascendingSortButton);
        sortPanel.add(descendingSortButton);
        ascendingSortButton.setVisible(false);
        actionsPanel.add(sortPanel);

        sortComboBox.addActionListener(e -> {
            if (!loading) {
                loading = true;
                page = 1;

                // disable the tabs
                platformTabbedPane.setEnabled(false);

                String newSort = ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue();

                PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane
                    .getSelectedComponent();
                setSortOrder(selectedPanel.getSortFieldsDefaultOrder().getOrDefault(newSort, true) == true);

                load(true);
            }
        });

        ascendingSortButton.addActionListener(e -> {
            if (!loading) {
                loading = true;
                setSortOrder(true);
                load(true);
            }
        });

        descendingSortButton.addActionListener(e -> {
            if (!loading) {
                loading = true;
                setSortOrder(false);
                load(true);
            }
        });

        actionsPanel.add(spacer);

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    executeSearch();
                }
            }
        });
        searchField.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        searchField.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            searchField.setText("");
            executeSearch();
        });
        actionsPanel.add(searchField);

        addManuallyButton.addActionListener(e -> {
            PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane
                .getSelectedComponent();

            String id = DialogManager.okDialog().setTitle(GetText.tr("Add Pack By ID/Slug/URL"))
                .setContent(GetText.tr("Enter an ID/slug/url for a pack to add manually:")).showInput();

            if (id != null && !id.isEmpty()) {
                selectedPanel.addById(id);
            }
        });
        actionsPanel.add(addManuallyButton);

        add(actionsPanel, BorderLayout.NORTH);

        // content panel

        contentPanel.setLayout(new GridBagLayout());

        // platform message panel
        platformMessageJLabel.setForeground(Color.YELLOW);
        platformMessageJPanel.add(platformMessageJLabel, BorderLayout.CENTER);

        // scrollpane

        scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane
                    .getSelectedComponent();

                if (!loading && selectedPanel.hasPagination() && selectedPanel.hasMorePages()) {
                    int maxValue = scrollPane.getVerticalScrollBar().getMaximum()
                        - scrollPane.getVerticalScrollBar().getVisibleAmount();
                    int currentValue = scrollPane.getVerticalScrollBar().getValue();

                    if ((float) currentValue / (float) maxValue > 0.9f) {
                        loadMorePacks();
                    }
                }
            }
        });

        // tabs

        platformTabbedPane.setTabPlacement(SwingConstants.LEFT);

        int index = 0;

        platformTabbedPane.add(atlauncherPacksPanel);
        platformTabbedPane.setTabComponentAt(index++, new PacksBrowserTabTitlePanel("ATLauncher"));

        platformTabbedPane.add(atlauncherFeaturedPacksPanel);
        platformTabbedPane.setTabComponentAt(index++,
            new PacksBrowserTabTitlePanel("ATLauncher Featured", "atlauncher"));

        if (ConfigManager.getConfigItem("platforms.curseforge.modpacksEnabled", true) == true) {
            platformTabbedPane.add(curseForgePacksPanel);
            platformTabbedPane.setTabComponentAt(index++, new PacksBrowserTabTitlePanel("CurseForge"));
        }

        if (ConfigManager.getConfigItem("platforms.modpacksch.modpacksEnabled", true) == true) {
            platformTabbedPane.add(ftbPacksPanel);
            platformTabbedPane.setTabComponentAt(index++, new PacksBrowserTabTitlePanel("FTB"));
        }

        if (ConfigManager.getConfigItem("platforms.modrinth.modpacksEnabled", true) == true) {
            platformTabbedPane.add(modrinthPacksPanel);
            platformTabbedPane.setTabComponentAt(index++, new PacksBrowserTabTitlePanel("Modrinth"));
        }

        if (ConfigManager.getConfigItem("platforms.technic.modpacksEnabled", true) == true) {
            platformTabbedPane.add(technicPacksPanel);
            platformTabbedPane.setTabComponentAt(index++, new PacksBrowserTabTitlePanel("Technic"));
        }

        platformTabbedPane.addChangeListener(e -> {
            PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane
                .getSelectedComponent();

            // send analytics page view
            //TODO: Analytics.sendScreenView(selectedPanel.getPlatformName() + " Platform Packs");

            afterTabChange();
        });

        // add default state
        afterTabChange();

        add(platformTabbedPane, BorderLayout.CENTER);
    }

    private void afterTabChange() {
        // add the scrollPane to the newly selected panel
        PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane.getSelectedComponent();
        selectedPanel.add(platformMessageJPanel, BorderLayout.NORTH);
        selectedPanel.add(scrollPane, BorderLayout.CENTER);

        // clear search
        searchField.setText("");

        // reset page
        loading = true;
        page = 1;

        // disable the tabs
        platformTabbedPane.setEnabled(false);

        // remove minecraft version, category and sort values
        minecraftVersionComboBox.removeAllItems();
        categoriesComboBox.removeAllItems();
        sortComboBox.removeAllItems();

        // add in minecraft versions combo box items if the platform supports it
        if (selectedPanel.supportsMinecraftVersionFiltering()) {
            minecraftVersionComboBox.addItem(new ComboItem<String>(null, GetText.tr("All Versions")));

            List<VersionManifestVersion> versionsToShow = selectedPanel
                .getSupportedMinecraftVersionsForFiltering().size() != 0
                ? selectedPanel.getSupportedMinecraftVersionsForFiltering()
                : MinecraftManager
                .getFilteredMinecraftVersions(
                    selectedPanel.getSupportedMinecraftVersionTypesForFiltering());

            for (VersionManifestVersion mv : versionsToShow) {
                minecraftVersionComboBox.addItem(new ComboItem<String>(mv.id, mv.id));
            }
        }

        // add in categories combo box items if the platform supports it
        if (selectedPanel.hasCategories()) {
            new Thread(() -> {
                categoriesComboBox.addItem(new ComboItem<String>(null, GetText.tr("All Categories")));
                for (Map.Entry<String, String> entry : selectedPanel.getCategoryFields().entrySet()) {
                    categoriesComboBox.addItem(new ComboItem<String>(entry.getKey(), entry.getValue()));
                }
            }).start();
        }

        // add in sort combo box items if the platform supports it
        if (selectedPanel.hasSort()) {
            for (Map.Entry<String, String> entry : selectedPanel.getSortFields().entrySet()) {
                sortComboBox.addItem(new ComboItem<String>(entry.getKey(), entry.getValue()));
            }
        }

        if (selectedPanel.supportsSortOrder()) {
            String newSort = ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue();

            setSortOrder(selectedPanel.getSortFieldsDefaultOrder().getOrDefault(newSort, true) == true);
        } else {
            setSortOrder(true);
        }

        // hide minecraft version/sort/category if not needed
        searchField.setVisible(selectedPanel.supportsSearch());
        minecraftVersionPanel.setVisible(selectedPanel.supportsMinecraftVersionFiltering());
        categoriesPanel.setVisible(selectedPanel.hasCategories());
        sortPanel.setVisible(selectedPanel.hasSort());
        ascendingSortButton.setVisible(selectedPanel.supportsSortOrder() && !sortDescending);
        descendingSortButton.setVisible(selectedPanel.supportsSortOrder() && sortDescending);
        addManuallyButton.setVisible(selectedPanel.supportsManualAdding());

        String platformMessage = selectedPanel.getPlatformMessage();
        platformMessageJPanel.setVisible(platformMessage != null);
        platformMessageJLabel.setText(new HTMLBuilder().center().text(platformMessage).build());

        // load in the content for the platform
        load(true);
    }

    private void setSortOrder(boolean sortDescending) {
        this.sortDescending = sortDescending;
        ascendingSortButton.setVisible(!sortDescending);
        descendingSortButton.setVisible(sortDescending);
    }

    private void loadMorePacks() {
        PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane.getSelectedComponent();

        if (selectedPanel.hasPagination()) {
            loading = true;
            platformTabbedPane.setEnabled(false);
            page += 1;

            //TODO: Analytics.sendEvent(page, "Next", "Navigation", selectedPanel.getAnalyticsCategory());

            // load in the content for the platform
            new Thread(() -> {
                String minecraftVersion = null;
                if (selectedPanel.hasCategories()) {
                    minecraftVersion = ((ComboItem<String>) minecraftVersionComboBox.getSelectedItem()).getValue();
                }

                String category = null;
                if (selectedPanel.hasCategories()) {
                    category = ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue();
                }

                String sort = null;
                if (selectedPanel.hasSort()) {
                    sort = ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue();
                }

                // load in the content for the platform
                selectedPanel.loadMorePacks(contentPanel, minecraftVersion, category, sort, sortDescending,
                    searchField.getText(), page);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        loading = false;
                        platformTabbedPane.setEnabled(true);
                    }
                });

                revalidate();
                repaint();
            }).start();
        }
    }

    private void executeSearch() {
        PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane.getSelectedComponent();

        loading = true;
        page = 1;

        // disable the tabs
        platformTabbedPane.setEnabled(false);

        if (!searchField.getText().isEmpty()) {
            //TODO: Analytics.sendEvent(searchField.getText(), "Search", selectedPanel.getAnalyticsCategory());
        }

        // load in the content for the platform
        load(true);
    }

    private void load(boolean scrollToTop) {
        PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane.getSelectedComponent();

        new Thread(() -> {
            String minecraftVersion = null;
            if (selectedPanel.supportsMinecraftVersionFiltering()
                && minecraftVersionComboBox.getSelectedItem() != null) {
                minecraftVersion = ((ComboItem<String>) minecraftVersionComboBox.getSelectedItem()).getValue();
            }

            String category = null;
            if (selectedPanel.hasCategories() && categoriesComboBox.getSelectedItem() != null) {
                category = ((ComboItem<String>) categoriesComboBox.getSelectedItem()).getValue();
            }

            String sort = null;
            if (selectedPanel.hasSort() && sortComboBox.getSelectedItem() != null) {
                sort = ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue();
            }

            // load in the content for the platform
            selectedPanel.load(contentPanel, minecraftVersion, category, sort, sortDescending, searchField.getText(),
                page);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (scrollToTop) {
                        scrollPane.getVerticalScrollBar().setValue(0);
                    }

                    loading = false;
                    platformTabbedPane.setEnabled(true);
                }
            });

            revalidate();
            repaint();
        }).start();
    }

    public void reload() {
        platformTabbedPane.setSelectedIndex(0);
    }

    public void refresh() {
    }

    @Override
    public String getTitle() {
        return GetText.tr("Packs");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        // since this is the default, this is the main view name
        return "ATLauncher Platform Packs";
    }

    @Subscribe
    public final void onLocalizationChanged(final LocalizationEvent.LocalizationChangedEvent event) {
        categoriesLabel.setText(GetText.tr("Category:"));
        sortLabel.setText(GetText.tr("Sort:"));
        searchField.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
    }

    @Subscribe
    @OnSide(Side.UI)
    public final void onThemeChanged(final ThemeEvent.ThemeChangedEvent event) {
        ascendingSortButton.setIcon(Utils.getIconImage(App.THEME.getIconPath("ascending")));
        descendingSortButton.setIcon(Utils.getIconImage(App.THEME.getIconPath("descending")));
    }
}
