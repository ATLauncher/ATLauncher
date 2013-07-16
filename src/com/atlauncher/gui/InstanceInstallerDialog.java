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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Pack;
import com.atlauncher.workers.InstanceInstaller;

public class InstanceInstallerDialog extends JDialog {

    private boolean isReinstall = false;
    private boolean isServer = false;
    private Pack pack = null;
    private Instance instance = null;

    private JPanel top;
    private JPanel middle;
    private JPanel bottom;
    private JButton install;
    private JButton cancel;
    private JProgressBar progressBar;
    private JProgressBar subProgressBar;
    private JLabel instanceNameLabel;
    private JTextField instanceNameField;
    private JLabel versionLabel;
    private JComboBox<String> versionsDropDown;
    private JLabel installForLabel;
    private JCheckBox installForMe;
    private JLabel useLatestLWJGLLabel;
    private JCheckBox useLatestLWJGL;

    public InstanceInstallerDialog(Object object) {
        this(object, false, false);
    }

    public InstanceInstallerDialog(Pack pack, boolean isServer) {
        this((Object) pack, false, true);
    }

    public InstanceInstallerDialog(Object object, boolean isUpdate, final boolean isServer) {
        super(App.settings.getParent(), ModalityType.APPLICATION_MODAL);
        if (object instanceof Pack) {
            pack = (Pack) object;
            setTitle(App.settings.getLocalizedString("common.installing") + " " + pack.getName());
            if (isServer) {
                setTitle(App.settings.getLocalizedString("common.installing") + " "
                        + pack.getName() + " " + App.settings.getLocalizedString("common.server"));
                this.isServer = true;
            }
        } else {
            instance = (Instance) object;
            pack = instance.getRealPack();
            isReinstall = true; // We're reinstalling
            setTitle(App.settings.getLocalizedString("common.reinstalling") + " "
                    + instance.getName());
        }
        setSize(400, 225);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(((isReinstall) ? App.settings.getLocalizedString("common.reinstalling")
                : App.settings.getLocalizedString("common.installing")) + " " + pack.getName()));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        if (!this.isServer) {
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            instanceNameLabel = new JLabel(App.settings.getLocalizedString("instance.name") + ": ");
            middle.add(instanceNameLabel, gbc);

            gbc.gridx++;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            instanceNameField = new JTextField(17);
            instanceNameField.setText(((isReinstall) ? instance.getName() : pack.getName()));
            if (isReinstall) {
                instanceNameField.setEnabled(false);
            }
            middle.add(instanceNameField, gbc);

            gbc.gridx = 0;
            gbc.gridy++;
        }
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        versionLabel = new JLabel(App.settings.getLocalizedString("instance.versiontoinstall")
                + ": ");
        middle.add(versionLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        versionsDropDown = new JComboBox<String>();
        if (pack.isTester()) {
            versionsDropDown.addItem("Dev");
        }
        for (int i = 0; i < pack.getVersionCount(); i++) {
            versionsDropDown.addItem(pack.getVersion(i));
        }
        if (isUpdate) {
            if (pack.isTester()) {
                versionsDropDown.setSelectedIndex(1);
            } else {
                versionsDropDown.setSelectedIndex(0);
            }
        } else if (isReinstall) {
            versionsDropDown.setSelectedItem(instance.getVersion());
        }
        versionsDropDown.setPreferredSize(new Dimension(200, 25));
        versionsDropDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = versionsDropDown.getSelectedIndex();
                if (pack.isTester()) {
                    index--;
                }
                if (App.settings.getMinecraftInstallMethod(pack.getMinecraftVersion(index))
                        .equalsIgnoreCase("new")) {
                    useLatestLWJGLLabel.setVisible(false);
                    useLatestLWJGL.setVisible(false);
                    useLatestLWJGL.setSelected(false);
                } else if (pack.isLatestLWJGLEnabled()) {
                    useLatestLWJGLLabel.setVisible(false);
                    useLatestLWJGL.setVisible(false);
                    useLatestLWJGL.setSelected(true);
                } else {
                    useLatestLWJGLLabel.setVisible(true);
                    useLatestLWJGL.setVisible(true);
                }
            }
        });
        middle.add(versionsDropDown, gbc);

        if (!this.isServer) {
            if (!isReinstall) {
                gbc.gridx = 0;
                gbc.gridy++;
                gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
                installForLabel = new JLabel(
                        App.settings.getLocalizedString("instance.installjustforme") + "? ");
                middle.add(installForLabel, gbc);

                gbc.gridx++;
                gbc.anchor = GridBagConstraints.BASELINE_LEADING;
                installForMe = new JCheckBox();
                middle.add(installForMe, gbc);
            }
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            useLatestLWJGLLabel = new JLabel(
                    App.settings.getLocalizedString("instance.uselatestlwjgl") + "? ");
            middle.add(useLatestLWJGLLabel, gbc);

            gbc.gridx++;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            useLatestLWJGL = new JCheckBox();
            middle.add(useLatestLWJGL, gbc);
        }

        int index = versionsDropDown.getSelectedIndex();
        if (pack.isTester()) {
            index--;
        }
        if (App.settings.getMinecraftInstallMethod(pack.getMinecraftVersion(index))
                .equalsIgnoreCase("new")) {
            useLatestLWJGLLabel.setVisible(false);
            useLatestLWJGL.setVisible(false);
        } else if (pack.isLatestLWJGLEnabled()) {
            useLatestLWJGLLabel.setVisible(false);
            useLatestLWJGL.setVisible(false);
            useLatestLWJGL.setSelected(true);
        } else {
            useLatestLWJGLLabel.setVisible(true);
            useLatestLWJGL.setVisible(true);
        }

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        install = new JButton(
                ((isReinstall) ? (isUpdate ? App.settings.getLocalizedString("common.update")
                        : App.settings.getLocalizedString("common.reinstall"))
                        : App.settings.getLocalizedString("common.install")));
        install.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isReinstall && !isServer
                        && App.settings.isInstance(instanceNameField.getText())) {
                    JOptionPane.showMessageDialog(
                            App.settings.getParent(),
                            "<html><center>"
                                    + App.settings.getLocalizedString("common.error")
                                    + "<br/><br/>"
                                    + App.settings.getLocalizedString("instance.alreadyinstance",
                                            instanceNameField.getText() + "<br/><br/>")
                                    + "</center></html>", App.settings
                                    .getLocalizedString("common.error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                final String version = (String) versionsDropDown.getSelectedItem();
                final JDialog dialog = new JDialog(App.settings.getParent(),
                        ((isReinstall) ? App.settings.getLocalizedString("common.reinstalling")
                                : App.settings.getLocalizedString("common.installing"))
                                + " "
                                + pack.getName() + " " + version, ModalityType.DOCUMENT_MODAL);
                dialog.setLocationRelativeTo(App.settings.getParent());
                dialog.setSize(300, 100);
                dialog.setResizable(false);

                JPanel topPanel = new JPanel();
                topPanel.setLayout(new BorderLayout());
                final JLabel doing = new JLabel(App.settings.getLocalizedString(
                        "instance.startingprocess",
                        ((isReinstall) ? App.settings.getLocalizedString("common.reinstall")
                                : App.settings.getLocalizedString("common.install"))));
                doing.setHorizontalAlignment(JLabel.CENTER);
                doing.setVerticalAlignment(JLabel.TOP);
                topPanel.add(doing);

                JPanel bottomPanel = new JPanel();
                bottomPanel.setLayout(new BorderLayout());
                progressBar = new JProgressBar(0, 100);
                bottomPanel.add(progressBar, BorderLayout.NORTH);
                progressBar.setIndeterminate(true);
                subProgressBar = new JProgressBar(0, 100);
                bottomPanel.add(subProgressBar, BorderLayout.SOUTH);
                subProgressBar.setValue(0);
                subProgressBar.setVisible(false);

                dialog.add(topPanel, BorderLayout.CENTER);
                dialog.add(bottomPanel, BorderLayout.SOUTH);

                final InstanceInstaller instanceInstaller = new InstanceInstaller((isServer ? ""
                        : instanceNameField.getText()), pack, version,
                        (useLatestLWJGL == null ? false : useLatestLWJGL.isSelected()),
                        isReinstall, isServer) {

                    protected void done() {
                        Boolean success = false;
                        int type;
                        String text;
                        String title;
                        if (isCancelled()) {
                            type = JOptionPane.ERROR_MESSAGE;
                            text = pack.getName()
                                    + " "
                                    + version
                                    + " "
                                    + App.settings.getLocalizedString("common.wasnt")
                                    + " "
                                    + ((isReinstall) ? App.settings
                                            .getLocalizedString("common.reinstalled")
                                            : App.settings.getLocalizedString("common.installed"))
                                    + "<br/><br/>"
                                    + App.settings.getLocalizedString("instance.actioncancelled");
                            title = pack.getName()
                                    + " "
                                    + version
                                    + " "
                                    + App.settings.getLocalizedString("common.not")
                                    + " "
                                    + ((isReinstall) ? App.settings
                                            .getLocalizedString("common.reinstalled")
                                            : App.settings.getLocalizedString("common.installed"));
                            if (isReinstall) {
                                App.settings.setInstanceUnplayable(instance);
                            }
                        } else {
                            try {
                                success = get();
                            } catch (InterruptedException e) {
                                App.settings.getConsole().logStackTrace(e);
                            } catch (ExecutionException e) {
                                App.settings.getConsole().logStackTrace(e);
                            }
                            if (success) {
                                type = JOptionPane.INFORMATION_MESSAGE;
                                text = pack.getName()
                                        + " "
                                        + version
                                        + " "
                                        + App.settings.getLocalizedString("common.hasbeen")
                                        + " "
                                        + ((isReinstall) ? App.settings
                                                .getLocalizedString("common.reinstalled")
                                                : App.settings
                                                        .getLocalizedString("common.installed"))
                                        + "<br/><br/>"
                                        + App.settings.getLocalizedString("instance.findit");
                                title = pack.getName() + " " + version + " "
                                        + App.settings.getLocalizedString("common.installed");
                                if (isReinstall) {
                                    instance.setVersion(version);
                                    instance.setMinecraftVersion(this.getMinecraftVersion());
                                    instance.setModsInstalled(this.getModsInstalled());
                                    instance.setJarOrder(this.getJarOrder());
                                    instance.setIsNewLaunchMethod(this.isNewLaunchMethod());
                                    if (this.isNewLaunchMethod()) {
                                        instance.setLibrariesNeeded(this.getLibrariesNeeded());
                                        instance.setMinecraftArguments(this.getMinecraftArguments());
                                        instance.setMainClass(this.getMainClass());
                                    }
                                    if (!instance.isPlayable()) {
                                        instance.setPlayable();
                                    }
                                } else if (isServer) {

                                } else {
                                    App.settings.getInstances().add(
                                            new Instance(instanceNameField.getText(), pack
                                                    .getName(), pack, installForMe.isSelected(),
                                                    version, this.getMinecraftVersion(), this
                                                            .getModsInstalled(),
                                                    this.getJarOrder(), this.getLibrariesNeeded(),
                                                    this.getMinecraftArguments(), this
                                                            .getMainClass(), this
                                                            .isNewLaunchMethod())); // Add It
                                }
                                App.settings.saveInstances();
                                App.settings.reloadInstancesPanel();
                                if (pack.isLoggingEnabled() && App.settings.enableLogs()) {
                                    App.settings.apiCall(App.settings.getAccount()
                                            .getMinecraftUsername(), "packinstalled", pack.getID()
                                            + "", version);
                                }
                            } else {
                                if (isReinstall) {
                                    type = JOptionPane.ERROR_MESSAGE;
                                    text = pack.getName()
                                            + " "
                                            + version
                                            + " "
                                            + App.settings.getLocalizedString("common.wasnt")
                                            + " "
                                            + App.settings.getLocalizedString("common.reinstalled")
                                            + "<br/><br/>"
                                            + App.settings
                                                    .getLocalizedString("instance.nolongerplayable")
                                            + "<br/><br/>"
                                            + App.settings
                                                    .getLocalizedString("instance.checkerrorlogs")
                                            + "!";
                                    title = pack.getName() + " " + version + " "
                                            + App.settings.getLocalizedString("common.not") + " "
                                            + App.settings.getLocalizedString("common.reinstalled");
                                    App.settings.setInstanceUnplayable(instance);
                                } else {
                                    // Install failed so delete the folder and clear Temp Dir
                                    Utils.delete(this.getRootDirectory());
                                    type = JOptionPane.ERROR_MESSAGE;
                                    text = pack.getName()
                                            + " "
                                            + version
                                            + " "
                                            + App.settings.getLocalizedString("common.wasnt")
                                            + " "
                                            + App.settings.getLocalizedString("common.installed")
                                            + "<br/><br/>"
                                            + App.settings
                                                    .getLocalizedString("instance.checkerrorlogs")
                                            + "!";
                                    title = pack.getName() + " " + version + " "
                                            + App.settings.getLocalizedString("common.not") + " "
                                            + App.settings.getLocalizedString("common.installed");
                                }
                            }
                        }

                        dialog.dispose();

                        Utils.cleanTempDirectory();

                        JOptionPane.showMessageDialog(App.settings.getParent(), "<html><center>"
                                + text + "</center></html>", title, type);
                    }

                };
                instanceInstaller.addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("progress" == evt.getPropertyName()) {
                            if (progressBar.isIndeterminate()) {
                                progressBar.setIndeterminate(false);
                            }
                            int progress = (Integer) evt.getNewValue();
                            if (progress > 100) {
                                progress = 100;
                            }
                            progressBar.setValue(progress);
                        } else if ("subprogress" == evt.getPropertyName()) {
                            if (!subProgressBar.isVisible()) {
                                subProgressBar.setVisible(true);
                            }
                            if (subProgressBar.isIndeterminate()) {
                                subProgressBar.setIndeterminate(false);
                            }
                            int progress = (Integer) evt.getNewValue();
                            if (progress > 100) {
                                progress = 100;
                            }
                            if (progress == 0) {
                                subProgressBar.setVisible(false);
                            }
                            subProgressBar.setValue(progress);
                        } else if ("subprogressint" == evt.getPropertyName()) {
                            if (!subProgressBar.isVisible()) {
                                subProgressBar.setVisible(true);
                            }
                            if (!subProgressBar.isIndeterminate()) {
                                subProgressBar.setIndeterminate(true);
                            }
                        } else if ("doing" == evt.getPropertyName()) {
                            String doingText = (String) evt.getNewValue();
                            doing.setText(doingText);
                        }

                    }
                });
                dialog.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        instanceInstaller.cancel(true);
                    }
                });
                if (isReinstall) {
                    instanceInstaller.setInstance(instance);
                }
                instanceInstaller.execute();
                dispose();
                dialog.setVisible(true);

            }
        });
        cancel = new JButton(App.settings.getLocalizedString("common.cancel"));
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
