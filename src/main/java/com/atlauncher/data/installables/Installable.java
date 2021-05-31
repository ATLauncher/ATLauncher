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
package com.atlauncher.data.installables;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.workers.InstanceInstaller;

import org.mini2Dx.gettext.GetText;

public abstract class Installable {
    public String instanceName;
    public boolean isServer = false;
    public boolean isUpdate = false;
    public boolean isReinstall = false;
    public boolean saveMods = false;
    public Instance instance;

    public Window parent = App.launcher.getParent();

    public boolean showModsChooser = true;
    public CurseForgeManifest curseForgeManifest;
    public Path curseExtractedPath;
    public ModpacksChPackManifest modpacksChPackManifest;
    public MultiMCManifest multiMCManifest;
    public Path multiMCExtractedPath;

    public abstract Pack getPack();

    public abstract PackVersion getPackVersion();

    public abstract LoaderVersion getLoaderVersion();

    public boolean startInstall() {
        if (!isReinstall && !isServer && InstanceManager.isInstance(instanceName)) {
            DialogManager.okDialog().setTitle(GetText.tr("Error")).setContent(new HTMLBuilder().center()
                    .text(GetText.tr("An instance already exists with that name.<br/><br/>Rename it and try again."))
                    .build()).setType(DialogManager.ERROR).show();
            return false;
        } else if (!isReinstall && !isServer && instanceName.replaceAll("[^A-Za-z0-9]", "").length() == 0) {
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("Instance name is invalid. It must contain at least 1 letter or number."))
                            .build())
                    .setType(DialogManager.ERROR).show();
            return false;
        } else if (!isReinstall && isServer && ServerManager.isServer(instanceName)) {
            DialogManager.okDialog().setTitle(GetText.tr("Error")).setContent(new HTMLBuilder().center()
                    .text(GetText.tr("A server already exists with that name.<br/><br/>Rename it and try again."))
                    .build()).setType(DialogManager.ERROR).show();
            return false;
        } else if (!isReinstall && isServer && instanceName.replaceAll("[^A-Za-z0-9]", "").length() == 0) {
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("Server name is invalid. It must contain at least 1 letter or number."))
                            .build())
                    .setType(DialogManager.ERROR).show();
            return false;
        }

        final Pack pack = getPack();
        final PackVersion version = getPackVersion();

        if (isReinstall || isUpdate) {
            Optional<VersionManifestVersion> minecraftVersion = Optional.ofNullable(version.minecraftVersion);

            if (minecraftVersion.isPresent() && !minecraftVersion.get().id.equalsIgnoreCase(instance.id) && !saveMods) {
                int ret = showDifferentMinecraftVersionsDialog();
                if (ret == -1 || ret == 2) {
                    return false;
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
        final JLabel doing = new JLabel(
                (isReinstall) ? GetText.tr("Starting Reinstall Process") : GetText.tr("Starting Install Process"));
        doing.setHorizontalAlignment(JLabel.CENTER);
        doing.setVerticalAlignment(JLabel.TOP);
        topPanel.add(doing);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        JProgressBar progressBar = new JProgressBar(0, 10000);
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        progressBar.setIndeterminate(true);

        JProgressBar subProgressBar = new JProgressBar(0, 10000);
        bottomPanel.add(subProgressBar, BorderLayout.SOUTH);
        subProgressBar.setValue(0);
        subProgressBar.setVisible(false);

        dialog.add(topPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        LoaderVersion loaderVersion = getLoaderVersion();

        boolean saveMods = !isServer && isReinstall && this.saveMods;

        final InstanceInstaller instanceInstaller = new InstanceInstaller(instanceName, pack, version, isReinstall,
                isServer, saveMods, null, showModsChooser, loaderVersion, curseForgeManifest, curseExtractedPath,
                modpacksChPackManifest, multiMCManifest, multiMCExtractedPath) {

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
                        text = GetText.tr("{0} {1} wasn't reinstalled.<br/><br/>Check error logs for more information.",
                                pack.getName(), version.version);
                    } else {
                        // #. {0} is the pack name and {1} is the pack version
                        title = GetText.tr("{0} {1} Not Installed", pack.getName(), version.version);

                        // #. {0} is the pack name and {1} is the pack version
                        text = GetText.tr("{0} {1} wasn't installed.<br/><br/>Check error logs for more information.",
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
                            text = GetText.tr("{0} {1} server has been installed.<br/><br/>Find it in the servers tab.",
                                    pack.getName(), version.version);
                        } else {
                            // #. {0} is the pack name and {1} is the pack version
                            text = GetText.tr("{0} {1} has been installed.<br/><br/>Find it in the instances tab.",
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

                DialogManager.okDialog().setTitle(title).setContent(new HTMLBuilder().center().text(text).build())
                        .setType(type).show();
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
        dialog.setVisible(true);

        return true;
    }

    public int showDifferentMinecraftVersionsDialog() {
        int ret = DialogManager.yesNoCancelDialog().setTitle(GetText.tr("Save Mods?"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "Since this update changes the Minecraft version, your custom mods may no longer work.<br/><br/>The mods installed will be removed since you've not checked the \"Save Mods\" checkbox.<br/><br/>Do you want to save your mods?"))
                        .build())
                .setType(DialogManager.WARNING).show();

        if (ret == 0) {
            saveMods = true;
        }

        return ret;
    }
}
