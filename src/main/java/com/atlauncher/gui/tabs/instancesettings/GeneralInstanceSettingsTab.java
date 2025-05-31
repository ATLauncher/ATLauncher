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
package com.atlauncher.gui.tabs.instancesettings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.QuickPlayOption;
import com.atlauncher.data.json.QuickPlay;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.ValidationUtils;

public class GeneralInstanceSettingsTab extends JPanel {
    private final Instance instance;

    private JComboBox<ComboItem<String>> account;

    // TODO: We might want to add a Swing component that hold both the label and the
    // input (text field or dropdown etc...)

    private JComboBox<ComboItem<QuickPlayOption>> quickPlayType;

    private JLabelWithHover quickPlayServerAddressLabel;
    private JTextField quickPlayServerAddress;

    private JLabelWithHover quickPlaySinglePlayerWorldLabel;
    private JComboBox<ComboItem<String>> quickPlaySinglePlayerWorld;

    private JLabelWithHover quickPlayRealmIdLabel;
    private JTextField quickPlayRealmId;

    final ImageIcon HELP_ICON = Utils.getIconImage(App.THEME.getIconPath("question"));

    final GridBagConstraints gbc = new GridBagConstraints();

    public GeneralInstanceSettingsTab(Instance instance) {
        this.instance = instance;

        setupComponents();
    }

    /**
     * Prepares [GridBagConstraints] for adding a new label component.
     * it should be called before adding the label to the panel
     */
    void prepareLabelConstraints() {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
    }

    /**
     * Prepares [GridBagConstraints] for adding a component after a label.
     * it should be called before adding the component to the panel
     */
    void prepareAfterLabelComponentConstraints() {
        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
    }

    private void setupComponents() {
        setLayout(new GridBagLayout());

        // Account

        prepareLabelConstraints();

        JLabelWithHover accountLabel = new JLabelWithHover(GetText.tr("Account Override") + ":", HELP_ICON, GetText.tr(
                "Which account to use when launching this instance. Use Launcher Default will use whichever account is selected in the launcher."));

        add(accountLabel, gbc);

        prepareAfterLabelComponentConstraints();
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

        // Quick play section

        QuickPlay quickPlay = instance.launcher.quickPlay;

        // Quick Play options

        prepareLabelConstraints();

        // Quick Play Feature components
        JLabelWithHover quickPlayTypeLabel = new JLabelWithHover(
                GetText.tr("Quick Play Type") + ":", HELP_ICON,
                GetText.tr(
                        "Select the type of the Quick Play feature, default to disabled."));

        add(quickPlayTypeLabel, gbc);

        prepareAfterLabelComponentConstraints();

        quickPlayType = new JComboBox<>();
        Arrays.stream(QuickPlayOption.compatibleValues(instance))
                .forEach(option -> quickPlayType.addItem(new ComboItem<>(option, option.label)));
        quickPlayType.setSelectedIndex(
                Arrays.asList(QuickPlayOption.compatibleValues(instance))
                        .indexOf(quickPlay.getSelectedQuickPlayOption()));

        // Code that is responsible for changing the input
        quickPlayType.addActionListener(e -> showInputForTheSelectedQuickPlayOption());

        add(quickPlayType, gbc);

        // Quick play server address

        // TODO: Allow to select the list of the servers in the game using dropdown as a
        // value for this text input

        prepareLabelConstraints();

        quickPlayServerAddressLabel = new JLabelWithHover(
                GetText.tr("Server address") + ":", HELP_ICON,
                GetText.tr(
                        "The server address that is used to connect to Minecraft server in multiplayer after" +
                                " launching the game."));

        add(quickPlayServerAddressLabel, gbc);

        prepareAfterLabelComponentConstraints();
        quickPlayServerAddress = new JTextField(13);
        quickPlayServerAddress.putClientProperty("JTextField.showClearButton", true);
        quickPlayServerAddress.putClientProperty("JTextField.clearCallback",
                (Runnable) () -> quickPlayServerAddress.setText(""));
        quickPlayServerAddress.setText(quickPlay.serverAddress);

        add(quickPlayServerAddress, gbc);

        // Quick play select single player world dropdown

        prepareLabelConstraints();
        quickPlaySinglePlayerWorldLabel = new JLabelWithHover(
                GetText.tr("Single Player World") + ":", HELP_ICON,
                GetText.tr(
                        "Select the single player world to load after launching the game."));
        add(quickPlaySinglePlayerWorldLabel, gbc);

        prepareAfterLabelComponentConstraints();
        quickPlaySinglePlayerWorld = new JComboBox<>();
        List<String> worldNames = instance.getSinglePlayerWorldNamesFromFilesystem();
        worldNames.forEach(saveName -> quickPlaySinglePlayerWorld.addItem(new ComboItem<>(saveName, saveName)));

        if (!worldNames.isEmpty()) {
            final int selectedWorldFolderNameIndex = worldNames.indexOf(quickPlay.worldName);
            quickPlaySinglePlayerWorld.setSelectedIndex(
                    selectedWorldFolderNameIndex != -1 ? selectedWorldFolderNameIndex : 0);
        }

        add(quickPlaySinglePlayerWorld, gbc);

        prepareLabelConstraints();
        quickPlayRealmIdLabel = new JLabelWithHover(
                GetText.tr("Minecraft Realm") + ":", HELP_ICON,
                GetText.tr(
                        "Type the id of the realm to join after launching the game."));
        add(quickPlayRealmIdLabel, gbc);

        // TODO: We might want to make this as dropdown to all the realms
        prepareAfterLabelComponentConstraints();
        quickPlayRealmId = new JTextField(13);
        quickPlayRealmId.putClientProperty("JTextField.showClearButton", true);
        quickPlayRealmId.putClientProperty("JTextField.clearCallback", (Runnable) () -> quickPlayRealmId.setText(""));
        quickPlayRealmId.setText(quickPlay.realmId);

        add(quickPlayRealmId, gbc);

        // Show only the input for the selected quick play type
        showInputForTheSelectedQuickPlayOption();
    }

    /**
     * A helper method to set the correct visibility for all the quick play inputs
     */
    public void showInputForTheSelectedQuickPlayOption() {
        QuickPlayOption quickPlayOption = ((ComboItem<QuickPlayOption>) quickPlayType.getSelectedItem()).getValue();
        switch (quickPlayOption) {
            case disabled:
                // Hide all
                setQuickPlayInputsVisibility(false, false, false);
                break;
            case multiPlayer:
                // Only show the server address input field
                setQuickPlayInputsVisibility(true, false, false);
                break;
            case singlePlayer:
                // Only show the select world dropdown
                setQuickPlayInputsVisibility(false, true, false);
                break;
            case realm:
                setQuickPlayInputsVisibility(false, false, true);
                break;
        }
    }

    /**
     * A helper method to set the visibility for different quick play inputs
     * (multiplayer, single player, realms)
     */
    public void setQuickPlayInputsVisibility(
            boolean serverAddress,
            boolean singlePlayerWorld,
            boolean realmId) {
        quickPlayServerAddressLabel.setVisible(serverAddress);
        quickPlayServerAddress.setVisible(serverAddress);
        quickPlaySinglePlayerWorldLabel.setVisible(singlePlayerWorld);
        quickPlaySinglePlayerWorld.setVisible(singlePlayerWorld);
        quickPlayRealmIdLabel.setVisible(realmId);
        quickPlayRealmId.setVisible(realmId);
    }

    public boolean isValidQuickPlayOptionValue() {
        QuickPlayOption quickPlayOption = ((ComboItem<QuickPlayOption>) quickPlayType.getSelectedItem()).getValue();
        switch (quickPlayOption) {
            case disabled:
                return true;
            case singlePlayer:
                if (quickPlaySinglePlayerWorld.getSelectedItem() == null) {
                    DialogManager.okDialog().setTitle(GetText.tr("Invalid Input"))
                            .setContent(new HTMLBuilder().center()
                                    .text(GetText.tr("You don't have any single player worlds yet on this instance."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return false;
                }
                return true;
            case multiPlayer:
                if (quickPlayServerAddress.getText().isEmpty()) {
                    DialogManager.okDialog().setTitle(GetText.tr("Invalid Input"))
                            .setContent(new HTMLBuilder().center()
                                    .text(GetText.tr("The server address is empty."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return false;
                }
                if (!ValidationUtils.isValidMinecraftServerAddress(quickPlayServerAddress.getText())) {
                    DialogManager.okDialog().setTitle(GetText.tr("Invalid Input"))
                            .setContent(new HTMLBuilder().center()
                                    .text(GetText.tr("The entered server address is invalid."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return false;
                }
                return true;
            case realm:
                if (quickPlayRealmId.getText().isEmpty()) {
                    DialogManager.okDialog().setTitle(GetText.tr("Invalid Input"))
                            .setContent(new HTMLBuilder().center()
                                    .text(GetText.tr("The realm id is empty."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return false;
                }
                return true;
        }
        return true;
    }

    public void saveSettings() {
        this.instance.launcher.account = ((ComboItem<String>) account.getSelectedItem()).getValue();
        QuickPlayOption quickPlayOption = ((ComboItem<QuickPlayOption>) quickPlayType.getSelectedItem()).getValue();
        this.instance.launcher.quickPlay = new QuickPlay(
                quickPlayOption == QuickPlayOption.multiPlayer ? quickPlayServerAddress.getText() : null,
                quickPlayOption == QuickPlayOption.singlePlayer
                        ? ((ComboItem<String>) quickPlaySinglePlayerWorld.getSelectedItem()).getValue()
                        : null,
                quickPlayOption == QuickPlayOption.realm ? quickPlayRealmId.getText() : null);
    }

}
