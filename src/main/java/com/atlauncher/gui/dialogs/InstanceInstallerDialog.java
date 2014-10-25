/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.dialogs;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

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
import java.util.ArrayList;
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

public class InstanceInstallerDialog extends JDialog {
    private static final long serialVersionUID = -6984886874482721558L;
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
    private JComboBox<PackVersion> versionsDropDown;
    private ArrayList<PackVersion> versions = new ArrayList<PackVersion>();
    private JLabel installForLabel;
    private JCheckBox installForMe;

    public InstanceInstallerDialog(Object object) {
        this(object, false, false);
    }

    public InstanceInstallerDialog(Pack pack, boolean isServer) {
        this((Object) pack, false, true);
    }

    public InstanceInstallerDialog(Object object, final boolean isUpdate, final boolean isServer) {
        super(App.settings.getParent(), ModalityType.APPLICATION_MODAL);
        if (object instanceof Pack) {
            pack = (Pack) object;
            setTitle(App.settings.getLocalizedString("common.installing") + " " + pack.getName());
            if (isServer) {
                setTitle(App.settings.getLocalizedString("common.installing") + " " + pack.getName() + " " + App
                        .settings.getLocalizedString("common.server"));
                this.isServer = true;
            }
        } else {
            instance = (Instance) object;
            pack = instance.getRealPack();
            isReinstall = true; // We're reinstalling
            setTitle(App.settings.getLocalizedString("common.reinstalling") + " " + instance.getName());
        }
        setSize(400, 225);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        ((LauncherFrame) App.settings.getParent()).blur.setBlur(true);

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(((isReinstall) ? App.settings.getLocalizedString("common.reinstalling") : App.settings
                .getLocalizedString("common.installing")) + " " + pack.getName()));

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
        versionLabel = new JLabel(App.settings.getLocalizedString("instance.versiontoinstall") + ": ");
        middle.add(versionLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        versionsDropDown = new JComboBox<PackVersion>();
        if (pack.isTester()) {
            for (PackVersion pv : pack.getDevVersions()) {
                if (!isServer || (isServer && pv.getMinecraftVersion().canCreateServer())) {
                    versions.add(pv);
                }
            }
        }
        for (PackVersion pv : pack.getVersions()) {
            if (!isServer || (isServer && pv.getMinecraftVersion().canCreateServer())) {
                versions.add(pv);
            }
        }
        PackVersion forUpdate = null;
        for (PackVersion version : versions) {
            if ((!version.isDev()) && (forUpdate == null)) {
                forUpdate = version;
            }
            versionsDropDown.addItem(version);
        }
        if (isUpdate && forUpdate != null) {
            versionsDropDown.setSelectedItem(forUpdate);
        } else if (isReinstall) {
            for (PackVersion version : versions) {
                if (version.versionMatches(instance.getVersion())) {
                    versionsDropDown.setSelectedItem(version);
                }
            }
        } else {
            for (PackVersion version : versions) {
                if (!version.isRecommended() || version.isDev()) {
                    continue;
                }
                versionsDropDown.setSelectedItem(version);
                break;
            }
        }
        versionsDropDown.setPreferredSize(new Dimension(200, 25));
        middle.add(versionsDropDown, gbc);

        if (!this.isServer) {
            if (!isReinstall) {
                gbc.gridx = 0;
                gbc.gridy++;
                gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
                installForLabel = new JLabel(App.settings.getLocalizedString("instance.installjustforme") + "? ");
                middle.add(installForLabel, gbc);

                gbc.gridx++;
                gbc.anchor = GridBagConstraints.BASELINE_LEADING;
                installForMe = new JCheckBox();
                middle.add(installForMe, gbc);
            }
        }

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        install = new JButton(((isReinstall) ? (isUpdate ? App.settings.getLocalizedString("common.update") : App
                .settings.getLocalizedString("common.reinstall")) : App.settings.getLocalizedString("common.install")));
        install.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isReinstall && !isServer && App.settings.isInstance(instanceNameField.getText())) {
                    instance = App.settings.getInstanceByName(instanceNameField.getText());
                    if (instance.getPackName().equalsIgnoreCase(pack.getName())) {
                        int ret = JOptionPane.showConfirmDialog(App.settings.getParent(),
                                "<html><p align=\"center\">" + App.settings.getLocalizedString("common.error") +
                                        "<br/><br/>" + App.settings.getLocalizedString("instance.alreadyinstance1",
                                        instanceNameField.getText() + "<br/><br/>") + "</p></html>",
                                App.settings.getLocalizedString("common.error"), JOptionPane.ERROR_MESSAGE);
                        if (ret != JOptionPane.YES_OPTION) {
                            return;
                        }
                        isReinstall = true;
                        if (instance == null) {
                            return;
                        }
                    } else {
                        JOptionPane.showMessageDialog(App.settings.getParent(), "<html><p align=\"center\">" + App
                                .settings.getLocalizedString("common.error") + "<br/><br/>" + App.settings
                                .getLocalizedString("instance.alreadyinstance",
                                        instanceNameField.getText() + "<br/><br/>") + "</p></html>",
                                App.settings.getLocalizedString("common.error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else if (!isReinstall && !isServer && instanceNameField.getText().replaceAll("[^A-Za-z0-9]",
                        "").length() == 0) {
                    JOptionPane.showMessageDialog(App.settings.getParent(), "<html><p align=\"center\">" + App
                            .settings.getLocalizedString("common.error") + "<br/><br/>" + App.settings
                            .getLocalizedString("instance.invalidname", instanceNameField.getText()) + "</p></html>",
                            App.settings.getLocalizedString("common.error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                final PackVersion version = (PackVersion) versionsDropDown.getSelectedItem();
                final JDialog dialog = new JDialog(App.settings.getParent(),
                        ((isReinstall) ? App.settings.getLocalizedString("common.reinstalling") : App.settings
                                .getLocalizedString("common.installing")) + " " + pack.getName() + " " + version
                                .getVersion() + ((isServer) ? " " + App.settings.getLocalizedString("common.server")
                                : ""), ModalityType.DOCUMENT_MODAL);
                dialog.setLocationRelativeTo(App.settings.getParent());
                dialog.setSize(300, 100);
                dialog.setResizable(false);

                JPanel topPanel = new JPanel();
                topPanel.setLayout(new BorderLayout());
                final JLabel doing = new JLabel(App.settings.getLocalizedString("instance.startingprocess",
                        ((isReinstall) ? App.settings.getLocalizedString("common.reinstall") : App.settings
                                .getLocalizedString("common.install"))));
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

                final InstanceInstaller instanceInstaller = new InstanceInstaller((isServer ? "" : instanceNameField
                        .getText()), pack, version, isReinstall, isServer) {

                    protected void done() {
                        Boolean success = false;
                        int type;
                        String text;
                        String title;
                        if (isCancelled()) {
                            type = JOptionPane.ERROR_MESSAGE;
                            text = pack.getName() + " " + version.getVersion() + " " + App.settings
                                    .getLocalizedString("common.wasnt") + " " + ((isReinstall) ? App.settings
                                    .getLocalizedString("common.reinstalled") : App.settings.getLocalizedString
                                    ("common.installed")) + "<br/><br/>" + App.settings.getLocalizedString("instance"
                                    + ".checkerrorlogs");
                            title = pack.getName() + " " + version.getVersion() + " " + App.settings
                                    .getLocalizedString("common.not") + " " + ((isReinstall) ? App.settings
                                    .getLocalizedString("common.reinstalled") : App.settings.getLocalizedString
                                    ("common.installed"));
                            if (isReinstall) {
                                if (shouldCoruptInstance()) {
                                    App.settings.setInstanceUnplayable(instance);
                                }
                            }
                        } else {
                            try {
                                success = get();
                            } catch (InterruptedException e) {
                                App.settings.logStackTrace(e);
                            } catch (ExecutionException e) {
                                App.settings.logStackTrace(e);
                            }
                            if (success) {
                                type = JOptionPane.INFORMATION_MESSAGE;
                                text = pack.getName() + " " + version.getVersion() + " " + App.settings
                                        .getLocalizedString("common.hasbeen") + " " + ((isReinstall) ? App.settings
                                        .getLocalizedString("common.reinstalled") : App.settings.getLocalizedString
                                        ("common.installed")) + "<br/><br/>" + ((isServer) ? App.settings
                                        .getLocalizedString("instance.finditserver",
                                                "<br/><br/>" + this.getRootDirectory().getAbsolutePath()) : App
                                        .settings.getLocalizedString("instance.findit"));
                                title = pack.getName() + " " + version.getVersion() + " " + App.settings
                                        .getLocalizedString("common.installed");
                                if (isReinstall) {
                                    instance.setVersion(version.getVersion());
                                    instance.setMinecraftVersion(version.getMinecraftVersion().getVersion());
                                    instance.setModsInstalled(this.getModsInstalled());
                                    instance.setJarOrder(this.getJarOrder());
                                    instance.setMemory(this.getMemory());
                                    instance.setPermgen(this.getPermGen());
                                    instance.setIsNewLaunchMethod(!this.isLegacy());
                                    instance.setLibrariesNeeded(this.getLibrariesNeeded());
                                    instance.setMinecraftArguments(this.getMinecraftArguments());
                                    instance.setExtraArguments(this.getExtraArguments());
                                    instance.setMainClass(this.getMainClass());
                                    instance.setAssets(version.getMinecraftVersion().getMojangVersion().getAssets());
                                    if (version.isDev()) {
                                        instance.setDevVersion();
                                    } else {
                                        instance.setNotDevVersion();
                                    }
                                    if (!instance.isPlayable()) {
                                        instance.setPlayable();
                                    }
                                } else if (isServer) {

                                } else {
                                    App.settings.getInstances().add(new Instance(instanceNameField.getText(),
                                            pack.getName(), pack, installForMe.isSelected(), version.getVersion(),
                                            version.getMinecraftVersion().getVersion(), this.getMemory(),
                                            this.getPermGen(), this.getModsInstalled(), this.getJarOrder(),
                                            this.getLibrariesNeeded(), this.getExtraArguments(),
                                            this.getMinecraftArguments(), this.getMainClass(),
                                            version.getMinecraftVersion().getMojangVersion().getAssets(),
                                            version.isDev(), !version.getMinecraftVersion().isLegacy()));
                                }
                                App.settings.saveInstances();
                                App.settings.reloadInstancesPanel();
                                if (pack.isLoggingEnabled() && App.settings.enableLogs() && !version.isDev()) {
                                    if (isServer) {
                                        pack.addServerInstall(version.getVersion());
                                    } else if (isUpdate) {
                                        pack.addUpdate(version.getVersion());
                                    } else {
                                        pack.addInstall(version.getVersion());
                                    }
                                }
                            } else {
                                if (isReinstall) {
                                    type = JOptionPane.ERROR_MESSAGE;
                                    text = pack.getName() + " " + version.getVersion() + " " + App.settings
                                            .getLocalizedString("common.wasnt") + " " + App.settings
                                            .getLocalizedString("common.reinstalled") + "<br/><br/>" + (this
                                            .shouldCoruptInstance() ? App.settings.getLocalizedString("instance" + "" +
                                            ".nolongerplayable") : "") + "<br/><br/>" + App.settings
                                            .getLocalizedString("instance.checkerrorlogs") + "!";
                                    title = pack.getName() + " " + version.getVersion() + " " + App.settings
                                            .getLocalizedString("common.not") + " " + App.settings.getLocalizedString
                                            ("common.reinstalled");
                                    if (this.shouldCoruptInstance()) {
                                        App.settings.setInstanceUnplayable(instance);
                                    }
                                } else {
                                    // Install failed so delete the folder and clear Temp Dir
                                    Utils.delete(this.getRootDirectory());
                                    type = JOptionPane.ERROR_MESSAGE;
                                    text = pack.getName() + " " + version.getVersion() + " " + App.settings
                                            .getLocalizedString("common.wasnt") + " " + App.settings
                                            .getLocalizedString("common.installed") + "<br/><br/>" + App.settings
                                            .getLocalizedString("instance.checkerrorlogs") + "!";
                                    title = pack.getName() + " " + version.getVersion() + " " + App.settings
                                            .getLocalizedString("common.not") + " " + App.settings.getLocalizedString
                                            ("common.installed");
                                }
                            }
                        }

                        dialog.dispose();

                        Utils.cleanTempDirectory();

                        JOptionPane.showMessageDialog(App.settings.getParent(), "<html><p align=\"center\">" + text +
                                "</p></html>", title, type);
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
                            int progress;
                            String paint = null;
                            if (evt.getNewValue() instanceof Integer) {
                                progress = (Integer) evt.getNewValue();
                            } else {
                                String[] parts = (String[]) evt.getNewValue();
                                progress = Integer.parseInt(parts[0]);
                                paint = parts[1];
                            }
                            if (progress >= 100) {
                                progress = 100;
                            }
                            if (progress < 0) {
                                if (subProgressBar.isStringPainted()) {
                                    subProgressBar.setStringPainted(false);
                                }
                                subProgressBar.setVisible(false);
                            } else {
                                if (!subProgressBar.isStringPainted()) {
                                    subProgressBar.setStringPainted(true);
                                }
                                if (paint != null) {
                                    subProgressBar.setString(paint);
                                }
                            }
                            subProgressBar.setValue(progress);
                        } else if ("subprogressint" == evt.getPropertyName()) {
                            if (subProgressBar.isStringPainted()) {
                                subProgressBar.setStringPainted(false);
                            }
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

    @Override
    public void dispose(){
        ((LauncherFrame) App.settings.getParent()).blur.setBlur(false);
        super.dispose();
    }
}
