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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.MojangAPIUtils;
import com.atlauncher.utils.Utils;

public class ChangeSkinDialog extends JDialog {
    private JTextField skinPath;
    private JComboBox<ComboItem<String>> skinType;
    private File selectedSkinFile;
    private JButton uploadButton;

    private final MicrosoftAccount account;

    public ChangeSkinDialog(MicrosoftAccount account) {
        this(account, App.launcher.getParent());
    }

    public ChangeSkinDialog(MicrosoftAccount account, Window parent) {
        super(parent, GetText.tr("Changing Skin"), ModalityType.DOCUMENT_MODAL);

        this.account = account;

        Analytics.sendScreenView("Change Skin Dialog");

        setSize(445, 180);
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
    }

    private void setupComponents() {
        // Top Panel Stuff
        JPanel top = new JPanel();
        top.add(new JLabel(GetText.tr("Changing Skin")));

        // Middle Panel Stuff
        JPanel middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Skin Browse
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel skinFilePath = new JLabel(GetText.tr("Skin File") + ": ");
        middle.add(skinFilePath, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JPanel skinPathPanel = new JPanel();
        skinPathPanel.setLayout(new BoxLayout(skinPathPanel, BoxLayout.X_AXIS));

        skinPath = new JTextField(16);
        JButton skinBrowseButton = new JButton(GetText.tr("Browse"));
        skinBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(FileSystem.SKINS.toFile());
            chooser.setDialogTitle(GetText.tr("Select skin file"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return "Skin File (.png)";
                }

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".png");
                }
            });

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedPath = chooser.getSelectedFile();

                try {
                    BufferedImage skinImage = ImageIO.read(selectedPath);

                    if (skinImage.getWidth() != 64 || (skinImage.getHeight() != 64 && skinImage.getHeight() != 32)) {
                        DialogManager.okDialog().setTitle("Invalid Skin")
                            .setContent(new HTMLBuilder().center()
                                .text(GetText.tr(
                                    "The skin you chose is invalid. Please make sure you selected the right file and try again.<br/><br/>All skins must be 64x64 or 64x32 pixels."))
                                .build())
                            .setType(DialogManager.ERROR).show();
                        return;
                    }

                    selectedSkinFile = selectedPath;
                    skinPath.setText(selectedPath.getAbsolutePath());
                    uploadButton.setEnabled(true);
                } catch (IOException err) {
                    LogManager.logStackTrace("Error reading in skin", err);
                }
            }
        });

        skinPathPanel.add(skinPath);
        skinPathPanel.add(Box.createHorizontalStrut(5));
        skinPathPanel.add(skinBrowseButton);

        middle.add(skinPathPanel, gbc);

        // Skin Type

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel skinTypeLabel = new JLabel(GetText.tr("Skin Type") + ": ");
        middle.add(skinTypeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        skinType = new JComboBox<>();
        skinType.addItem(new ComboItem<>("classic", "Classic"));
        skinType.addItem(new ComboItem<>("slim", "Slim"));
        middle.add(skinType, gbc);

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());

        uploadButton = new JButton(GetText.tr("Upload"));
        uploadButton.addActionListener(e -> {
            ProgressDialog<Boolean> progressDialog = new ProgressDialog<>(GetText.tr("Updating Skin"), 0,
                GetText.tr("Updating Skin"));
            progressDialog.addThread(new Thread(() -> {
                account.changeSkinPreCheck();
                progressDialog.setReturnValue(MojangAPIUtils.uploadSkin(account, selectedSkinFile,
                    ((ComboItem<String>) (skinType.getSelectedItem())).getValue()));
                progressDialog.close();
            }));
            progressDialog.start();

            if (!Boolean.TRUE.equals(progressDialog.getReturnValue())) {
                DialogManager.okDialog().setTitle("Error Updating Skin")
                    .setContent(new HTMLBuilder().center()
                        .text(GetText.tr(
                            "There was an error updating your skin.<br/><br/>Please check the logs and try again."))
                        .build())
                    .setType(DialogManager.ERROR).show();
                return;
            }

            // once api call is done, update the skin in the launcher
            account.updateSkin();

            close();
        });
        uploadButton.setEnabled(false);
        bottom.add(uploadButton);

        JButton cancelButton = new JButton(GetText.tr("Cancel"));
        cancelButton.addActionListener(e -> close());
        bottom.add(cancelButton);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void close() {
        setVisible(false);
        dispose();
    }
}
