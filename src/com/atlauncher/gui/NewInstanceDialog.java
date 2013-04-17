/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.atlauncher.data.Instance;
import com.atlauncher.data.Instances;
import com.atlauncher.data.Pack;
import com.atlauncher.data.Version;
import com.atlauncher.workers.PackInstaller;

@SuppressWarnings("serial")
public class NewInstanceDialog extends JDialog {

    private JPanel top;
    private JPanel middle;
    private JPanel bottom;
    private JButton install;
    private JButton cancel;
    private JProgressBar progressBar;
    private JLabel instanceNameLabel;
    private JTextField instanceNameField;
    private JLabel versionLabel;
    private JComboBox<Version> versionsDropDown;
    @SuppressWarnings("unused")
    private Instances instances;

    public NewInstanceDialog(final JFrame parent, final Pack pack,
            final Instances instances) {
        super(parent, "New Instance", ModalityType.APPLICATION_MODAL);
        this.instances = instances;
        setSize(400, 200);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // Top Panel Stuff

        top = new JPanel();
        top.add(new JLabel("Installing " + pack.getName()));

        // Middle Panel Stuff

        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        instanceNameLabel = new JLabel("Instance Name: ");
        middle.add(instanceNameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        instanceNameField = new JTextField(17);
        instanceNameField.setText(pack.getName());
        middle.add(instanceNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        versionLabel = new JLabel("Version To Install: ");
        middle.add(versionLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        versionsDropDown = new JComboBox<Version>();
        for (int i = 0; i < pack.getVersionCount(); i++) {
            versionsDropDown.addItem(pack.getVersion(i));
        }
        versionsDropDown.setPreferredSize(new Dimension(200, 25));
        middle.add(versionsDropDown, gbc);

        // Bottom Panel Stuff

        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        install = new JButton("Install");
        install.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (instances.isInstance(instanceNameField.getText())) {
                    JOptionPane
                            .showMessageDialog(
                                    parent,
                                    "<html><center>Error!<br/><br/>There is already an instance called "
                                            + instanceNameField.getText()
                                            + "<br/><br/>Rename it and try again</center></html>",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
                } else {
                    final Version version = (Version) versionsDropDown
                            .getSelectedItem();
                    final JDialog dialog = new JDialog(parent, "Installing "
                            + pack.getName() + " " + version,
                            ModalityType.APPLICATION_MODAL);
                    dialog.setLocationRelativeTo(parent);
                    dialog.setSize(300, 75);
                    dialog.setResizable(false);

                    JPanel topPanel = new JPanel();
                    topPanel.setLayout(new BorderLayout());
                    final JLabel doing = new JLabel("Starting Install Process");
                    doing.setHorizontalAlignment(JLabel.CENTER);
                    topPanel.add(doing);

                    JPanel bottomPanel = new JPanel();
                    bottomPanel.setLayout(new BorderLayout());
                    progressBar = new JProgressBar();
                    bottomPanel.add(progressBar);
                    progressBar.setIndeterminate(true);

                    dialog.add(topPanel, BorderLayout.CENTER);
                    dialog.add(bottomPanel, BorderLayout.SOUTH);

                    PackInstaller packInstaller = new PackInstaller(pack,
                            version, instanceNameField.getText()) {

                        protected void done() {
                            Boolean success = false;
                            int type;
                            String text;
                            String title;
                            if (!isCancelled()) {
                                try {
                                    success = get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (success) {
                                type = JOptionPane.INFORMATION_MESSAGE;
                                text = pack.getName()
                                        + " "
                                        + version
                                        + " has been installed<br/><br/>Find it in your 'Instances' tab named '"
                                        + instanceNameField.getText() + "'";
                                title = pack.getName() + " " + version
                                        + " Installed";
                                instances.addInstance(new Instance(
                                        instanceNameField.getText(), pack
                                                .getName(), version));
                                instances.reloadTable();
                            } else {
                                type = JOptionPane.ERROR_MESSAGE;
                                text = pack.getName()
                                        + " "
                                        + version
                                        + " wasn't installed<br/><br/>Check error logs for the error!";
                                title = pack.getName() + " " + version
                                        + " Not Installed";
                            }

                            dialog.dispose();

                            JOptionPane.showMessageDialog(parent,
                                    "<html><center>" + text
                                            + "</center></html>", title, type);
                        }

                    };
                    packInstaller
                            .addPropertyChangeListener(new PropertyChangeListener() {

                                public void propertyChange(
                                        PropertyChangeEvent evt) {
                                    if ("progress" == evt.getPropertyName()) {
                                        if (progressBar.isIndeterminate()) {
                                            progressBar.setIndeterminate(false);
                                        }
                                        int progress = (Integer) evt
                                                .getNewValue();
                                        progressBar.setValue(progress);
                                    } else if ("doing" == evt.getPropertyName()) {
                                        String doingText = (String) evt
                                                .getNewValue();
                                        doing.setText(doingText);
                                    }

                                }
                            });
                    packInstaller.execute();
                    dispose();
                    dialog.setVisible(true);
                }
            }
        });
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottom.add(install);
        bottom.add(cancel);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setVisible(true);
    }
}
