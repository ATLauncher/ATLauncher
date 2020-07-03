/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.data.curse.pack.CurseManifest;
import com.atlauncher.data.json.Version;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseApi;
import com.atlauncher.workers.InstanceInstaller;

import org.mini2Dx.gettext.GetText;

public class InstanceInstallerDialog extends JDialog {
    private static final long serialVersionUID = -6984886874482721558L;
    private int versionLength = 0;
    private int loaderVersionLength = 0;
    private boolean isReinstall = false;
    private boolean isServer = false;
    private Pack pack = null;
    private Instance instance = null;
    private InstanceV2 instanceV2 = null;
    private CurseManifest curseManifest = null;

    private JPanel top;
    private JPanel middle;
    private JPanel bottom;
    private JButton install;
    private JButton cancel;
    private JProgressBar progressBar;
    private JProgressBar subProgressBar;
    private JLabel instanceNameLabel;
    private JTextField nameField;
    private JLabel versionLabel;
    private JComboBox<PackVersion> versionsDropDown;
    private List<PackVersion> versions = new ArrayList<>();
    private JLabel loaderVersionLabel;
    private JComboBox<LoaderVersion> loaderVersionsDropDown;
    private List<LoaderVersion> loaderVersions = new ArrayList<>();
    private JLabel enableUserLockLabel;
    private JCheckBox enableUserLock;
    private JLabel saveModsLabel;
    private JCheckBox saveModsCheckbox;
    private boolean isUpdate;
    private PackVersion autoInstallVersion;

    public InstanceInstallerDialog(CurseManifest manifest, File manifestFile) {
        this(manifest, false, false, null, null, false, manifestFile);
    }

    public InstanceInstallerDialog(Object object) {
        this(object, false, false, null, null, true, null);
    }

    public InstanceInstallerDialog(Pack pack, PackVersion version, String shareCode, boolean showModsChooser) {
        this(pack, false, false, version, shareCode, showModsChooser, null);
    }

    public InstanceInstallerDialog(Pack pack, boolean isServer) {
        this((Object) pack, false, true, null, null, true, null);
    }

    public InstanceInstallerDialog(Object object, final boolean isUpdate, final boolean isServer,
            final PackVersion autoInstallVersion, final String shareCode, final boolean showModsChooser,
            File manifestFile) {
        super(App.settings.getParent(), ModalityType.APPLICATION_MODAL);

        this.isUpdate = isUpdate;
        this.autoInstallVersion = autoInstallVersion;

        Analytics.sendScreenView("Instance Installer Dialog");

        if (object instanceof Pack) {
            pack = (Pack) object;
            // #. {0} is the name of the pack the user is installing
            setTitle(GetText.tr("Installing {0}", pack.getName()));
            if (isServer) {
                // #. {0} is the name of the pack the user is installing
                setTitle(GetText.tr("Installing {0} Server", pack.getName()));
                this.isServer = true;
            }
        } else if (object instanceof Instance) {
            instance = (Instance) object;
            pack = instance.getRealPack();
            isReinstall = true; // We're reinstalling
            // #. {0} is the name of the pack the user is reinstalling
            setTitle(GetText.tr("Reinstalling {0}", instance.getName()));
        } else if (object instanceof CurseManifest) {
            curseManifest = (CurseManifest) object;

            pack = new Pack();
            pack.name = curseManifest.name;

            if (curseManifest.projectID != null) {
                CurseMod cursePack = CurseApi.getModById(curseManifest.projectID);

                curseManifest.websiteUrl = cursePack.websiteUrl;

                pack.id = curseManifest.projectID;
                pack.description = cursePack.summary;
                pack.cursePack = cursePack;
            }

            PackVersion packVersion = new PackVersion();
            packVersion.version = curseManifest.version;

            try {
                packVersion.minecraftVersion = App.settings.getMinecraftVersion(curseManifest.minecraft.version);
            } catch (InvalidMinecraftVersion e) {
                LogManager.error(e.getMessage());
                return;
            }

            packVersion.hasLoader = true;

            pack.versions = Arrays.asList(packVersion);

            isReinstall = false;

            // #. {0} is the name of the pack the user is installing from Curse
            setTitle(GetText.tr("Installing {0} From Curse", curseManifest.name));
        } else {
            instanceV2 = (InstanceV2) object;
            pack = instanceV2.getPack();
            isReinstall = true; // We're reinstalling
            // #. {0} is the name of the pack the user is installing
            setTitle(GetText.tr("Reinstalling {0}", instanceV2.launcher.name));
        }
        setSize(400, 225);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        install = new JButton(
                ((isReinstall) ? (isUpdate ? GetText.tr("Update") : GetText.tr("Reinstall")) : GetText.tr("Install")));

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(
                ((isReinstall) ? GetText.tr("Reinstalling") : GetText.tr("Installing")) + " " + pack.getName()));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(2, 0, 2, 10);
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        instanceNameLabel = new JLabel(GetText.tr("Name") + ": ");
        middle.add(instanceNameLabel, gbc);

        gbc.gridx++;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        nameField = new JTextField(17);
        nameField.setText(((isReinstall) ? (instanceV2 != null ? instanceV2.launcher.name : instance.getName())
                : pack.getName()));
        if (isReinstall) {
            nameField.setEnabled(false);
        }
        nameField.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                nameField.requestFocusInWindow();
            }
        });
        nameField.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(final FocusEvent pE) {
            }

            @Override
            public void focusGained(final FocusEvent pE) {
                nameField.selectAll();
            }
        });
        middle.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 2, 10);

        gbc = this.setupVersionsDropdown(gbc);
        gbc = this.setupLoaderVersionsDropdown(gbc);

        if (!this.isServer && !isReinstall) {
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = new Insets(2, 0, 2, 10);
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            enableUserLockLabel = new JLabel(GetText.tr("Enable User Lock") + "? ");
            middle.add(enableUserLockLabel, gbc);

            gbc.gridx++;
            gbc.insets = new Insets(2, 0, 2, 0);
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            enableUserLock = new JCheckBox();
            enableUserLock.addActionListener(e -> {
                if (enableUserLock.isSelected()) {
                    int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Enable User Lock") + "? ")
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "Enabling the user lock setting will lock this instance to only be played<br/>by the person installing this instance (you) and will not show the instance to anyone else.<br/><br/>Are you sure you want to do this?"))
                                    .build())
                            .setType(DialogManager.WARNING).show();

                    if (ret != 0) {
                        enableUserLock.setSelected(false);
                    }
                }
            });
            middle.add(enableUserLock, gbc);
        }

        if (!this.isServer && isReinstall) {
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = new Insets(2, 0, 2, 10);
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            saveModsLabel = new JLabel(GetText.tr("Save Mods") + "? ");
            middle.add(saveModsLabel, gbc);

            gbc.gridx++;
            gbc.insets = new Insets(2, 0, 2, 0);
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            saveModsCheckbox = new JCheckBox();
            saveModsCheckbox.addActionListener(e -> {
                if (saveModsCheckbox.isSelected()) {
                    int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Save Mods") + "? ")
                            .setContent(new HTMLBuilder().center().text(GetText.tr(
                                    "Since this update changes the Minecraft version, your custom mods may no longer work.<br/><br/>Checking this box will keep your custom mods, otherwise they'll be removed.<br/><br/>Are you sure you want to do this?"))
                                    .build())
                            .setType(DialogManager.INFO).show();

                    if (ret != 0) {
                        saveModsCheckbox.setSelected(false);
                    }
                }
            });

            saveModsLabel.setVisible(!((PackVersion) versionsDropDown.getSelectedItem()).minecraftVersion.version
                    .equalsIgnoreCase(instanceV2 != null ? this.instanceV2.id : this.instance.getMinecraftVersion()));
            saveModsCheckbox.setVisible(!((PackVersion) versionsDropDown.getSelectedItem()).minecraftVersion.version
                    .equalsIgnoreCase(instanceV2 != null ? this.instanceV2.id : this.instance.getMinecraftVersion()));

            middle.add(saveModsCheckbox, gbc);
        }

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        install.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isReinstall && !isServer && App.settings.isInstance(nameField.getText())) {
                    DialogManager.okDialog().setTitle(GetText.tr("Error"))
                            .setContent(new HTMLBuilder().center().text(GetText
                                    .tr("An instance already exists with that name.<br/><br/>Rename it and try again."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return;
                } else if (!isReinstall && !isServer
                        && nameField.getText().replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                    DialogManager.okDialog().setTitle(GetText.tr("Error")).setContent(new HTMLBuilder().center()
                            .text(GetText.tr("Instance name is invalid. It must contain at least 1 letter or number."))
                            .build()).setType(DialogManager.ERROR).show();
                    return;
                } else if (!isReinstall && isServer && App.settings.isServer(nameField.getText())) {
                    DialogManager.okDialog().setTitle(GetText.tr("Error"))
                            .setContent(new HTMLBuilder().center().text(GetText
                                    .tr("A server already exists with that name.<br/><br/>Rename it and try again."))
                                    .build())
                            .setType(DialogManager.ERROR).show();
                    return;
                } else if (!isReinstall && isServer
                        && nameField.getText().replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                    DialogManager.okDialog().setTitle(GetText.tr("Error")).setContent(new HTMLBuilder().center()
                            .text(GetText.tr("Server name is invalid. It must contain at least 1 letter or number."))
                            .build()).setType(DialogManager.ERROR).show();
                    return;
                }

                final PackVersion version = (PackVersion) versionsDropDown.getSelectedItem();
                final JDialog dialog = new JDialog(App.settings.getParent(), isReinstall ? (
                // #. {0} is the name of the pack the user is installing
                isServer ? GetText.tr("Reinstalling {0} Server", pack.getName())
                        // #. {0} is the name of the pack the user is installing
                        : GetText.tr("Reinstalling {0}", pack.getName())) : (
                // #. {0} is the name of the pack the user is installing
                isServer ? GetText.tr("Installing {0} Server", pack.getName())
                        // #. {0} is the name of the pack the user is installing
                        : GetText.tr("Installing {0}", pack.getName())), ModalityType.DOCUMENT_MODAL);
                dialog.setLocationRelativeTo(App.settings.getParent());
                dialog.setSize(300, 100);
                dialog.setResizable(false);

                JPanel topPanel = new JPanel();
                topPanel.setLayout(new BorderLayout());
                final JLabel doing = new JLabel((isReinstall) ? GetText.tr("Starting Reinstall Process")
                        : GetText.tr("Starting Install Process"));
                doing.setHorizontalAlignment(JLabel.CENTER);
                doing.setVerticalAlignment(JLabel.TOP);
                topPanel.add(doing);

                JPanel bottomPanel = new JPanel();
                bottomPanel.setLayout(new BorderLayout());
                progressBar = new JProgressBar(0, 10000);
                bottomPanel.add(progressBar, BorderLayout.NORTH);
                progressBar.setIndeterminate(true);
                subProgressBar = new JProgressBar(0, 10000);
                bottomPanel.add(subProgressBar, BorderLayout.SOUTH);
                subProgressBar.setValue(0);
                subProgressBar.setVisible(false);

                dialog.add(topPanel, BorderLayout.CENTER);
                dialog.add(bottomPanel, BorderLayout.SOUTH);

                LoaderVersion loaderVersion = (version.hasLoader() && version.hasChoosableLoader())
                        ? (LoaderVersion) loaderVersionsDropDown.getSelectedItem()
                        : null;

                boolean saveMods = !isServer && isReinstall && saveModsCheckbox.isSelected();

                final InstanceInstaller instanceInstaller = new InstanceInstaller(nameField.getText(), pack, version,
                        isReinstall, isServer, saveMods, shareCode, showModsChooser, loaderVersion, curseManifest,
                        manifestFile) {

                    protected void done() {
                        Boolean success = false;
                        int type;
                        String text;
                        String title;
                        if (isCancelled()) {
                            type = DialogManager.ERROR;

                            if (isReinstall) {
                                // #. {0} is the pack name and {1} is the pack version
                                title = GetText.tr("{0} {1} Not Reinstalled", pack.getName(), version.version);

                                // #. {0} is the pack name and {1} is the pack version
                                text = GetText.tr(
                                        "{0} {1} wasn't reinstalled.<br/><br/>Check error logs for more information.",
                                        pack.getName(), version.version);

                                if (instanceIsCorrupt) {
                                    if (instance != null) {
                                        App.settings.setInstanceUnplayable(instance);
                                    }
                                }
                            } else {
                                // #. {0} is the pack name and {1} is the pack version
                                title = GetText.tr("{0} {1} Not Installed", pack.getName(), version.version);

                                // #. {0} is the pack name and {1} is the pack version
                                text = GetText.tr(
                                        "{0} {1} wasn't installed.<br/><br/>Check error logs for more information.",
                                        pack.getName(), version.version);
                            }
                        } else {
                            type = DialogManager.INFO;

                            try {
                                success = get();
                            } catch (InterruptedException ignored) {
                                Thread.currentThread().interrupt();
                                return;
                            } catch (ExecutionException e) {
                                LogManager.logStackTrace(e);
                            }

                            if (success) {
                                type = DialogManager.INFO;

                                // #. {0} is the pack name and {1} is the pack version
                                title = GetText.tr("{0} {1} Installed", pack.getName(), version.version);

                                if (isReinstall) {
                                    // #. {0} is the pack name and {1} is the pack version
                                    text = GetText.tr("{0} {1} has been reinstalled.", pack.getName(), version.version);
                                } else if (isServer) {
                                    // #. {0} is the pack name and {1} is the pack version
                                    text = GetText.tr(
                                            "{0} {1} server has been installed.<br/><br/>Find it in the servers tab.",
                                            pack.getName(), version.version);
                                } else {
                                    // #. {0} is the pack name and {1} is the pack version
                                    text = GetText.tr(
                                            "{0} {1} has been installed.<br/><br/>Find it in the instances tab.",
                                            pack.getName(), version.version);
                                }

                                if (isServer) {
                                    App.settings.reloadServersPanel();
                                } else {
                                    App.settings.reloadInstancesPanel();
                                }

                                if (pack.isLoggingEnabled() && App.settings.enableLogs() && !version.isDev) {
                                    if (isServer) {
                                        pack.addServerInstall(version.version);
                                    } else if (isUpdate) {
                                        pack.addUpdate(version.version);
                                    } else {
                                        pack.addInstall(version.version);
                                    }
                                }
                            } else {

                                if (isReinstall) {
                                    // #. {0} is the pack name and {1} is the pack version
                                    title = GetText.tr("{0} {1} Not Reinstalled", pack.getName(), version.version);

                                    // #. {0} is the pack name and {1} is the pack version
                                    text = GetText.tr(
                                            "{0} {1} wasn't reinstalled.<br/><br/>Check error logs for more information.",
                                            pack.getName(), version.version);

                                    if (instanceIsCorrupt) {
                                        if (instance != null) {
                                            App.settings.setInstanceUnplayable(instance);
                                        }
                                    }
                                } else {
                                    // #. {0} is the pack name and {1} is the pack version
                                    title = GetText.tr("{0} {1} Not Installed", pack.getName(), version.version);

                                    // #. {0} is the pack name and {1} is the pack version
                                    text = GetText.tr(
                                            "{0} {1} wasn't installed.<br/><br/>Check error logs for more information.",
                                            pack.getName(), version.version);
                                }
                            }
                        }

                        dialog.dispose();

                        DialogManager.okDialog().setTitle(title)
                                .setContent(new HTMLBuilder().center().text(text).build()).setType(type).show();
                    }

                };
                instanceInstaller.addPropertyChangeListener(evt -> {
                    if ("progress" == evt.getPropertyName()) {
                        if (progressBar.isIndeterminate()) {
                            progressBar.setIndeterminate(false);
                        }
                        double progress = 0.0;
                        if (evt.getNewValue() instanceof Double) {
                            progress = (Double) evt.getNewValue();
                        } else if (evt.getNewValue() instanceof Integer) {
                            progress = ((Integer) evt.getNewValue()) * 100.0;
                        }
                        if (progress > 100.0) {
                            progress = 100.0;
                        }
                        progressBar.setValue((int) Math.round(progress * 100.0));
                    } else if ("subprogress" == evt.getPropertyName()) {
                        if (!subProgressBar.isVisible()) {
                            subProgressBar.setVisible(true);
                        }
                        if (subProgressBar.isIndeterminate()) {
                            subProgressBar.setIndeterminate(false);
                        }
                        double progress;
                        String paint = null;
                        if (evt.getNewValue() instanceof Double) {
                            progress = (Double) evt.getNewValue();
                        } else if (evt.getNewValue() instanceof Integer) {
                            progress = ((Integer) evt.getNewValue()) * 100.0;
                        } else {
                            String[] parts = (String[]) evt.getNewValue();
                            progress = Double.parseDouble(parts[0]);
                            paint = parts[1];
                        }
                        if (progress >= 100.0) {
                            progress = 100.0;
                        }
                        if (progress < 0.0) {
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
                        if (paint == null && progress > 0.0) {
                            subProgressBar.setString(String.format("%.2f%%", progress));
                        }
                        subProgressBar.setValue((int) Math.round(progress * 100.0));
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

                });
                dialog.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        instanceInstaller.cancel(true);
                    }
                });
                if (isReinstall) {
                    if (instanceV2 != null) {
                        instanceInstaller.setInstance(instanceV2);
                    } else {
                        instanceInstaller.setInstance(instance);
                    }
                }
                instanceInstaller.execute();
                dispose();
                dialog.setVisible(true);

            }
        });
        cancel = new JButton(GetText.tr("Cancel"));
        cancel.addActionListener(e -> dispose());
        bottom.add(install);
        bottom.add(cancel);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setVisible(true);
    }

    private GridBagConstraints setupVersionsDropdown(GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        versionLabel = new JLabel(GetText.tr("Version To Install") + ": ");
        middle.add(versionLabel, gbc);

        gbc.gridx++;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        versionsDropDown = new JComboBox<>();
        if (pack.isTester()) {
            for (PackVersion pv : pack.getDevVersions()) {
                if (!isServer || (isServer && pv.minecraftVersion.server)) {
                    versions.add(pv);
                }
            }
        }
        for (PackVersion pv : pack.getVersions()) {
            if (!isServer || (isServer && pv.minecraftVersion.server)) {
                versions.add(pv);
            }
        }
        PackVersion forUpdate = null;
        for (PackVersion version : versions) {
            if ((!version.isDev) && (forUpdate == null)) {
                forUpdate = version;
            }
            versionsDropDown.addItem(version);
        }
        if (isUpdate && forUpdate != null) {
            versionsDropDown.setSelectedItem(forUpdate);
        } else if (isReinstall) {
            for (PackVersion version : versions) {
                if (version
                        .versionMatches((instanceV2 != null ? instanceV2.launcher.version : instance.getVersion()))) {
                    versionsDropDown.setSelectedItem(version);
                }
            }
        } else {
            for (PackVersion version : versions) {
                if (!version.isRecommended || version.isDev) {
                    continue;
                }
                versionsDropDown.setSelectedItem(version);
                break;
            }
        }

        // ensures that font width is taken into account
        for (PackVersion version : versions) {
            versionLength = Math.max(versionLength,
                    getFontMetrics(App.THEME.getNormalFont()).stringWidth(version.toString()) + 25);
        }

        // ensures that the dropdown is at least 200 px wide
        versionLength = Math.max(200, versionLength);

        // ensures that there is a maximum width of 250 px to prevent overflow
        versionLength = Math.min(250, versionLength);

        versionsDropDown.setPreferredSize(new Dimension(versionLength, 23));
        middle.add(versionsDropDown, gbc);

        versionsDropDown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateLoaderVersions((PackVersion) e.getItem());

                if (!isServer && isReinstall) {
                    this.saveModsLabel
                            .setVisible(!((PackVersion) e.getItem()).minecraftVersion.version.equalsIgnoreCase(
                                    instanceV2 != null ? this.instanceV2.id : this.instance.getMinecraftVersion()));
                    this.saveModsCheckbox
                            .setVisible(!((PackVersion) e.getItem()).minecraftVersion.version.equalsIgnoreCase(
                                    instanceV2 != null ? this.instanceV2.id : this.instance.getMinecraftVersion()));
                }
            }
        });

        if (autoInstallVersion != null) {
            versionsDropDown.setSelectedItem(autoInstallVersion);
            versionsDropDown.setEnabled(false);
        }

        return gbc;
    }

    protected void updateLoaderVersions(PackVersion item) {
        if (!item.hasLoader() || !item.hasChoosableLoader()) {
            loaderVersionLabel.setVisible(false);
            loaderVersionsDropDown.setVisible(false);
            return;
        }

        loaderVersionsDropDown.setEnabled(false);
        loaderVersions.clear();

        loaderVersionsDropDown.removeAllItems();
        loaderVersionsDropDown.addItem(new LoaderVersion(GetText.tr("Getting Loader Versions")));

        loaderVersionLabel.setVisible(true);
        loaderVersionsDropDown.setVisible(true);

        install.setEnabled(false);
        versionsDropDown.setEnabled(false);

        Runnable r = () -> {
            Version jsonVersion = Gsons.DEFAULT.fromJson(pack.getJSON(item.version), Version.class);

            if (jsonVersion == null) {
                return;
            }

            loaderVersions.clear();
            loaderVersions.addAll(jsonVersion.getLoader().getChoosableVersions(jsonVersion.getMinecraft()));

            // ensures that font width is taken into account
            for (LoaderVersion version : loaderVersions) {
                loaderVersionLength = Math.max(loaderVersionLength,
                        getFontMetrics(App.THEME.getNormalFont()).stringWidth(version.toString()) + 25);
            }

            loaderVersionsDropDown.removeAllItems();

            loaderVersions.stream().forEach(version -> loaderVersionsDropDown.addItem(version));

            if (isReinstall && (instanceV2 != null ? instanceV2.launcher.loaderVersion != null
                    : instance.installedWithLoaderVersion())) {
                String loaderVersionString = (instanceV2 != null ? instanceV2.launcher.loaderVersion.version
                        : instance.getLoaderVersion().version);

                for (int i = 0; i < loaderVersionsDropDown.getItemCount(); i++) {
                    LoaderVersion loaderVersion = loaderVersionsDropDown.getItemAt(i);

                    if (loaderVersion.version.equals(loaderVersionString)) {
                        loaderVersionsDropDown.setSelectedItem(loaderVersion);
                        break;
                    }
                }
            }

            // ensures that the dropdown is at least 200 px wide
            loaderVersionLength = Math.max(200, loaderVersionLength);

            // ensures that there is a maximum width of 250 px to prevent overflow
            loaderVersionLength = Math.min(250, loaderVersionLength);

            loaderVersionsDropDown.setPreferredSize(new Dimension(loaderVersionLength, 25));

            loaderVersionsDropDown.setEnabled(true);
            loaderVersionLabel.setVisible(true);
            loaderVersionsDropDown.setVisible(true);
            install.setEnabled(true);
            versionsDropDown.setEnabled(true);
        };

        new Thread(r).start();
    }

    private GridBagConstraints setupLoaderVersionsDropdown(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(2, 0, 2, 10);
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        loaderVersionLabel = new JLabel(GetText.tr("Loader Version") + ": ");
        middle.add(loaderVersionLabel, gbc);

        gbc.gridx++;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        loaderVersionsDropDown = new JComboBox<>();
        this.updateLoaderVersions((PackVersion) this.versionsDropDown.getSelectedItem());
        middle.add(loaderVersionsDropDown, gbc);

        return gbc;
    }
}
