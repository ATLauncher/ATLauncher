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
package com.atlauncher.gui.tabs.instances;

import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.dialogs.ImportInstanceDialog;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.sort.InstanceSortingStrategies;
import com.atlauncher.utils.sort.InstanceSortingStrategy;
import org.mini2Dx.gettext.GetText;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.event.ItemEvent;

public final class InstancesNavigationPanel extends JPanel implements RelocalizationListener{
    private final InstancesTab parent;

    private final JButton importButton = new JButton(GetText.tr("Import"));
    private final JButton searchButton = new JButton(GetText.tr("Search"));
    private final JButton clearButton = new JButton(GetText.tr("Clear"));
    private final InstancesSearchField searchField;
    private final JComboBox<InstanceSortingStrategy> sortingBox = new JComboBox<>(InstanceSortingStrategies.values());

    public InstancesNavigationPanel(final InstancesTab parent){
        this.parent = parent;
        this.searchField = new InstancesSearchField(parent);

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(importButton);
        this.add(Box.createHorizontalGlue());
        this.add(searchField);
        this.add(Box.createHorizontalStrut(5));
        this.add(this.searchButton);
        this.add(Box.createHorizontalStrut(5));
        this.add(this.clearButton);
        this.add(Box.createHorizontalStrut(5));
        this.add(this.sortingBox);
        this.addListeners();

        RelocalizationManager.addListener(this);
    }

    private void addListeners(){
        // action listeners
        this.importButton.addActionListener((e)->new ImportInstanceDialog());
        this.searchButton.addActionListener((e)->{
            Analytics.sendEvent(searchField.getText(), "Search", "Instance");
            this.parent.fireSearchEvent(new InstancesSearchEvent(e.getSource(), this.searchField.getSearchPattern()));
        });
        this.clearButton.addActionListener((e)->{
            this.searchField.setText("");
            this.parent.fireSearchEvent(new InstancesSearchEvent(e.getSource(), null));
        });

        // item listeners
        this.sortingBox.addItemListener((e)->{
            if(e.getStateChange() == ItemEvent.SELECTED){
                this.parent.fireSortEvent(new InstancesSortEvent(e.getSource(), (InstanceSortingStrategy)e.getItem()));
            }
        });
    }

    @Override
    public void onRelocalization(){
        this.importButton.setText(GetText.tr("Import"));
        this.clearButton.setText(GetText.tr("Clear"));
        this.searchButton.setText(GetText.tr("Search"));
    }
}