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

import javax.swing.JPanel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.card.InstanceCard;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.viewmodel.base.IInstancesTabViewModel;

public final class InstancesListPanel extends JPanel
        implements RelocalizationListener {

    private final IInstancesTabViewModel viewModel;
    private final InstancesTab parent;

    private final NilCard nilCard = new NilCard(
            getNilMessage(),
            new NilCard.Action[] {
                    NilCard.Action.createCreatePackAction(),
                    NilCard.Action.createDownloadPackAction()
            });

    public InstancesListPanel(final InstancesTab parent, final IInstancesTabViewModel viewModel) {
        super(new GridBagLayout());
        this.parent = parent;
        this.viewModel = viewModel;
        this.createView();
        RelocalizationManager.addListener(this);
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

        viewModel.getInstancesList().subscribe(instancesList -> {

            gbc.gridy = 0;
            removeAll();

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
        });
    }

    @Override
    public void onRelocalization() {
        this.nilCard.setMessage(getNilMessage());
        nilCard.setActions(new NilCard.Action[] {
                NilCard.Action.createCreatePackAction(),
                NilCard.Action.createDownloadPackAction()
        });
    }
}
