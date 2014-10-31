/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.atlauncher.gui.dialogs;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.utils.Utils;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class RenameInstanceDialog extends JDialog {
    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel instanceNameLabel;
    private JTextField instanceName;

    private JButton saveButton;

    public RenameInstanceDialog(final Instance instance) {
        super(null, Language.INSTANCE.localize("instance.renaminginstance"), ModalityType.APPLICATION_MODAL);
        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(Language.INSTANCE.localize("instance.renaminginstance")));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        instanceNameLabel = new JLabel(Language.INSTANCE.localize("instance.name") + ": ");
        middle.add(instanceNameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        instanceName = new JTextField(16);
        instanceName.setText(instance.getName());
        middle.add(instanceName, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        saveButton = new JButton(Language.INSTANCE.localize("common.save"));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInstance(instanceName.getText())) {
                    JOptionPane.showMessageDialog(RenameInstanceDialog.this, Language.INSTANCE.localizeWithReplace
                            ("instance.alreadyinstance", instanceName.getText()), Language.INSTANCE.localize("common"
                            + ".error"), JOptionPane.ERROR_MESSAGE);
                } else if (instanceName.getText().replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                    JOptionPane.showMessageDialog(App.settings.getParent(), "<html><p align=\"center\">" + Language
                            .INSTANCE.localize("common.error") + "<br/><br/>" + Language.INSTANCE.localizeWithReplace
                            ("instance.invalidname", instanceName.getText()) + "</p></html>",
                            Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    if (instance.rename(instanceName.getText())) {
                        App.settings.saveInstances();
                        App.settings.reloadInstancesPanel();
                    } else {
                        LogManager.error("Unknown Error Occured While Renaming Instance!");
                        JOptionPane.showMessageDialog(RenameInstanceDialog.this,
                                "<html><p align=\"center\">" + Language.INSTANCE.localizeWithReplace("instance" + "" +
                                        ".errorrenaming", instance.getName() + "<br/><br/>") + "</p></html>",
                                Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                    }
                    close();
                }
            }
        });
        bottom.add(saveButton);

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
}
