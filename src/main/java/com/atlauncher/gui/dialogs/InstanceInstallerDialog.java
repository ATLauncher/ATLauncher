/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Pack;
import com.atlauncher.data.version.PackVersion;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
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
    private JLabel enableUserLockLabel;
    private JCheckBox enableUserLock;

    private PackVersion autoInstallVersion;
    private String shareCode;

    public InstanceInstallerDialog(Object object) {
        this(object, false, false, null, null, true);
    }

    public InstanceInstallerDialog(Pack pack, PackVersion version, String shareCode, boolean showModsChooser) {
        this(pack, false, false, version, shareCode, showModsChooser);
    }

    public InstanceInstallerDialog(Pack pack, boolean isServer) {
        this((Object) pack, false, true, null, null, true);
    }

    public InstanceInstallerDialog(Object object, final boolean isUpdate, final boolean isServer, final PackVersion
            autoInstallVersion, final String shareCode, final boolean showModsChooser) {
        super(App.settings.getParent(), ModalityType.APPLICATION_MODAL);

        this.autoInstallVersion = autoInstallVersion;
        this.shareCode = shareCode;

        if (object instanceof Pack) {
            pack = (Pack) object;
            setTitle(LanguageManager.localize("common.installing") + " " + pack.getName());
            if (isServer) {
                setTitle(LanguageManager.localize("common.installing") + " " + pack.getName() + " " + LanguageManager
                        .localize("common.server"));
                this.isServer = true;
            }
        } else {
            instance = (Instance) object;
            pack = instance.getRealPack();
            isReinstall = true; // We're reinstalling
            setTitle(LanguageManager.localize("common.reinstalling") + " " + instance.getName());
        }
        setSize(400, 225);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(((isReinstall) ? LanguageManager.localize("common.reinstalling") : LanguageManager
                .localize("common.installing")) + " " + pack.getName()));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        if (!this.isServer) {
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            instanceNameLabel = new JLabel(LanguageManager.localize("instance.name") + ": ");
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
        versionLabel = new JLabel(LanguageManager.localize("instance.versiontoinstall") + ": ");
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

        if (autoInstallVersion != null) {
            versionsDropDown.setSelectedItem(autoInstallVersion);
            versionsDropDown.setEnabled(false);
        }

        if (!this.isServer) {
            if (!isReinstall) {
                gbc.gridx = 0;
                gbc.gridy++;
                gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
                enableUserLockLabel = new JLabel(LanguageManager.localize("instance.enableuserlock") + "? ");
                middle.add(enableUserLockLabel, gbc);

                gbc.gridx++;
                gbc.anchor = GridBagConstraints.BASELINE_LEADING;
                enableUserLock = new JCheckBox();
                enableUserLock.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (enableUserLock.isSelected()) {
                            String[] options = {LanguageManager.localize("common.yes"), LanguageManager.localize
                                    ("common.no")};

                            int ret = JOptionPane.showOptionDialog(null, HTMLUtils.centerParagraph(LanguageManager
                                    .localizeWithReplace("instance.userlockhelp", "<br/>")), LanguageManager
                                    .localize("instance.userlocktitle"), JOptionPane.DEFAULT_OPTION, JOptionPane
                                    .WARNING_MESSAGE, null, options, options[0]);

                            if (ret != 0) {
                                enableUserLock.setSelected(false);
                            }
                        }
                    }
                });
                middle.add(enableUserLock, gbc);
            }
        }

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        install = new JButton(((isReinstall) ? (isUpdate ? LanguageManager.localize("common.update") :
                LanguageManager.localize("common.reinstall")) : LanguageManager.localize("common.install")));
        install.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isReinstall && !isServer && InstanceManager.isInstance(instanceNameField.getText())) {
                    instance = InstanceManager.getInstanceByName(instanceNameField.getText());
                    if (instance.getPackName().equalsIgnoreCase(pack.getName())) {
                        int ret = JOptionPane.showConfirmDialog(App.settings.getParent(), HTMLUtils.centerParagraph(LanguageManager.localize("common.error") +
                                "<br/><br/>" + LanguageManager.localizeWithReplace("instance" + "" +
                                ".alreadyinstance1", instanceNameField.getText() + "<br/><br/>")), LanguageManager
                                .localize("common.error"), JOptionPane.ERROR_MESSAGE);
                        if (ret != JOptionPane.YES_OPTION) {
                            return;
                        }
                        isReinstall = true;
                        if (instance == null) {
                            return;
                        }
                    } else {
                        JOptionPane.showMessageDialog(App.settings.getParent(), HTMLUtils.centerParagraph
                                (LanguageManager.localize("common.error") +
                                "<br/><br/>" + LanguageManager.localizeWithReplace("instance" + "" +
                                ".alreadyinstance", instanceNameField.getText() + "<br/><br/>")), LanguageManager
                                .localize("common.error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else if (!isReinstall && !isServer && instanceNameField.getText().replaceAll("[^A-Za-z0-9]", "")
                        .length() == 0) {
                    JOptionPane.showMessageDialog(App.settings.getParent(), HTMLUtils.centerParagraph(LanguageManager
                            .localize("common.error") + "<br/><br/>" + LanguageManager.localizeWithReplace("instance" +
                            ".invalidname", instanceNameField.getText())), LanguageManager.localize
                            ("common.error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                final PackVersion version = (PackVersion) versionsDropDown.getSelectedItem();
                final JDialog dialog = new JDialog(App.settings.getParent(), ((isReinstall) ? LanguageManager
                        .localize("common.reinstalling") : LanguageManager.localize("common.installing")) + " " +
                        pack.getName() + " " + version.getVersion() + ((isServer) ? " " + LanguageManager.localize
                        ("common.server") : ""), ModalityType.DOCUMENT_MODAL);
                dialog.setLocationRelativeTo(App.settings.getParent());
                dialog.setSize(300, 100);
                dialog.setResizable(false);

                JPanel topPanel = new JPanel();
                topPanel.setLayout(new BorderLayout());
                final JLabel doing = new JLabel(LanguageManager.localizeWithReplace("instance.startingprocess", (
                        (isReinstall) ? LanguageManager.localize("common.reinstall") : LanguageManager.localize
                                ("common.install"))));
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
                        .getText()), pack, version, isReinstall, shareCode, isServer, showModsChooser) {

                    protected void done() {
                        Boolean success = false;
                        int type;
                        String text;
                        String title;
                        if (isCancelled()) {
                            type = JOptionPane.ERROR_MESSAGE;
                            text = pack.getName() + " " + version.getVersion() + " " + LanguageManager.localize
                                    ("common.wasnt") + " " + ((isReinstall) ? LanguageManager.localize("common" + "" +
                                    ".reinstalled") : LanguageManager.localize("common.installed")) +
                                    "<br/><br/>" + LanguageManager.localize("instance" + ".checkerrorlogs");
                            title = pack.getName() + " " + version.getVersion() + " " + LanguageManager.localize
                                    ("common.not") + " " + ((isReinstall) ? LanguageManager.localize("common" + "" +
                                    ".reinstalled") : LanguageManager.localize("common.installed"));
                            if (isReinstall) {
                                if (this.corrupt) {
                                    InstanceManager.setInstanceUnplayable(instance);
                                }
                            }
                        } else {
                            try {
                                success = get();
                            } catch (InterruptedException | ExecutionException e) {
                                LogManager.logStackTrace(e);
                            }
                            if (success) {
                                type = JOptionPane.INFORMATION_MESSAGE;
                                text = pack.getName() + " " + version.getVersion() + " " + LanguageManager.localize
                                        ("common.hasbeen") + " " + ((isReinstall) ? LanguageManager.localize("common"
                                        + ".reinstalled") : LanguageManager.localize("common.installed")) +
                                        "<br/><br/>" + ((isServer) ? LanguageManager.localizeWithReplace("instance" +
                                        ".finditserver", "<br/><br/>" + this.root) : LanguageManager.localize
                                        ("instance.findit"));
                                title = pack.getName() + " " + packVersion.getVersion() + " " + LanguageManager
                                        .localize("common.installed");
                                if (isReinstall) {
                                    instance.setVersion(packVersion.getVersion());
                                    instance.setMinecraftVersion(packVersion.getMinecraftVersion().getVersion());
                                    instance.setModsInstalled(this.installedMods);
                                    instance.setJarOrder(this.jarOrder);
                                    instance.setMemory(this.memory);
                                    instance.setPermgen(this.permgen);
                                    instance.setIsNewLaunchMethod(!this.packVersion.getMinecraftVersion().isLegacy());
                                    instance.setLibrariesNeeded(this.librariesNeeded);
                                    instance.setMinecraftArguments(this.packVersion.getMinecraftVersion()
                                            .getMojangVersion().getMinecraftArguments());
                                    instance.setExtraArguments(this.extraArgs);
                                    instance.setMainClass(this.mainClass);
                                    instance.setAssets(this.packVersion.getMinecraftVersion().getMojangVersion()
                                            .getAssets());
                                    if (packVersion.isDev()) {
                                        instance.setDevVersion();
                                        if (packVersion.getHash() != null) {
                                            instance.setHash(packVersion.getHash());
                                        }
                                    } else {
                                        instance.setNotDevVersion();
                                    }
                                    if (!instance.isPlayable()) {
                                        instance.setPlayable();
                                    }
                                } else if (isServer) {

                                } else {
                                    Instance newInstance = new Instance(instanceNameField.getText(), pack.getName(),
                                            pack, enableUserLock.isSelected(), version.getVersion(), packVersion
                                            .getMinecraftVersion().getVersion(), this.memory, this.permgen, this
                                            .installedMods, this.jarOrder, this.librariesNeeded, this.extraArgs, this
                                            .packVersion.getMinecraftVersion().getMojangVersion()
                                            .getMinecraftArguments(), this.mainClass, this.packVersion
                                            .getMinecraftVersion().getMojangVersion().getAssets(), this.packVersion
                                            .isDev(), !packVersion.getMinecraftVersion().isLegacy());

                                    if (packVersion.isDev() && (packVersion.getHash() != null)) {
                                        newInstance.setHash(packVersion.getHash());
                                    }

                                    InstanceManager.addInstance(newInstance);

                                }
                                InstanceManager.saveInstances();
                                EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.InstancesChangeEvent
                                        .class));
                                if (pack.isLoggingEnabled() && SettingsManager.enableLogs() && !packVersion.isDev()) {
                                    if (isServer) {
                                        pack.addServerInstall(packVersion.getVersion());
                                    } else if (isUpdate) {
                                        pack.addUpdate(packVersion.getVersion());
                                    } else {
                                        pack.addInstall(packVersion.getVersion());
                                    }
                                }
                            } else {
                                if (isReinstall) {
                                    type = JOptionPane.ERROR_MESSAGE;
                                    text = pack.getName() + " " + packVersion.getVersion() + " " + LanguageManager
                                            .localize("common.wasnt") + " " + LanguageManager.localize("common" + "" +
                                            ".reinstalled") + "<br/><br/>" + (this.corrupt ? LanguageManager
                                            .localize("instance.nolongerplayable") : "") + "<br/><br/>" +
                                            LanguageManager.localize("instance.checkerrorlogs") + "!";
                                    title = pack.getName() + " " + packVersion.getVersion() + " " + LanguageManager.localize("common.not") + " " + LanguageManager.localize("common" + "" +
                                            ".reinstalled");
                                    if (this.corrupt) {
                                        InstanceManager.setInstanceUnplayable(instance);
                                    }
                                } else {
                                    // Install failed so delete the folder and clear Temp Dir
                                    FileUtils.delete(this.root);
                                    type = JOptionPane.ERROR_MESSAGE;
                                    text = pack.getName() + " " + packVersion.getVersion() + " " + LanguageManager
                                            .localize("common.wasnt") + " " + LanguageManager.localize("common" + "" +
                                            ".installed") + "<br/><br/>" + LanguageManager.localize("instance" + "" +
                                            ".checkerrorlogs") + "!";
                                    title = pack.getName() + " " + packVersion.getVersion() + " " + LanguageManager.localize("common.not") + " " + LanguageManager.localize("common" + "" +
                                            ".installed");
                                }
                            }
                        }

                        dialog.dispose();

                        Utils.cleanTempDirectory();

                        JOptionPane.showMessageDialog(App.settings.getParent(), HTMLUtils.centerParagraph(text),
                                title, type);
                    }

                };

                instanceInstaller.addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("progress".equals(evt.getPropertyName())) {
                            if (progressBar.isIndeterminate()) {
                                progressBar.setIndeterminate(false);
                            }
                            int progress = (Integer) evt.getNewValue();
                            if (progress > 100) {
                                progress = 100;
                            }
                            progressBar.setValue(progress);
                        } else if ("subprogress".equals(evt.getPropertyName())) {
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
                        } else if ("subprogressint".equals(evt.getPropertyName())) {
                            if (subProgressBar.isStringPainted()) {
                                subProgressBar.setStringPainted(false);
                            }
                            if (!subProgressBar.isVisible()) {
                                subProgressBar.setVisible(true);
                            }
                            if (!subProgressBar.isIndeterminate()) {
                                subProgressBar.setIndeterminate(true);
                            }
                        } else if ("doing".equals(evt.getPropertyName())) {
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
        cancel = new JButton(LanguageManager.localize("common.cancel"));
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
