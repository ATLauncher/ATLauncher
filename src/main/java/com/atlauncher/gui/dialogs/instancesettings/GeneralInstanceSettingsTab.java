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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class GeneralInstanceSettingsTab extends JPanel {
    private final Instance instance;

    private JComboBox<ComboItem<String>> account;
    private JComboBox<ComboItem<Boolean>> enableDiscordIntegration;
    private JTextField initialJoinServerAddress;

    final ImageIcon HELP_ICON = Utils.getIconImage(App.THEME.getIconPath("question"));
    final ImageIcon ERROR_ICON = Utils.getIconImage(App.THEME.getIconPath("error"));
    final ImageIcon WARNING_ICON = Utils.getIconImage(App.THEME.getIconPath("warning"));

    final Border RESTART_BORDER = BorderFactory.createEmptyBorder(0, 0, 0, 5);

    final GridBagConstraints gbc = new GridBagConstraints();

    public GeneralInstanceSettingsTab(Instance instance) {
        this.instance = instance;

        setupComponents();
    }

    private void setupComponents() {
        setLayout(new GridBagLayout());

        // Account

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover accountLabel = new JLabelWithHover(GetText.tr("Account Override") + ":", HELP_ICON, GetText.tr(
                "Which account to use when launching this instance. Use Launcher Default will use whichever account is selected in the launcher."));

        add(accountLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        account = new JComboBox<>();
        account.addItem(new ComboItem<>(null, GetText.tr("Use Launcher Default")));
        AccountManager.getAccounts().stream()
                .forEach(a -> account.addItem(new ComboItem<>(a.username, a.minecraftUsername)));

        for (int i = 0; i < account.getItemCount(); i++) {
            ComboItem<String> item = account.getItemAt(i);

            if ((item.getValue() == null && instance.launcher.account == null)
                    || (item.getValue() != null && item.getValue().equalsIgnoreCase(instance.launcher.account))) {
                account.setSelectedIndex(i);
                break;
            }
        }

        add(account, gbc);

        // Enable Discord Integration

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableDiscordIntegrationLabel = new JLabelWithHover(
                GetText.tr("Enable Discord Integration") + "?", HELP_ICON,
                GetText.tr("This will enable showing which pack you're playing in Discord."));
        add(enableDiscordIntegrationLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableDiscordIntegration = new JComboBox<>();
        enableDiscordIntegration.addItem(new ComboItem<>(null, GetText.tr("Use Launcher Default")));
        enableDiscordIntegration.addItem(new ComboItem<>(true, GetText.tr("Yes")));
        enableDiscordIntegration.addItem(new ComboItem<>(false, GetText.tr("No")));

        enableDiscordIntegration.setEnabled(!OS.isArm());

        if (instance.launcher.enableDiscordIntegration == null) {
            enableDiscordIntegration.setSelectedIndex(0);
        } else if (instance.launcher.enableDiscordIntegration) {
            enableDiscordIntegration.setSelectedIndex(1);
        } else {
            enableDiscordIntegration.setSelectedIndex(2);
        }

        add(enableDiscordIntegration, gbc);

        // Join server on launch

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover initialJoinServerAddressLabel = new JLabelWithHover(
            GetText.tr("Join Server On Launch") + ":", HELP_ICON,
            GetText.tr(
                "Enter the server address if you want to join a Minecraft server when you launch the game, " +
                    "leave it empty if you don't want to join a server after launching the game."
            )
        );

        add(initialJoinServerAddressLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        initialJoinServerAddress = new JTextField(13);
        initialJoinServerAddress.putClientProperty("JTextField.showClearButton", true);
        initialJoinServerAddress.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            initialJoinServerAddress.setText("");
        });
        initialJoinServerAddress.setText(instance.launcher.initialJoinServerAddress);

        add(initialJoinServerAddress, gbc);
    }

    public void saveSettings() {
        this.instance.launcher.account = ((ComboItem<String>) account.getSelectedItem()).getValue();
        this.instance.launcher.enableDiscordIntegration = ((ComboItem<Boolean>) enableDiscordIntegration
                .getSelectedItem()).getValue();
        this.instance.launcher.initialJoinServerAddress = initialJoinServerAddress.getText();
    }

}
