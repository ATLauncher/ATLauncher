/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CursePackUtils;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class AddCursePackDialog extends JDialog {
    private JPanel middle;
    private JPanel bottom;

    private JLabel urlLabel;
    private JTextField url;

    private JLabel zipLabel;
    private JTextField zipPath;
    private JButton zipBrowseButton;

    private JButton addButton;

    public AddCursePackDialog() {
        super(App.settings.getParent(), GetText.tr("Add Curse Pack"), ModalityType.APPLICATION_MODAL);
        setSize(450, 200);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        Analytics.sendScreenView("Add Curse Pack Dialog");

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new BorderLayout());

        JEditorPane infoMessage = new JEditorPane("text/html", new HTMLBuilder().center()
                .text(GetText.tr("Paste in a link to a modpack, or upload a zip file from CurseForge")).build());
        infoMessage.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        infoMessage.setEditable(false);
        middle.add(infoMessage, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        urlLabel = new JLabel(GetText.tr("CurseForge Url") + ": ");
        mainPanel.add(urlLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        url = new JTextField(25);
        url.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                emptyZipPathField();
                changeAddButtonStatus();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                emptyZipPathField();
                changeAddButtonStatus();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                emptyZipPathField();
                changeAddButtonStatus();
            }
        });
        mainPanel.add(url, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        zipLabel = new JLabel(GetText.tr("CurseForge Zip") + ": ");
        zipLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        mainPanel.add(zipLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JPanel zipPathPanel = new JPanel(new FlowLayout());
        zipPath = new JTextField(17);
        zipPath.setEnabled(false);
        zipPathPanel.add(zipPath);

        zipBrowseButton = new JButton(GetText.tr("Browse"));
        zipBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(FileSystem.USER_DOWNLOADS.toFile());
            chooser.setDialogTitle(GetText.tr("Select"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return "CurseForge modpack files (.zip)";
                }

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return false;
                    }

                    return f.getName().endsWith(".zip");
                }
            });

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                zipPath.setText(chooser.getSelectedFile().getAbsolutePath());
                url.setText("");
                changeAddButtonStatus();
            }
        });
        zipPathPanel.add(zipBrowseButton);
        mainPanel.add(zipPathPanel, gbc);

        middle.add(mainPanel, BorderLayout.CENTER);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        addButton = new JButton(GetText.tr("Add"));
        addButton.addActionListener(e -> {
            setVisible(false);

            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Adding Curse Pack"), 0,
                    GetText.tr("Adding Curse Pack"));

            dialog.addThread(new Thread(() -> {
                if (!url.getText().isEmpty()) {
                    Analytics.sendEvent(url.getText(), "AddFromUrl", "CursePack");
                    dialog.setReturnValue(CursePackUtils.loadFromUrl(url.getText()));
                } else if (!zipPath.getText().isEmpty()) {
                    Analytics.sendEvent(new File(zipPath.getText()).getName(), "AddFromZip", "CursePack");
                    dialog.setReturnValue(CursePackUtils.loadFromFile(new File(zipPath.getText())));
                } else {
                    dialog.setReturnValue(false);
                }
                dialog.close();
            }));

            dialog.start();

            if (!((boolean) dialog.getReturnValue())) {
                setVisible(true);
                DialogManager.okDialog().setTitle(GetText.tr("Failed To Add Pack"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "An error occured when trying to add Curse pack.<br/><br/>Check the console for more information."))
                                .build())
                        .setType(DialogManager.ERROR).show();
            } else {
                dispose();
            }
        });
        bottom.add(addButton);

        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                setVisible(false);
                dispose();
            }
        });

        try {
            String clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);

            if (clipboard.startsWith("https://www.curseforge.com/minecraft/modpacks")) {
                url.setText(clipboard);
            }
        } catch (HeadlessException | UnsupportedFlavorException | IOException ignored) {
        }

        setVisible(true);
    }

    private void emptyZipPathField() {
        if (!url.getText().isEmpty()) {
            zipPath.setText("");
        }
    }

    private void changeAddButtonStatus() {
        addButton.setEnabled(!url.getText().isEmpty() || !zipPath.getText().isEmpty());
    }
}
