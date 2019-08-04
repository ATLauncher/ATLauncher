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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

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
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class FileChooserDialog extends JDialog {
    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel nameLabel;
    private JTextField textField;

    private JLabel selectorLabel;
    private JComboBox<String> selector;

    private File[] filesChosen;
    private String[] fileOptions;

    private JButton bottomButton;
    private JButton selectButton;

    private boolean closed = false;

    public FileChooserDialog(String title, String labelName, String bottomText, String selectorText,
            String[] subOptions, String[] options) {
        super(App.settings.getParent(), title, ModalityType.APPLICATION_MODAL);
        this.fileOptions = options;
        setSize(400, 175);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(title));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        nameLabel = new JLabel(labelName + ": ");
        middle.add(nameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        textField = new JTextField(16);
        textField.setEnabled(false);
        middle.add(textField, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        selectButton = new JButton(GetText.tr("Select"));
        selectButton.addActionListener(e -> {
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

                    for (String ext : fileOptions) {
                        if (f.getName().endsWith(ext)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
            fileChooser.showOpenDialog(App.settings.getParent());
            filesChosen = fileChooser.getSelectedFiles();
            if (filesChosen != null && filesChosen.length >= 1) {
                if (filesChosen.length == 1) {
                    textField.setText(filesChosen[0].getAbsolutePath());
                } else {
                    textField.setText(filesChosen.length + " Files Selected!");
                }
            }
        });
        middle.add(selectButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        selectorLabel = new JLabel(selectorText + ": ");
        middle.add(selectorLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        selector = new JComboBox<>();
        for (String item : subOptions) {
            selector.addItem(item);
        }
        middle.add(selector, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        bottomButton = new JButton(bottomText);
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
            for (String ext : fileOptions) {
                if (file.getName().endsWith(ext)) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public String getSelectorValue() {
        return (String) this.selector.getSelectedItem();
    }

}
