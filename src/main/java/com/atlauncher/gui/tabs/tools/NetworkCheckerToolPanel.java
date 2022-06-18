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
package com.atlauncher.gui.tabs.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;

public class NetworkCheckerToolPanel extends AbstractToolPanel implements ActionListener {
    private static final Logger LOG = LogManager.getLogger(NetworkCheckerToolPanel.class);
    private final IToolsViewModel viewModel;

    public NetworkCheckerToolPanel(IToolsViewModel viewModel) {
        super(GetText.tr("Network Checker"));
        this.viewModel = viewModel;

        JLabel INFO_LABEL = new JLabel(new HTMLBuilder().center().split(70)
            .text(GetText.tr(
                "This tool does various tests on your network and determines any issues that may pop up with "
                    + "connecting to our file servers and to other servers."))
            .build());
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(this);
        viewModel.onCanRunNetworkCheckerChanged(LAUNCH_BUTTON::setEnabled);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Network Checker"))
            .setContent(new HTMLBuilder().center().split(75).text(GetText.tr(
                    "Please note that the data from this tool is sent to ATLauncher so we can diagnose possible issues in your setup. This test may take up to 10 minutes or longer to complete and you will be unable to do anything while it's running. Please also keep in mind that this test will use some of your bandwidth, it will use approximately 100MB.<br/><br/>Do you wish to continue?"))
                .build())
            .setType(DialogManager.INFO).show();

        if (ret == 0) {
            final ProgressDialog<Boolean> dialog = new ProgressDialog<>(GetText.tr("Network Checker"),
                17 + viewModel.hostsLength(), GetText.tr("Network Checker Running. Please Wait!"),
                "Network Checker Tool Cancelled!");
            dialog.addThread(new Thread(() -> {
                viewModel.runNetworkChecker(
                    taskComplete -> dialog.doneTask(),
                    fail -> dialog.setReturnValue(false),
                    success -> dialog.setReturnValue(true)
                );

                dialog.close();
            }));
            dialog.start();

            if (dialog.getReturnValue() == null || !dialog.getReturnValue()) {
                LOG.error("Network Test failed to run!");
            } else {
                LOG.info("Network Test ran and submitted to {}!", Constants.LAUNCHER_NAME);

                DialogManager.okDialog().setTitle(GetText.tr("Network Checker"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "The network checker tool has completed and the data sent off to ATLauncher.<br/><br/>Thanks for your input to help understand and fix network related issues."))
                        .build())
                    .setType(DialogManager.INFO).show();
            }
        }
    }
}
