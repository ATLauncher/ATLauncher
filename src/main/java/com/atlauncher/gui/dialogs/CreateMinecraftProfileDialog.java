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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.MojangAPIUtils;
import com.atlauncher.utils.Utils;

public class CreateMinecraftProfileDialog extends JDialog {
    private final Pattern VALID_PROFILE_NAME = Pattern.compile("[a-zA-Z0-9_]{3,16}");

    private JTextField profileName;

    private final String accessToken;

    public CreateMinecraftProfileDialog(String accessToken) {
        this(accessToken, App.launcher.getParent());
    }

    public CreateMinecraftProfileDialog(String accessToken, Window parent) {
        super(parent, GetText.tr("Create Minecraft Profile"), ModalityType.DOCUMENT_MODAL);

        this.accessToken = accessToken;

        Analytics.sendScreenView("Create Minecraft Profile Dialog");

        setSize(320, 150);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/icon.png"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        setupComponents();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        setVisible(true);
    }

    private void setupComponents() {
        // Top Panel Stuff
        JPanel top = new JPanel();
        top.add(new JLabel(GetText.tr("Create Minecraft Profile")));

        // Middle Panel Stuff
        JPanel middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel instanceNameLabel = new JLabel(GetText.tr("Username") + ": ");
        middle.add(instanceNameLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        profileName = new JTextField(16);
        profileName.setText("");
        middle.add(profileName, gbc);

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        JButton createButton = new JButton(GetText.tr("Create"));
        createButton.addActionListener(e -> {
            ProgressDialog<Boolean> progressDialog = new ProgressDialog<>(GetText.tr("Creating Minecraft Profile"), 0,
                    GetText.tr("Creating Minecraft Profile"));
            progressDialog.addThread(new Thread(() -> {
                if (!VALID_PROFILE_NAME.matcher(profileName.getText()).matches()) {
                    DialogManager.okDialog().setParent(CreateMinecraftProfileDialog.this).setTitle(GetText.tr("Error"))
                            .setContent(
                                    GetText.tr(
                                            "Invalid profile name, it must contain only alphanumeric characters and underscore, and be 3 to 16 characters long."))
                            .setType(DialogManager.ERROR).show();
                    progressDialog.setReturnValue(false);
                    progressDialog.close();
                    return;
                }

                if (!MojangAPIUtils.checkUsernameAvailable(accessToken, profileName.getText())) {
                    DialogManager.okDialog().setParent(CreateMinecraftProfileDialog.this).setTitle(GetText.tr("Error"))
                            .setContent(
                                    GetText.tr(
                                            "Username is not available, please try another one."))
                            .setType(DialogManager.ERROR).show();
                    progressDialog.setReturnValue(false);
                    progressDialog.close();
                    return;
                }

                if (!MojangAPIUtils.createMcProfile(accessToken, profileName.getText())) {
                    LogManager.error("Unknown Error Occurred While Creating Profile!");
                    DialogManager.okDialog().setParent(CreateMinecraftProfileDialog.this).setTitle(GetText.tr("Error"))
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "An error occurred creating your profile.<br/><br/>Please check the console and try again."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    progressDialog.setReturnValue(false);
                    progressDialog.close();
                    return;
                }

                progressDialog.setReturnValue(true);
                progressDialog.close();
            }));
            progressDialog.start();

            if (progressDialog.getReturnValue()) {
                close();
            }

        });
        bottom.add(createButton);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void close() {
        setVisible(false);
        dispose();
    }
}
