/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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
import com.atlauncher.utils.Utils;

public class FileChooserDialog extends JDialog {

    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel nameLabel;
    private JTextField textField;

    private JLabel selectorLabel;
    private JComboBox<String> selector;

    private File fileChosen;
    private String[] fileOptions;

    private JButton bottomButton;
    private JButton selectButton;

    public FileChooserDialog(String title, String labelName, String bottomText,
            String selectorText, String selectorSelectText, String[] subOptions, String[] options) {
        super(App.settings.getParent(), title, ModalityType.APPLICATION_MODAL);
        this.fileOptions = options;
        setSize(400, 175);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/resources/Icon.png"));
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
        selectButton = new JButton(App.settings.getLocalizedString("common.select"));
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(App.settings.getBaseDir());
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
                if (fileChooser.getSelectedFile() != null) {
                    fileChosen = fileChooser.getSelectedFile();
                    textField.setText(fileChosen.getAbsolutePath());
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
        selector = new JComboBox<String>();
        selector.addItem(selectorSelectText);
        for (String item : subOptions) {
            selector.addItem(item);
        }
        middle.add(selector, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        bottomButton = new JButton(bottomText);
        bottomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        bottom.add(bottomButton);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        setVisible(true);
    }

    private void close() {
        setVisible(false);
        dispose();
    }

    public File getChosenFile() {
        if (this.fileChosen == null) {
            return null;
        }
        for (String ext : fileOptions) {
            if (this.fileChosen.getName().endsWith(ext)) {
                return this.fileChosen;
            }
        }
        return null;
    }

    public String getSelectorValue() {
        if (this.selector.getSelectedIndex() == 0) {
            return null;
        } else {
            return (String) this.selector.getSelectedItem();
        }
    }

}
