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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.stream.Collectors;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.gui.card.InstanceCard;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.viewmodel.base.IInstancesTabViewModel;

public final class InstancesListPanel extends HierarchyPanel
        implements RelocalizationListener {

    private final InstancesTab instancesTab;
    private final IInstancesTabViewModel viewModel;

    private final NilCard nilCard = new NilCard(
            getNilMessage(),
            new NilCard.Action[] {
                    NilCard.Action.createCreatePackAction(),
                    NilCard.Action.createDownloadPackAction()
            });

    public InstancesListPanel(InstancesTab instancesTab, final IInstancesTabViewModel viewModel) {
        super(new FlowLayout(FlowLayout.LEFT, 16, 16)); // Create a grid with 3
        // columns and gaps
        this.instancesTab = instancesTab;
        this.viewModel = viewModel;
        PerformanceManager.start("Displaying Instances");
    }

    private static String getNilMessage() {
        return new HTMLBuilder()
                .text(GetText.tr("There are no instances to display.<br/><br/>Install one from the Packs tab."))
                .build();
    }

    @Override
    protected void onShow() {
        addDisposable(viewModel.getInstancesList()
                .map(instancesList -> {
                    viewModel.setIsLoading(true);
                    return instancesList.instances.stream().map(instance -> new InstanceCard(
                            instance.instance,
                            instance.hasUpdate,
                            instancesList.instanceTitleFormat)).collect(Collectors.toList());
                }).subscribe(instances -> {
                    removeAll();

                    if (instances.isEmpty()) {
                        this.add(this.nilCard);
                    } else {
                        PerformanceManager.start("Render cards");

                        instances.forEach(instance -> {
                            instance.setPreferredSize(new Dimension(150, 150)); // Ensure square shape
                            this.add(instance);
                        });

                        validate();
                        repaint();
                        PerformanceManager.end("Render cards");
                    }

                    viewModel.setIsLoading(false);

                    // After repainting is done, let scroll view resume
                    invokeLater(() -> instancesTab.setScroll(viewModel.getScroll()));
                    PerformanceManager.end("Displaying Instances");
                }));
    }

    @Override
    public void onRelocalization() {
        this.nilCard.setMessage(getNilMessage());
        nilCard.setActions(new NilCard.Action[] {
                NilCard.Action.createCreatePackAction(),
                NilCard.Action.createDownloadPackAction()
        });
    }

    @Override
    protected void createViewModel() {
    }

    @Override
    protected void onDestroy() {
        removeAll();
    }
}
