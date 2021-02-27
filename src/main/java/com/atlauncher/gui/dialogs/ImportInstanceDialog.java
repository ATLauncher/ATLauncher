/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;

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
import com.atlauncher.constants.UIConstants;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ImportPackUtils;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class ImportInstanceDialog extends JDialog {

    private final JTextField url;
    private final JTextField filePath;

    private final JButton addButton;

    public ImportInstanceDialog() {
        super(App.launcher.getParent(), GetText.tr("Import Instance"), ModalityType.DOCUMENT_MODAL);
        setSize(500, 250);
        setMinimumSize(new Dimension(500, 250));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        Analytics.sendScreenView("Import Instance Dialog");

        // Middle Panel Stuff
        JPanel middle = new JPanel();
        middle.setLayout(new BorderLayout());

        JEditorPane infoMessage = new JEditorPane("text/html", new HTMLBuilder().center().text(GetText.tr(
                "Select an exported instance zip to import it.<br/>We currently support ATLauncher, CurseForge and MultiMC exported zip files."))
                .build());
        infoMessage.setEditable(false);
        middle.add(infoMessage, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel urlLabel = new JLabel(GetText.tr("Url") + ": ");
        mainPanel.add(urlLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
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
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel fileLabel = new JLabel(GetText.tr("File") + ": ");
        fileLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        mainPanel.add(fileLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JPanel filePathPanel = new JPanel(new FlowLayout());
        filePath = new JTextField(17);
        filePath.setEnabled(false);
        filePathPanel.add(filePath);

        JButton browseButton = new JButton(GetText.tr("Browse"));
        browseButton.addActionListener(e -> {
            if (App.settings.useNativeFilePicker) {
                FileDialog fileDialog = new FileDialog(this, GetText.tr("Select file/s"), FileDialog.LOAD);
                fileDialog.setFilenameFilter(new FilenameFilter() {
                    @Override
                    public boolean accept(File f, String name) {
                        if (f.isDirectory()) {
                            return true;
                        }

                        return f.getName().endsWith(".zip");
                    }
                });
                fileDialog.setVisible(true);

                if (fileDialog.getFiles().length != 0) {
                    filePath.setText(fileDialog.getFiles()[0].getAbsolutePath());
                    changeAddButtonStatus();
                }
            } else {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(FileSystem.USER_DOWNLOADS.toFile());
                chooser.setDialogTitle(GetText.tr("Select"));
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileFilter(new FileFilter() {
                    @Override
                    public String getDescription() {
                        return "Modpack Export (.zip)";
                    }

                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }

                        return f.getName().endsWith(".zip");
                    }
                });

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    filePath.setText(chooser.getSelectedFile().getAbsolutePath());
                    changeAddButtonStatus();
                }
            }
        });
        filePathPanel.add(browseButton);
        mainPanel.add(filePathPanel, gbc);

        middle.add(mainPanel, BorderLayout.CENTER);

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        addButton = new JButton(GetText.tr("Import"));
        addButton.addActionListener(e -> {
            setVisible(false);

            final ProgressDialog<Boolean> dialog = new ProgressDialog<>(GetText.tr("Import Instance"), 0,
                    GetText.tr("Import Instance"), this);

            dialog.addThread(new Thread(() -> {
                if (!url.getText().isEmpty()) {
                    Analytics.sendEvent(url.getText(), "AddFromUrl", "ImportInstance");
                    dialog.setReturnValue(ImportPackUtils.loadFromUrl(url.getText()));
                } else if (!filePath.getText().isEmpty()) {
                    Analytics.sendEvent(new File(filePath.getText()).getName(), "AddFromZip", "ImportInstance");
                    dialog.setReturnValue(ImportPackUtils.loadFromFile(new File(filePath.getText())));
                } else {
                    dialog.setReturnValue(false);
                }
                dialog.close();
            }));

            dialog.start();

            if (!dialog.getReturnValue()) {
                DialogManager.okDialog().setTitle(GetText.tr("Failed To Import Instance"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "An error occured when trying to import an instance.<br/><br/>Check the console for more information."))
                                .build())
                        .setType(DialogManager.ERROR).show();
                setVisible(true);
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

        setVisible(true);
    }

    private void emptyZipPathField() {
        if (!url.getText().isEmpty()) {
            filePath.setText("");
        }
    }

    private void changeAddButtonStatus() {
        addButton.setEnabled(!url.getText().isEmpty() || !filePath.getText().isEmpty());
    }
}
