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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.MinecraftVersion;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.json.Version;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.modpacksch.ModpacksChPackLink;
import com.atlauncher.data.modpacksch.ModpacksChPackLinkType;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.data.modpacksch.ModpacksChPackVersion;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import org.mini2Dx.gettext.GetText;

import okhttp3.CacheControl;

public class InstanceInstallerDialog extends JDialog {
    private static final long serialVersionUID = -6984886874482721558L;
    private int versionLength = 0;
    private int loaderVersionLength = 0;
    private boolean isReinstall = false;
    private boolean isServer = false;
    private Pack pack;
    private Instance instance = null;
    private CurseForgeManifest curseForgeManifest = null;
    private CurseForgeProject curseForgeProject = null;
    private ModpacksChPackManifest modpacksChPackManifest = null;
    private MultiMCManifest multiMCManifest = null;

    private JPanel middle;
    private JButton install;
    private JProgressBar progressBar;
    private JProgressBar subProgressBar;
    private JTextField nameField;
    private JComboBox<PackVersion> versionsDropDown;
    private final List<PackVersion> versions = new ArrayList<>();
    private JLabel loaderVersionLabel;
    private JComboBox<LoaderVersion> loaderVersionsDropDown;
    private final List<LoaderVersion> loaderVersions = new ArrayList<>();
    private JCheckBox enableUserLock;
    private JLabel saveModsLabel;
    private JCheckBox saveModsCheckbox;
    private final boolean isUpdate;
    private final PackVersion autoInstallVersion;

    public InstanceInstallerDialog(CurseForgeManifest manifest, Path curseExtractedPath) {
        this(manifest, false, false, null, null, false, curseExtractedPath, null, App.launcher.getParent());
    }

    public InstanceInstallerDialog(MultiMCManifest manifest, Path multiMCExtractedPath) {
        this(manifest, false, false, null, null, false, null, multiMCExtractedPath, App.launcher.getParent());
    }

    public InstanceInstallerDialog(Object object) {
        this(object, false, false, null, null, true, null, null, App.launcher.getParent());
    }

    public InstanceInstallerDialog(Window parent, Object object) {
        this(object, false, false, null, null, true, null, null, parent);
    }

    public InstanceInstallerDialog(Pack pack, PackVersion version, String shareCode, boolean showModsChooser) {
        this(pack, false, false, version, shareCode, showModsChooser, null, null, App.launcher.getParent());
    }

    public InstanceInstallerDialog(Pack pack, boolean isServer) {
        this(pack, false, true, null, null, true, null, null, App.launcher.getParent());
    }

    public InstanceInstallerDialog(Object object, boolean isUpdate, boolean isServer, PackVersion autoInstallVersion,
            String shareCode, boolean showModsChooser, Path curseExtractedPath, Path multiMCExtractedPath) {
        this(object, isUpdate, isServer, autoInstallVersion, shareCode, showModsChooser, curseExtractedPath,
                multiMCExtractedPath, App.launcher.getParent());
    }

    public InstanceInstallerDialog(Object object, final boolean isUpdate, final boolean isServer,
            final PackVersion autoInstallVersion, final String shareCode, final boolean showModsChooser,
            Path curseExtractedPath, Path multiMCExtractedPath, Window parent) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        setName("instanceInstallerDialog");
        this.isUpdate = isUpdate;
        this.autoInstallVersion = autoInstallVersion;

        Analytics.sendScreenView("Instance Installer Dialog");

        if (object instanceof Pack) {
            handlePackInstall(object, isServer);
        } else if (object instanceof CurseForgeProject) {
            handleCurseForgeInstall(object);
        } else if (object instanceof ModpacksChPackManifest) {
            handleModpacksChInstall(object);
        } else if (object instanceof CurseForgeManifest) {
            handleCurseForgeImport(object);
        } else if (object instanceof MultiMCManifest) {
            handleMultiMcImport(object);
        } else {
            handleInstanceInstall(object);
        }

        setSize(450, 240);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        install = new JButton(
                ((isReinstall) ? (isUpdate ? GetText.tr("Update") : GetText.tr("Reinstall")) : GetText.tr("Install")));

        // Top Panel Stuff
        JPanel top = new JPanel();
        top.add(new JLabel(((isReinstall) ? (isUpdate ? GetText.tr("Updating") : GetText.tr("Reinstalling"))
                : GetText.tr("Installing")) + " " + pack.getName()));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel instanceNameLabel = new JLabel(GetText.tr("Name") + ": ");
        middle.add(instanceNameLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        nameField = new JTextField(17);
        nameField.setText(((isReinstall) ? instance.launcher.name : pack.getName()));
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
        gbc.insets = UIConstants.LABEL_INSETS;

        gbc = this.setupVersionsDropdown(gbc);
        gbc = this.setupLoaderVersionsDropdown(gbc);

        if (!this.isServer && !isReinstall) {
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            JLabel enableUserLockLabel = new JLabelWithHover(GetText.tr("Enable User Lock") + "?",
                    Utils.getIconImage("/assets/image/Help.png"),
                    new HTMLBuilder().center().text(GetText.tr(
                            "Enabling the user lock setting will lock this instance to only be played<br/>by the person installing this instance (you) and will not show the instance to anyone else."))
                            .build());
            middle.add(enableUserLockLabel, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            enableUserLock = new JCheckBox();
            middle.add(enableUserLock, gbc);
        }

        if (!this.isServer && isReinstall) {
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            saveModsLabel = new JLabelWithHover(GetText.tr("Save Mods") + "? ",
                    Utils.getIconImage("/assets/image/Help.png"),
                    new HTMLBuilder().center().text(GetText.tr(
                            "Since this update changes the Minecraft version, your custom mods may no longer work.<br/><br/>Checking this box will keep your custom mods, otherwise they'll be removed."))
                            .build());
            middle.add(saveModsLabel, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.FIELD_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            saveModsCheckbox = new JCheckBox();

            PackVersion packVersion = ((PackVersion) versionsDropDown.getSelectedItem());
            Optional<MinecraftVersion> minecraftVersion = Optional.ofNullable(packVersion.minecraftVersion);

            saveModsLabel.setVisible(
                    minecraftVersion.isPresent() && !minecraftVersion.get().version.equalsIgnoreCase(this.instance.id));
            saveModsCheckbox.setVisible(
                    minecraftVersion.isPresent() && !minecraftVersion.get().version.equalsIgnoreCase(this.instance.id));

            middle.add(saveModsCheckbox, gbc);
        }

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        install.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isReinstall && !isServer && InstanceManager.isInstance(nameField.getText())) {
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
                } else if (!isReinstall && isServer && ServerManager.isServer(nameField.getText())) {
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

                final PackVersion version = ((PackVersion) versionsDropDown.getSelectedItem());

                if (isReinstall || isUpdate) {
                    Optional<MinecraftVersion> minecraftVersion = Optional.ofNullable(version.minecraftVersion);

                    if (minecraftVersion.isPresent() && !minecraftVersion.get().version.equalsIgnoreCase(instance.id)
                            && !saveModsCheckbox.isSelected()) {
                        if (showDifferentMinecraftVersionsDialog() == -1) {
                            return;
                        }
                    }
                }

                final JDialog dialog = new JDialog(parent, isReinstall ? (
                // #. {0} is the name of the pack the user is installing
                isServer ? GetText.tr("Reinstalling {0} Server", pack.getName())
                        // #. {0} is the name of the pack the user is installing
                        : GetText.tr("Reinstalling {0}", pack.getName())) : (
                // #. {0} is the name of the pack the user is installing
                isServer ? GetText.tr("Installing {0} Server", pack.getName())
                        // #. {0} is the name of the pack the user is installing
                        : GetText.tr("Installing {0}", pack.getName())), ModalityType.DOCUMENT_MODAL);
                dialog.setLocationRelativeTo(App.launcher.getParent());
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
                        isReinstall, isServer, saveMods, shareCode, showModsChooser, loaderVersion, curseForgeManifest,
                        curseExtractedPath, modpacksChPackManifest, multiMCManifest, multiMCExtractedPath) {

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
                            } else {
                                // #. {0} is the pack name and {1} is the pack version
                                title = GetText.tr("{0} {1} Not Installed", pack.getName(), version.version);

                                // #. {0} is the pack name and {1} is the pack version
                                text = GetText.tr(
                                        "{0} {1} wasn't installed.<br/><br/>Check error logs for more information.",
                                        pack.getName(), version.version);

                                if (Files.exists(this.root) && Files.isDirectory(this.root)) {
                                    FileUtils.deleteDirectory(this.root);
                                }
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
                                    App.launcher.reloadServersPanel();
                                } else {
                                    App.launcher.reloadInstancesPanel();
                                }

                                if (pack.isLoggingEnabled() && App.settings.enableLogs && !version.isDev) {
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

                        if (this.curseForgeExtractedPath != null) {
                            FileUtils.deleteDirectory(this.curseForgeExtractedPath);
                        }

                        if (this.multiMCExtractedPath != null) {
                            FileUtils.deleteDirectory(this.multiMCExtractedPath);
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
                    instanceInstaller.setInstance(instance);
                }
                instanceInstaller.execute();
                dispose();
                dialog.setVisible(true);

            }
        });
        JButton cancel = new JButton(GetText.tr("Cancel"));
        cancel.addActionListener(e -> dispose());
        bottom.add(install);
        bottom.add(cancel);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setVisible(true);
    }

    private int showDifferentMinecraftVersionsDialog() {
        int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Save Mods?"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "Since this update changes the Minecraft version, your custom mods may no longer work.<br/><br/>The mods installed will be removed since you've not checked the \"Save Mods\" checkbox.<br/><br/>Do you want to save your mods?"))
                        .build())
                .setType(DialogManager.WARNING).show();

        if (ret == 0) {
            saveModsCheckbox.setSelected(true);
        }

        return ret;
    }

    private void handlePackInstall(Object object, final boolean isServer) {
        pack = (Pack) object;
        // #. {0} is the name of the pack the user is installing
        setTitle(GetText.tr("Installing {0}", pack.getName()));
        if (isServer) {
            // #. {0} is the name of the pack the user is installing
            setTitle(GetText.tr("Installing {0} Server", pack.getName()));
            this.isServer = true;
        }
    }

    private void handleCurseForgeInstall(Object object) {
        curseForgeProject = (CurseForgeProject) object;

        pack = new Pack();
        pack.name = curseForgeProject.name;

        pack.externalId = curseForgeProject.id;
        pack.description = curseForgeProject.summary;
        pack.websiteURL = curseForgeProject.websiteUrl;
        pack.curseForgeProject = curseForgeProject;

        List<CurseForgeFile> files = CurseForgeApi.getFilesForProject(curseForgeProject.id);

        pack.versions = files.stream().sorted(Comparator.comparingInt((CurseForgeFile file) -> file.id).reversed())
                .map(f -> {
                    PackVersion packVersion = new PackVersion();
                    packVersion.version = f.displayName;
                    packVersion.hasLoader = true;
                    packVersion._curseForgeFile = f;

                    try {
                        packVersion.minecraftVersion = MinecraftManager.getMinecraftVersion(f.gameVersion.get(0));
                    } catch (InvalidMinecraftVersion e) {
                        LogManager.error(e.getMessage());
                        return null;
                    }

                    return packVersion;
                }).filter(pv -> pv != null).collect(Collectors.toList());

        // #. {0} is the name of the pack the user is installing
        setTitle(GetText.tr("Installing {0}", curseForgeProject.name));
    }

    private void handleModpacksChInstall(Object object) {
        modpacksChPackManifest = (ModpacksChPackManifest) object;

        pack = new Pack();
        pack.externalId = modpacksChPackManifest.id;
        pack.name = modpacksChPackManifest.name;
        pack.description = modpacksChPackManifest.description;

        ModpacksChPackLink link = modpacksChPackManifest.links.stream()
                .filter(l -> l.type == ModpacksChPackLinkType.WEBSITE).findFirst().orElse(null);

        if (link != null) {
            pack.websiteURL = link.link;
        }

        pack.modpacksChPack = modpacksChPackManifest;

        pack.versions = modpacksChPackManifest.versions.stream()
                .sorted(Comparator.comparingInt((ModpacksChPackVersion version) -> version.updated).reversed())
                .map(v -> {
                    PackVersion packVersion = new PackVersion();
                    packVersion.version = v.name;
                    packVersion.hasLoader = true;
                    packVersion._modpacksChId = v.id;
                    return packVersion;
                }).filter(pv -> pv != null).collect(Collectors.toList());

        isReinstall = false;

        // #. {0} is the name of the pack the user is installing
        setTitle(GetText.tr("Installing {0}", modpacksChPackManifest.name));
    }

    private void handleCurseForgeImport(Object object) {
        curseForgeManifest = (CurseForgeManifest) object;

        pack = new Pack();
        pack.name = curseForgeManifest.name;

        if (curseForgeManifest.projectID != null) {
            CurseForgeProject curseForgeProject = CurseForgeApi.getProjectById(curseForgeManifest.projectID);

            curseForgeManifest.websiteUrl = curseForgeProject.websiteUrl;

            pack.externalId = curseForgeManifest.projectID;
            pack.description = curseForgeProject.summary;
            pack.curseForgeProject = curseForgeProject;
        }

        PackVersion packVersion = new PackVersion();
        packVersion.version = curseForgeManifest.version;

        try {
            packVersion.minecraftVersion = MinecraftManager.getMinecraftVersion(curseForgeManifest.minecraft.version);
        } catch (InvalidMinecraftVersion e) {
            LogManager.error(e.getMessage());
            return;
        }

        packVersion.hasLoader = true;

        pack.versions = Collections.singletonList(packVersion);

        isReinstall = false;

        // #. {0} is the name of the pack the user is installing
        setTitle(GetText.tr("Installing {0}", curseForgeManifest.name));
    }

    private void handleMultiMcImport(Object object) {
        multiMCManifest = (MultiMCManifest) object;

        pack = new Pack();
        pack.name = multiMCManifest.config.name;

        PackVersion packVersion = new PackVersion();
        packVersion.version = "1";

        try {
            packVersion.minecraftVersion = MinecraftManager.getMinecraftVersion(multiMCManifest.components.stream()
                    .filter(c -> c.uid.equalsIgnoreCase("net.minecraft")).findFirst().get().version);
        } catch (InvalidMinecraftVersion e) {
            LogManager.error(e.getMessage());
            return;
        }

        packVersion.hasLoader = multiMCManifest.components.stream()
                .anyMatch(c -> c.uid.equalsIgnoreCase("net.fabricmc.intermediary")
                        || c.uid.equalsIgnoreCase("net.fabricmc.intermediary"));

        pack.versions = Collections.singletonList(packVersion);

        isReinstall = false;

        // #. {0} is the name of the pack the user is installing
        setTitle(GetText.tr("Installing {0}", multiMCManifest.config.name));
    }

    private void handleInstanceInstall(Object object) {
        instance = (Instance) object;

        if (instance.isModpacksChPack()) {
            final ProgressDialog<ModpacksChPackManifest> dialog = new ProgressDialog<>(
                    GetText.tr("Downloading Pack Manifest"), 0, GetText.tr("Downloading Pack Manifest"),
                    "Cancelled downloading modpacks.ch pack manifest");
            dialog.addThread(new Thread(() -> {
                ModpacksChPackManifest packManifest = com.atlauncher.network.Download.build()
                        .setUrl(String.format("%s/modpack/%d", Constants.MODPACKS_CH_API_URL,
                                instance.launcher.modpacksChPackManifest.id))
                        .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build())
                        .asClass(ModpacksChPackManifest.class);
                dialog.setReturnValue(packManifest);
                dialog.close();
            }));
            dialog.start();

            if (dialog.wasClosed) {
                setVisible(false);
                dispose();
                return;
            }

            handleModpacksChInstall(dialog.getReturnValue());
        } else if (instance.isCurseForgePack()) {
            handleCurseForgeInstall(instance.launcher.curseForgeProject);
        } else {
            pack = instance.getPack();
        }

        isReinstall = true; // We're reinstalling

        // #. {0} is the name of the pack the user is installing
        if (isUpdate) {
            setTitle(GetText.tr("Updating {0}", instance.launcher.name));
        } else {
            setTitle(GetText.tr("Reinstalling {0}", instance.launcher.name));
        }
    }

    private GridBagConstraints setupVersionsDropdown(GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel versionLabel = new JLabel(GetText.tr("Version To Install") + ": ");
        middle.add(versionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
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
                if (version.versionMatches(instance)) {
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
                    PackVersion packVersion = ((PackVersion) e.getItem());
                    Optional<MinecraftVersion> minecraftVersion = Optional.ofNullable(packVersion.minecraftVersion);

                    saveModsLabel.setVisible(minecraftVersion.isPresent()
                            && !minecraftVersion.get().version.equalsIgnoreCase(this.instance.id));
                    saveModsCheckbox.setVisible(minecraftVersion.isPresent()
                            && !minecraftVersion.get().version.equalsIgnoreCase(this.instance.id));
                }
            }
        });

        if (autoInstallVersion != null) {
            versionsDropDown.setSelectedItem(autoInstallVersion);
            versionsDropDown.setEnabled(false);
        }

        if (multiMCManifest != null) {
            gbc.gridx--;
            versionLabel.setVisible(multiMCManifest == null);
            versionsDropDown.setVisible(multiMCManifest == null);
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

            loaderVersions.forEach(version -> loaderVersionsDropDown.addItem(version));

            if (isReinstall && instance.launcher.loaderVersion != null) {
                String loaderVersionString = instance.launcher.loaderVersion.version;

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

            loaderVersionsDropDown.setPreferredSize(new Dimension(loaderVersionLength, 23));

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
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        loaderVersionLabel = new JLabel(GetText.tr("Loader Version") + ": ");
        middle.add(loaderVersionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        loaderVersionsDropDown = new JComboBox<>();
        this.updateLoaderVersions((PackVersion) this.versionsDropDown.getSelectedItem());
        middle.add(loaderVersionsDropDown, gbc);

        return gbc;
    }
}
