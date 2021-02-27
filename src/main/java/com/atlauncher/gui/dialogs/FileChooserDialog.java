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
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class FileChooserDialog extends JDialog {

    private final JTextField textField;

    private final JComboBox<String> selector;

    private File[] filesChosen;

    private boolean closed = false;

    public FileChooserDialog(Window parent, String title, String labelName, String bottomText, String selectorText,
            String[] subOptions) {
        super(parent, title, ModalityType.DOCUMENT_MODAL);
        setSize(400, 175);
        setMinimumSize(new Dimension(400, 175));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // Top Panel Stuff
        JPanel top = new JPanel();
        top.add(new JLabel(title));

        // Middle Panel Stuff
        JPanel middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        gbc.insets = UIConstants.LABEL_INSETS;
        JLabel nameLabel = new JLabel(labelName + ": ");
        middle.add(nameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        gbc.insets = UIConstants.FIELD_INSETS;

        JPanel filePathPanel = new JPanel();
        filePathPanel.setLayout(new BoxLayout(filePathPanel, BoxLayout.X_AXIS));

        textField = new JTextField(16);
        textField.setEnabled(false);

        JButton selectButton = new JButton(GetText.tr("Select"));
        selectButton.addActionListener(e -> {
            if (App.settings.useNativeFilePicker) {
                filesChosen = getFilesUsingFileDialog();
            } else {
                filesChosen = getFilesUsingJFileChooser();
            }
            if (filesChosen != null && filesChosen.length >= 1) {
                if (filesChosen.length == 1) {
                    textField.setText(filesChosen[0].getAbsolutePath());
                } else {
                    textField.setText(filesChosen.length + " Files Selected!");
                }
            }
        });

        filePathPanel.add(textField);
        filePathPanel.add(Box.createHorizontalStrut(5));
        filePathPanel.add(selectButton);

        middle.add(filePathPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        gbc.insets = UIConstants.LABEL_INSETS;
        JLabel selectorLabel = new JLabel(selectorText + ": ");
        middle.add(selectorLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        gbc.insets = UIConstants.FIELD_INSETS;
        selector = new JComboBox<>();
        for (String item : subOptions) {
            selector.addItem(item);
        }
        middle.add(selector, gbc);

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        JButton bottomButton = new JButton(bottomText);
        bottomButton.addActionListener(e -> close());
        bottom.add(bottomButton);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                closed = true;
                close();
            }
        });

        setVisible(true);
    }

    private File[] getFilesUsingJFileChooser() {
        JFileChooser fileChooser = new JFileChooser(FileSystem.BASE_DIR.toFile());
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return "Mod Files (.jar; .zip; .litemod)";
            }

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }

                return shouldAcceptFilename(f.getName());
            }
        });
        fileChooser.showOpenDialog(App.launcher.getParent());

        return fileChooser.getSelectedFiles();
    }

    private boolean shouldAcceptFilename(String name) {
        return Utils.isAcceptedModFile(name);
    }

    private File[] getFilesUsingFileDialog() {
        FileDialog fd = new FileDialog(this, GetText.tr("Select file/s"), FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return shouldAcceptFilename(name);
            }
        });
        fd.setVisible(true);

        return fd.getFiles();
    }

    private void close() {
        setVisible(false);
        dispose();
    }

    public boolean wasClosed() {
        return this.closed;
    }

    public ArrayList<File> getChosenFiles() {
        ArrayList<File> files = new ArrayList<>();
        if (this.filesChosen == null) {
            return null;
        }
        for (File file : filesChosen) {
            if (Utils.isAcceptedModFile(file)) {
                files.add(file);
            }
        }
        return files;
    }

    public String getSelectorValue() {
        return (String) this.selector.getSelectedItem();
    }

}
