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
package com.atlauncher.gui.dialogs.instancesettings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class CommandsInstanceSettingsTab extends JPanel {
    private final Instance instance;

    private final JComboBox<ComboItem<Boolean>> enableCommands;
    private final JTextField preLaunchCommand;
    private final JTextField postExitCommand;
    private final JTextField wrapperCommand;

    final ImageIcon HELP_ICON = Utils.getIconImage(App.THEME.getIconPath("question"));
    final ImageIcon ERROR_ICON = Utils.getIconImage(App.THEME.getIconPath("error"));
    final ImageIcon WARNING_ICON = Utils.getIconImage(App.THEME.getIconPath("warning"));

    final Border RESTART_BORDER = BorderFactory.createEmptyBorder(0, 0, 0, 5);

    final GridBagConstraints gbc = new GridBagConstraints();

    public CommandsInstanceSettingsTab(Instance instance) {
        this.instance = instance;

        setLayout(new GridBagLayout());

        // Enable commands

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableCommandsLabel = new JLabelWithHover(GetText.tr("Enable commands") + "?", HELP_ICON,
                GetText.tr("This allows you to turn launch/exit commands on or off."));
        add(enableCommandsLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableCommands = new JComboBox<>();
        enableCommands.addItem(new ComboItem<>(null, GetText.tr("Use Launcher Default")));
        enableCommands.addItem(new ComboItem<>(true, GetText.tr("Yes")));
        enableCommands.addItem(new ComboItem<>(false, GetText.tr("No")));

        if (instance.launcher.enableCommands == null) {
            enableCommands.setSelectedIndex(0);
        } else if (instance.launcher.enableCommands) {
            enableCommands.setSelectedIndex(1);
        } else {
            enableCommands.setSelectedIndex(2);
        }

        add(enableCommands, gbc);

        // Pre-launch command
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover preLaunchCommandLabel = new JLabelWithHover(GetText.tr("Pre-launch command") + ":", HELP_ICON,
                GetText.tr(
                        "This command will be run before the instance launches. The game will not run until the command has finished."));
        add(preLaunchCommandLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        preLaunchCommand = new JTextField(App.settings.preLaunchCommand, 32);
        preLaunchCommand.setPreferredSize(new Dimension(516, 24));

        if (this.instance.launcher.preLaunchCommand != null) {
            preLaunchCommand.setText(this.instance.launcher.preLaunchCommand);
        }

        add(preLaunchCommand, gbc);

        // Post-exit command
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover postExitCommandLabel = new JLabelWithHover(GetText.tr("Post-exit command") + ":", HELP_ICON,
                GetText.tr(
                        "This command will be run after the instance exits. It will run even if the instance is killed or if it crashes and exits."));
        add(postExitCommandLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        postExitCommand = new JTextField(App.settings.postExitCommand, 32);
        postExitCommand.setPreferredSize(new Dimension(516, 24));

        if (this.instance.launcher.postExitCommand != null) {
            postExitCommand.setText(this.instance.launcher.postExitCommand);
        }

        add(postExitCommand, gbc);

        // wrapper command
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover wrapperCommandLabel = new JLabelWithHover(GetText.tr("Wrapper command") + ":", HELP_ICON,
                GetText.tr(
                        "Wrapper command allow launcher using an extra wrapper program (like 'prime-run' on Linux)\nUse %command% to substitute launch command\n%\"command\"% to substitute launch as a whole string (like 'bash -c' on Linux)"));
        add(wrapperCommandLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        wrapperCommand = new JTextField(App.settings.wrapperCommand, 32);
        wrapperCommand.setPreferredSize(new Dimension(516, 24));

        if (this.instance.launcher.wrapperCommand != null) {
            wrapperCommand.setText(this.instance.launcher.wrapperCommand);
        }

        add(wrapperCommand, gbc);

        // Parameter Information
        gbc.gridx = 0;
        gbc.gridy++;
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

        enableCommands.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Boolean enabled = ((ComboItem<Boolean>) e.getItem()).getValue();

                toggleFields(enabled);
            }
        });

        toggleFields(instance.launcher.enableCommands);
    }

    private void toggleFields(Boolean enabled) {
        boolean fieldsEnabled = enabled != null && enabled;

        preLaunchCommand.setEnabled(fieldsEnabled);
        postExitCommand.setEnabled(fieldsEnabled);
        wrapperCommand.setEnabled(fieldsEnabled);
    }

    private String nullIfEmpty(String str) {
        if (str.isEmpty())
            return null;
        else
            return str;
    }

    public void saveSettings() {
        this.instance.launcher.enableCommands = ((ComboItem<Boolean>) enableCommands.getSelectedItem()).getValue();
        this.instance.launcher.preLaunchCommand = nullIfEmpty(preLaunchCommand.getText());
        this.instance.launcher.postExitCommand = nullIfEmpty(postExitCommand.getText());
        this.instance.launcher.wrapperCommand = nullIfEmpty(wrapperCommand.getText());
    }

}
