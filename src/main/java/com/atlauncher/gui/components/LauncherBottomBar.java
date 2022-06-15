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
package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.FileSystem;
import com.atlauncher.events.launcher.UpdateDataEvent;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.utils.OS;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class LauncherBottomBar extends BottomBar {
    private JButton openFolder;
    private JButton checkForUpdates;

    public LauncherBottomBar() {
        JPanel leftSide = new JPanel();
        leftSide.setLayout(new GridBagLayout());
        JPanel middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        createButtons();
        setupListeners();

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;

        gbc.insets = new Insets(0, 5, 0, 5);
        leftSide.add(new ToggleConsoleButton(), gbc);

        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.gridx++;
        leftSide.add(openFolder, gbc);

        gbc.gridx++;
        leftSide.add(checkForUpdates, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        middle.add(new AccountsDropDown(), gbc);

        add(leftSide, BorderLayout.WEST);
        add(middle, BorderLayout.CENTER);
        AppEventBus.register(this);
    }

    /**
     * Sets up the listeners on the buttons
     */
    private void setupListeners() {
        openFolder.addActionListener(e -> OS.openFileExplorer(FileSystem.BASE_DIR));
        checkForUpdates.addActionListener(e -> {
            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Checking For Updates"), 0,
                GetText.tr("Checking For Updates"), "Aborting Update Check!");
            dialog.addThread(new Thread(() -> {
                AppEventBus.postToDefault(UpdateDataEvent.of());
                App.launcher.updateData(true);
                dialog.close();
            }));
            dialog.start();
        });

        AppEventBus.register(this);
    }

    /**
     * Creates the JButton's for use in the bar
     */
    private void createButtons() {
        openFolder = new JButton(GetText.tr("Open Folder"));

        checkForUpdates = new JButton(GetText.tr("Check For Updates"));
        checkForUpdates.setName("checkForUpdates");
    }

    @Subscribe
    public final void onLocalizationChanged(final LocalizationChangedEvent event) {
        this.checkForUpdates.setText(GetText.tr("Check For Updates"));
        this.openFolder.setText(GetText.tr("Open Folder"));
    }
}
