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

import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.card.InstanceCard;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.utils.sort.InstanceSortingStrategy;
import org.mini2Dx.gettext.GetText;

import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class InstancesListPanel extends JPanel implements InstancesSortEventListener, InstancesSearchEventListener, RelocalizationListener {
    private static NilCard createNilCard(){
        return new NilCard(GetText.tr("There are no instances to display.\n\nInstall one from the Packs tab."));
    }

    private static Stream<Instance> createInstanceStream(final Pattern searchPattern, final InstanceSortingStrategy sortingStrategy){
        Stream<Instance> stream = InstanceManager.getInstancesSorted().stream();
        if(searchPattern != null){
            stream = stream.filter(createSearchFilter(searchPattern));
        }

        if(sortingStrategy != null){
            stream = stream.sorted(sortingStrategy);
        }
        return stream;
    }

    private static Predicate<Instance> createSearchFilter(final Pattern searchPattern){
        return (val)->searchPattern.matcher(val.launcher.name).find();
    }

    private final NilCard nilCard = createNilCard();
    private Pattern searchPattern;
    private InstanceSortingStrategy sortingStrategy;

    public InstancesListPanel(final InstancesTab parent){
        super(new GridBagLayout());
        this.setName("instancesPanel");
        this.loadInstances();
        parent.addSortEventListener(this);
        parent.addSearchEventListener(this);
        RelocalizationManager.addListener(this);
    }

    private void loadInstances(){
        this.removeAll();

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.fill = GridBagConstraints.BOTH;
        createInstanceStream(this.searchPattern, this.sortingStrategy)
            .forEach((val)->{
                this.add(new InstanceCard(val), gbc);
                gbc.gridy++;
            });

        if(this.getComponentCount() == 0){
            this.add(this.nilCard, gbc);
        }

        this.validate();
        this.repaint();
    }

    public Pattern getSearchPattern(){
        return this.searchPattern;
    }

    public void setSearchPattern(final Pattern searchPattern){
        this.searchPattern = searchPattern;
        this.loadInstances();
    }

    public InstanceSortingStrategy getSortingStrategy(){
        return this.sortingStrategy;
    }

    public void setSortingStrategy(final InstanceSortingStrategy sortingStrategy){
        this.sortingStrategy = sortingStrategy;
        this.loadInstances();
    }

    @Override
    public void onSearch(InstancesSearchEvent event){
        this.setSearchPattern(event.getSearchPattern());
    }

    @Override
    public void onSort(InstancesSortEvent event){
        this.setSortingStrategy(event.getStrategy());
    }

    @Override
    public void onRelocalization(){
        this.nilCard.setMessage(GetText.tr("There are no instances to display.\n\nInstall one from the Packs tab."));
    }
}