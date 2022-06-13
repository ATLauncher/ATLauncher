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
package com.atlauncher.gui.tabs.instances;

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.atlauncher.gui.dialogs.ImportInstanceDialog;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.utils.sort.InstanceSortingStrategies;
import com.atlauncher.utils.sort.InstanceSortingStrategy;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public final class InstancesNavigationPanel extends JPanel {
    private final InstancesTab parent;

    private final JButton importButton = new JButton(GetText.tr("Import"));
    private final InstancesSearchField searchField;
    private final JComboBox<InstanceSortingStrategy> sortingBox = new JComboBox<>(InstanceSortingStrategies.values());

    public InstancesNavigationPanel(final InstancesTab parent) {
        this.parent = parent;
        this.searchField = new InstancesSearchField(parent);
        this.sortingBox.setMaximumSize(new Dimension(190, 23));

        if (App.settings.defaultInstanceSorting != InstanceSortingStrategies.BY_NAME) {
            this.sortingBox.setSelectedItem(App.settings.defaultInstanceSorting);
        }

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(importButton);
        this.add(Box.createHorizontalGlue());
        this.add(searchField);
        this.add(Box.createHorizontalStrut(5));
        this.add(this.sortingBox);
        this.addListeners();

        AppEventBus.register(this);
    }

    private void addListeners() {
        // action listeners
        this.importButton.addActionListener((e) -> new ImportInstanceDialog());

        // item listeners
        this.sortingBox.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                this.parent.fireSortEvent(new InstancesSortEvent(e.getSource(), (InstanceSortingStrategy) e.getItem()));
            }
        });
    }

    @Subscribe
    public final void onLocalizationChanged(final LocalizationChangedEvent event) {
        this.importButton.setText(GetText.tr("Import"));
        this.searchField.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        this.sortingBox.repaint();
    }
}
