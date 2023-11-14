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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.SwingUtilities;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
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
        super(new GridBagLayout());
        this.instancesTab = instancesTab;
        this.viewModel = viewModel;
        PerformanceManager.start("Displaying Instances");
    }

    private static String getNilMessage() {
        return new HTMLBuilder()
                .text(GetText.tr("There are no instances to display.<br/><br/>Install one from the Packs tab."))
                .build();
    }

    public void createView() {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.fill = GridBagConstraints.BOTH;

        addDisposable(viewModel.getInstancesList().subscribe(instancesList -> {
            gbc.gridy = 0;
            removeAll();
            // Broken, Issue is that because >this< is run on the event thread,
            // The "true" is sent async to the loading view.
            // Which occurs on the next event loop operation.
            // But "false" is sent after, so by the time the next event loop occurs the UI already is reset.
            // Doing a direct operation is also impossible it seems, as Swing requires >this< to finish before updating
            // the UI.
            // Truly a hell.
            viewModel.setIsLoading(true);

            if (instancesList.instances.isEmpty()) {
                this.add(this.nilCard, gbc);
            } else {
                instancesList.instances.forEach(instance -> {
                    this.add(
                        new InstanceCard(
                            instance.instance,
                            instance.hasUpdate,
                            instancesList.instanceTitleFormat
                        ),
                        gbc
                    );
                    gbc.gridy++;
                });
            }

            viewModel.setIsLoading(false); // Broken, reason above
            validate();
            repaint();

            // After repainting is done, let scroll view resume
            invokeLater(()-> instancesTab.setScroll(viewModel.getScroll()));
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
    protected void onShow() {
        createView();
    }

    @Override
    protected void onDestroy() {
        removeAll();
    }
}
