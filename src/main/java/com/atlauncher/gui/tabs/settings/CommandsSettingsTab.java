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
package com.atlauncher.gui.tabs.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.*;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import com.atlauncher.listener.DelayedSavingKeyListener;
import com.atlauncher.viewmodel.base.settings.ICommandsSettingsViewModel;
import com.atlauncher.viewmodel.impl.settings.CommandsSettingsViewModel;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.gui.components.JLabelWithHover;

public class CommandsSettingsTab extends AbstractSettingsTab {
    private final JTextField preLaunchCommand;
    private final JTextField postExitCommand;
    private final JTextField wrapperCommand;

    private final JCheckBox enableCommands;
    private final JLabel enableCommandsLabel;
    private final JLabelWithHover preLaunchCommandLabel;
    private final JLabelWithHover postExitCommandLabel;
    private final JLabelWithHover wrapperCommandLabel;


    public CommandsSettingsTab() {
        ICommandsSettingsViewModel viewModel = new CommandsSettingsViewModel();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        // region Enable Checkbox
        // TODO Relocalization support for below
        enableCommandsLabel = new JLabelWithHover(GetText.tr("Enable commands") + "?", HELP_ICON,
            GetText.tr("This allows you to turn launch/exit commands on or off."));
        add(enableCommandsLabel, gbc);

        nextColumn();
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;

        enableCommands = new JCheckBox();
        enableCommands.addItemListener(e -> {
            viewModel.setEnableCommands(e.getStateChange() == ItemEvent.SELECTED);
        });
        add(enableCommands, gbc);

        nextRow();
        // endregion

        // region Pre-launch command
        // TODO Relocalization support for below
        preLaunchCommandLabel = new JLabelWithHover(GetText.tr("Pre-launch command") + ":", HELP_ICON,
            GetText.tr(
                "This command will be run before the instance launches. The game will not run until the command has finished."));
        add(preLaunchCommandLabel, gbc);

        nextColumn();

        preLaunchCommand = new JTextField(App.settings.preLaunchCommand, 32);
        preLaunchCommand.addKeyListener(new DelayedSavingKeyListener(
            500,
            () -> viewModel.setPreLaunchCommand(preLaunchCommand.getText()),
            viewModel::setPreLaunchCommandPending));
        viewModel.addOnPreLaunchCommandChanged(preLaunchCommand::setText);
        preLaunchCommand.setPreferredSize(new Dimension(516, 24));
        add(preLaunchCommand, gbc);

        nextRow();
        // endregion

        // region Post-exit command
        // TODO Relocalization support for below
        postExitCommandLabel = new JLabelWithHover(GetText.tr("Post-exit command") + ":", HELP_ICON,
            GetText.tr(
                "This command will be run after the instance exits. It will run even if the instance is killed or if it crashes and exits."));
        add(postExitCommandLabel, gbc);

        nextColumn();

        postExitCommand = new JTextField(App.settings.postExitCommand, 32);
        postExitCommand.addKeyListener(new DelayedSavingKeyListener(
            500,
            () -> viewModel.setPostExitCommand(postExitCommand.getText()),
            viewModel::setPostExitCommandPending
        ));
        viewModel.addOnPostExitCommandChanged(postExitCommand::setText);
        postExitCommand.setPreferredSize(new Dimension(516, 24));
        add(postExitCommand, gbc);

        nextRow();
        // endregion

        // region Wrapper command
        // TODO Relocalization support for below
        wrapperCommandLabel = new JLabelWithHover(GetText.tr("Wrapper command") + ":", HELP_ICON,
            GetText.tr(
                "Wrapper command allow launcher using an extra wrapper program (like 'prime-run' on Linux)\nUse %command% to substitute launch command\n%\"command\"% to substitute launch as a whole string (like 'bash -c' on Linux)"));
        add(wrapperCommandLabel, gbc);

        nextColumn();

        wrapperCommand = new JTextField(App.settings.wrapperCommand, 32);
        wrapperCommand.setPreferredSize(new Dimension(516, 24));
        wrapperCommand.addKeyListener(new DelayedSavingKeyListener(
            500,
            () -> viewModel.setWrapperCommand(wrapperCommand.getText()),
            viewModel::setWrapperCommandPending));
        viewModel.addOnWrapperCommandChanged(wrapperCommand::setText);
        add(wrapperCommand, gbc);

        nextRow();
        // endregion

        // region Information text pane
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;

        JTextPane parameterInformation = new JTextPane();
        parameterInformation
            .setText(GetText.tr("Commands will be run in the directory of the instance that is launched/exited.")
                + System.lineSeparator() + GetText.tr("The following variables are available for each command")
                + ":" + System.lineSeparator() + "$INST_NAME: " + GetText.tr("The name of the instance")
                + System.lineSeparator() + "$INST_ID: "
                + GetText.tr("The name of the instance's root directory") + System.lineSeparator()
                + "$INST_DIR: " + GetText.tr("The absolute path to the instance directory")
                + System.lineSeparator() + "$INST_MC_DIR: " + GetText.tr("Alias for") + " $INST_DIR"
                + System.lineSeparator() + "$INST_JAVA: "
                + GetText.tr("The absolute path to the java executable used for launch")
                + System.lineSeparator() + "$INST_JAVA_ARGS: "
                + GetText.tr("The JVM parameters used for launch") + System.lineSeparator());
        parameterInformation.setEditable(false);

        add(parameterInformation, gbc);
        // endregion

        viewModel.addOnEnableCommandsChanged(enabled -> {
            enableCommands.setSelected(enabled);
            preLaunchCommand.setEnabled(enabled);
            postExitCommand.setEnabled(enabled);
            wrapperCommand.setEnabled(enabled);
        });
    }

    private void nextRow() {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
    }

    private void nextColumn() {
        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
    }

    @Override
    public String getTitle() {
        return GetText.tr("Commands");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Commands";
    }
}
