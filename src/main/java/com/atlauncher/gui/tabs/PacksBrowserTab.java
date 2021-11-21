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
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.panels.packbrowser.ATLauncherPacksPanel;
import com.atlauncher.gui.panels.packbrowser.CurseForgePacksPanel;
import com.atlauncher.gui.panels.packbrowser.FTBPacksPanel;
import com.atlauncher.gui.panels.packbrowser.ModrinthPacksPanel;
import com.atlauncher.gui.panels.packbrowser.PackBrowserPlatformPanel;
import com.atlauncher.gui.panels.packbrowser.PacksBrowserTabTitlePanel;
import com.atlauncher.gui.panels.packbrowser.TechnicPacksPanel;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ComboItem;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public final class PacksBrowserTab extends JPanel implements Tab, RelocalizationListener {
    private final JPanel actionsPanel = new JPanel();
    private final JPanel categoriesPanel = new JPanel();
    private final JLabel categoriesLabel = new JLabel(GetText.tr("Category:"));
    private final JComboBox<ComboItem<Integer>> categoriesComboBox = new JComboBox<>();
    private final JPanel sortPanel = new JPanel();
    private final JLabel sortLabel = new JLabel(GetText.tr("Sort:"));
    private final JComboBox<ComboItem<String>> sortComboBox = new JComboBox<>();
    private final JPanel spacer = new JPanel();
    private final JTextField searchField = new JTextField(16);
    private final JButton searchButton = new JButton(GetText.tr("Search"));
    private final JButton clearButton = new JButton(GetText.tr("Clear"));

    private final JTabbedPane platformTabbedPane = new JTabbedPane();
    private final PackBrowserPlatformPanel atlauncherPacksPanel = new ATLauncherPacksPanel();
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
        setName("packsBrowserPanel");
        RelocalizationManager.addListener(this);

        initComponents();
    }

    private void initComponents() {
        actionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel
                .setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));

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
        actionsPanel.add(sortPanel);

        sortComboBox.addActionListener(e -> {
            if (!loading) {
                loading = true;
                page = 1;

                // disable the tabs
                platformTabbedPane.setEnabled(false);

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
        actionsPanel.add(searchField);

        searchButton.addActionListener(e -> {
            executeSearch();
        });
        actionsPanel.add(searchButton);

        clearButton.addActionListener(e -> {
            searchField.setText("");
            executeSearch();
        });
        actionsPanel.add(clearButton);

        add(actionsPanel, BorderLayout.NORTH);

        // content panel

        contentPanel.setLayout(new GridBagLayout());

        // scrollpane

        scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!loading) {
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
            Analytics.sendScreenView(selectedPanel.getPlatformName() + " Platform Packs");

            afterTabChange();
        });

        // add default state
        afterTabChange();

        add(platformTabbedPane, BorderLayout.CENTER);
    }

    private void afterTabChange() {
        // add the scrollPane to the newly selected panel
        PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane.getSelectedComponent();
        selectedPanel.add(scrollPane, BorderLayout.CENTER);

        // clear search
        searchField.setText("");

        // reset page
        loading = true;
        page = 1;

        // disable the tabs
        platformTabbedPane.setEnabled(false);

        // remove category and sort values
        categoriesComboBox.removeAllItems();
        sortComboBox.removeAllItems();

        // add in categories combo box items if the platform supports it
        if (selectedPanel.hasCategories()) {
            new Thread(() -> {
                categoriesComboBox.addItem(new ComboItem<Integer>(null, GetText.tr("All Categories")));
                for (Map.Entry<Integer, String> entry : selectedPanel.getCategoryFields().entrySet()) {
                    categoriesComboBox.addItem(new ComboItem<Integer>(entry.getKey(), entry.getValue()));
                }
            }).start();
        }

        // add in sort combo box items if the platform supports it
        if (selectedPanel.hasSort()) {
            for (Map.Entry<String, String> entry : selectedPanel.getSortFields().entrySet()) {
                sortComboBox.addItem(new ComboItem<String>(entry.getKey(), entry.getValue()));
            }
        }

        // hide sort/category if not needed
        categoriesPanel.setVisible(selectedPanel.hasCategories());
        sortPanel.setVisible(selectedPanel.hasSort());

        // load in the content for the platform
        load(true);
    }

    private void loadMorePacks() {
        PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane.getSelectedComponent();

        if (selectedPanel.hasPagination()) {
            loading = true;
            platformTabbedPane.setEnabled(false);
            page += 1;

            Analytics.sendEvent(page, "Next", "Navigation", selectedPanel.getAnalyticsCategory());

            // load in the content for the platform
            new Thread(() -> {
                Integer category = null;
                if (selectedPanel.hasCategories()) {
                    category = ((ComboItem<Integer>) categoriesComboBox.getSelectedItem()).getValue();
                }

                String sort = null;
                if (selectedPanel.hasSort()) {
                    sort = ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue();
                }

                // load in the content for the platform
                selectedPanel.loadMorePacks(contentPanel, category, sort, searchField.getText(), page);

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
            Analytics.sendEvent(searchField.getText(), "Search", selectedPanel.getAnalyticsCategory());
        }

        // load in the content for the platform
        load(true);
    }

    private void load(boolean scrollToTop) {
        PackBrowserPlatformPanel selectedPanel = (PackBrowserPlatformPanel) platformTabbedPane.getSelectedComponent();

        new Thread(() -> {
            Integer category = null;
            if (selectedPanel.hasCategories()) {
                category = ((ComboItem<Integer>) categoriesComboBox.getSelectedItem()).getValue();
            }

            String sort = null;
            if (selectedPanel.hasSort()) {
                sort = ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue();
            }

            // load in the content for the platform
            selectedPanel.load(contentPanel, category, sort, searchField.getText(), page);

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

    @Override
    public void onRelocalization() {
        searchButton.setText(GetText.tr("Search"));
        clearButton.setText(GetText.tr("Clear"));
        categoriesLabel.setText(GetText.tr("Category:"));
        sortLabel.setText(GetText.tr("Sort:"));
    }
}
