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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
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
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.data.json.Version;
import com.atlauncher.data.loaders.LoaderVersion;
import com.atlauncher.data.mojang.LoggingClient;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

public class InstanceInstallerDialog extends JDialog {
    private static final long serialVersionUID = -6984886874482721558L;
    private int versionLength = 0;
    private int loaderVersionLength = 0;
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
    private List<PackVersion> versions = new ArrayList<>();
    private JLabel loaderVersionLabel;
    private JComboBox<LoaderVersion> loaderVersionsDropDown;
    private List<LoaderVersion> loaderVersions = new ArrayList<>();
    private JLabel enableUserLockLabel;
    private JCheckBox enableUserLock;
    private boolean isUpdate;
    private PackVersion autoInstallVersion;

    public InstanceInstallerDialog(Object object) {
        this(object, false, false, null, null, true);
    }

    public InstanceInstallerDialog(Pack pack, PackVersion version, String shareCode, boolean showModsChooser) {
        this(pack, false, false, version, shareCode, showModsChooser);
    }

    public InstanceInstallerDialog(Pack pack, boolean isServer) {
        this((Object) pack, false, true, null, null, true);
    }

    public InstanceInstallerDialog(Object object, final boolean isUpdate, final boolean isServer,
            final PackVersion autoInstallVersion, final String shareCode, final boolean showModsChooser) {
        super(App.settings.getParent(), ModalityType.APPLICATION_MODAL);

        this.isUpdate = isUpdate;
        this.autoInstallVersion = autoInstallVersion;

        if (object instanceof Pack) {
            pack = (Pack) object;
            setTitle(Language.INSTANCE.localize("common.installing") + " " + pack.getName());
            if (isServer) {
                setTitle(Language.INSTANCE.localize("common.installing") + " " + pack.getName() + " "
                        + Language.INSTANCE.localize("common.server"));
                this.isServer = true;
            }
        } else {
            instance = (Instance) object;
            pack = instance.getRealPack();
            isReinstall = true; // We're reinstalling
            setTitle(Language.INSTANCE.localize("common.reinstalling") + " " + instance.getName());
        }
        setSize(400, 225);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        install = new JButton(((isReinstall)
                ? (isUpdate ? Language.INSTANCE.localize("common.update")
                        : Language.INSTANCE.localize("common.reinstall"))
                : Language.INSTANCE.localize("common.install")));

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel(((isReinstall) ? Language.INSTANCE.localize("common.reinstalling")
                : Language.INSTANCE.localize("common.installing")) + " " + pack.getName()));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        if (!this.isServer) {
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            instanceNameLabel = new JLabel(Language.INSTANCE.localize("instance.name") + ": ");
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

        gbc = this.setupVersionsDropdown(gbc);
        gbc = this.setupLoaderVersionsDropdown(gbc);

        if (!this.isServer) {
            if (!isReinstall) {
                gbc.gridx = 0;
                gbc.gridy++;
                gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
                enableUserLockLabel = new JLabel(Language.INSTANCE.localize("instance.enableuserlock") + "? ");
                middle.add(enableUserLockLabel, gbc);

                gbc.gridx++;
                gbc.anchor = GridBagConstraints.BASELINE_LEADING;
                enableUserLock = new JCheckBox();
                enableUserLock.addActionListener(e -> {
                    if (enableUserLock.isSelected()) {
                        int ret = DialogManager.optionDialog()
                                .setTitle(Language.INSTANCE.localize("instance.userlocktitle"))
                                .setContent(HTMLUtils.centerParagraph(
                                        Language.INSTANCE.localizeWithReplace("instance.userlockhelp", "<br/>")))
                                .setType(DialogManager.WARNING)
                                .addOption(Language.INSTANCE.localize("common.yes"), true)
                                .addOption(Language.INSTANCE.localize("common.no")).show();

                        if (ret != 0) {
                            enableUserLock.setSelected(false);
                        }
                    }
                });
                middle.add(enableUserLock, gbc);
            }
        }

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        install.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isReinstall && !isServer && App.settings.isInstance(instanceNameField.getText())) {
                    instance = App.settings.getInstanceByName(instanceNameField.getText());
                    if (instance.getPackName().equalsIgnoreCase(pack.getName())) {
                        int ret = DialogManager.yesNoDialog().setTitle(Language.INSTANCE.localize("common.error"))
                                .setContent(HTMLUtils
                                        .centerParagraph(Language.INSTANCE.localize("common.error") + "<br/><br/>"
                                                + Language.INSTANCE.localizeWithReplace("instance.alreadyinstance1",
                                                        instanceNameField.getText() + "<br/><br/>")))
                                .setType(DialogManager.ERROR).show();
                        if (ret != DialogManager.YES_OPTION) {
                            return;
                        }
                        isReinstall = true;
                        if (instance == null) {
                            return;
                        }
                    } else {
                        DialogManager.okDialog().setTitle(Language.INSTANCE.localize("common.error"))
                                .setContent(HTMLUtils
                                        .centerParagraph(Language.INSTANCE.localize("common.error") + "<br/><br/>"
                                                + Language.INSTANCE.localizeWithReplace("instance.alreadyinstance",
                                                        instanceNameField.getText() + "<br/><br/>")))
                                .setType(DialogManager.ERROR).show();
                        return;
                    }
                } else if (!isReinstall && !isServer
                        && instanceNameField.getText().replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                    DialogManager.okDialog().setTitle(Language.INSTANCE.localize("common.error"))
                            .setContent(
                                    HTMLUtils
                                            .centerParagraph(Language.INSTANCE.localize("common.error") + "<br/><br/>"
                                                    + Language.INSTANCE.localizeWithReplace("instance.invalidname",
                                                            instanceNameField.getText())))
                            .setType(DialogManager.ERROR).show();
                    return;
                }
                final PackVersion version = (PackVersion) versionsDropDown.getSelectedItem();
                final JDialog dialog = new JDialog(App.settings.getParent(),
                        ((isReinstall) ? Language.INSTANCE.localize("common.reinstalling")
                                : Language.INSTANCE.localize("common.installing")) + " " + pack.getName() + " "
                                + version.getVersion()
                                + ((isServer) ? " " + Language.INSTANCE.localize("common.server") : ""),
                        ModalityType.DOCUMENT_MODAL);
                dialog.setLocationRelativeTo(App.settings.getParent());
                dialog.setSize(300, 100);
                dialog.setResizable(false);

                JPanel topPanel = new JPanel();
                topPanel.setLayout(new BorderLayout());
                final JLabel doing = new JLabel(Language.INSTANCE.localizeWithReplace("instance.startingprocess",
                        ((isReinstall) ? Language.INSTANCE.localize("common.reinstall")
                                : Language.INSTANCE.localize("common.install"))));
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

                final InstanceInstaller instanceInstaller = new InstanceInstaller(
                        (isServer ? "" : instanceNameField.getText()), pack, version, isReinstall, isServer, shareCode,
                        showModsChooser, (LoaderVersion) loaderVersionsDropDown.getSelectedItem()) {

                    protected void done() {
                        Boolean success = false;
                        int type;
                        String text;
                        String title;
                        if (isCancelled()) {
                            type = DialogManager.ERROR;
                            text = pack.getName() + " " + version.getVersion() + " "
                                    + Language.INSTANCE.localize("common.wasnt") + " "
                                    + ((isReinstall) ? Language.INSTANCE.localize("common.reinstalled")
                                            : Language.INSTANCE.localize("common.installed"))
                                    + "<br/><br/>" + Language.INSTANCE.localize("instance" + ".checkerrorlogs");
                            title = pack.getName() + " " + version.getVersion() + " "
                                    + Language.INSTANCE.localize("common.not") + " "
                                    + ((isReinstall) ? Language.INSTANCE.localize("common.reinstalled")
                                            : Language.INSTANCE.localize("common.installed"));
                            if (isReinstall) {
                                if (shouldCoruptInstance()) {
                                    App.settings.setInstanceUnplayable(instance);
                                }
                            }
                        } else {
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
                                text = pack.getName() + " " + version.getVersion() + " "
                                        + Language.INSTANCE.localize("common.hasbeen") + " "
                                        + ((isReinstall) ? Language.INSTANCE.localize("common.reinstalled")
                                                : Language.INSTANCE.localize("common.installed"))
                                        + "<br/><br/>"
                                        + ((isServer)
                                                ? Language.INSTANCE.localizeWithReplace("instance" + ".finditserver",
                                                        "<br/><br/>" + this.getRootDirectory().getAbsolutePath())
                                                : Language.INSTANCE.localize("instance.findit"));
                                title = pack.getName() + " " + version.getVersion() + " "
                                        + Language.INSTANCE.localize("common.installed");
                                if (isReinstall) {
                                    LoggingClient loggingClient = version.getMinecraftVersion().getMojangVersion()
                                            .hasLogging()
                                                    ? version.getMinecraftVersion().getMojangVersion().getLogging()
                                                            .getClient()
                                                    : null;

                                    instance.setVersion(version.getVersion());
                                    instance.setMinecraftVersion(version.getMinecraftVersion().getVersion());
                                    instance.setVersionType(version.getMinecraftVersion().getMojangVersion().getType());
                                    instance.setModsInstalled(this.getModsInstalled());
                                    instance.setJarOrder(this.getJarOrder());
                                    instance.setMemory(this.getMemory());
                                    instance.setPermgen(this.getPermGen());
                                    instance.setIsNewLaunchMethod(!this.isLegacy());
                                    instance.setUsesNewLibraries(true);
                                    instance.setLibraries(this.getLibrariesForLaunch());
                                    if (this.hasArguments()) {
                                        instance.setArguments(this.getArguments());
                                    } else {
                                        instance.setMinecraftArguments(this.getMinecraftArguments());
                                    }
                                    instance.setExtraArguments(this.getExtraArguments());
                                    instance.setMainClass(this.getMainClass());
                                    instance.setAssets(version.getMinecraftVersion().getMojangVersion().getAssets());
                                    instance.setLogging(loggingClient);
                                    instance.setJava(this.getJsonVersion().getJava());
                                    instance.setEnableCurseIntegration(
                                            this.getJsonVersion().hasEnabledCurseIntegration());
                                    instance.setEnableEditingMods(this.getJsonVersion().hasEnabledEditingMods());
                                    instance.setLoaderVersion((LoaderVersion) loaderVersionsDropDown.getSelectedItem());
                                    if (version.isDev()) {
                                        instance.setDevVersion();
                                        if (version.getHash() != null) {
                                            instance.setHash(version.getHash());
                                        }
                                    } else {
                                        instance.setNotDevVersion();
                                    }
                                    if (!instance.isPlayable()) {
                                        instance.setPlayable();
                                    }
                                } else if (isServer) {

                                } else {
                                    LoggingClient loggingClient = version.getMinecraftVersion().getMojangVersion()
                                            .hasLogging()
                                                    ? version.getMinecraftVersion().getMojangVersion().getLogging()
                                                            .getClient()
                                                    : null;

                                    Instance newInstance = new Instance(instanceNameField.getText(), pack.getName(),
                                            pack, enableUserLock.isSelected(), version.getVersion(),
                                            version.getMinecraftVersion().getVersion(),
                                            version.getMinecraftVersion().getMojangVersion().getType(),
                                            this.getMemory(), this.getPermGen(), this.getModsInstalled(),
                                            this.getJarOrder(), this.getLibrariesForLaunch(), this.getExtraArguments(),
                                            this.getMinecraftArguments(), this.getMainClass(),
                                            version.getMinecraftVersion().getMojangVersion().getAssets(), loggingClient,
                                            version.isDev(), !version.getMinecraftVersion().isLegacy(),
                                            this.getJsonVersion().getJava(),
                                            this.getJsonVersion().hasEnabledCurseIntegration(),
                                            this.getJsonVersion().hasEnabledEditingMods(),
                                            (LoaderVersion) loaderVersionsDropDown.getSelectedItem());

                                    if (this.hasArguments()) {
                                        newInstance.setArguments(this.getArguments());
                                    }

                                    if (version.isDev() && (version.getHash() != null)) {
                                        newInstance.setHash(version.getHash());
                                    }

                                    App.settings.getInstances().add(newInstance);

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
                                    type = DialogManager.ERROR;
                                    text = pack.getName() + " " + version.getVersion() + " "
                                            + Language.INSTANCE.localize("common.wasnt") + " "
                                            + Language.INSTANCE.localize("common.reinstalled") + "<br/><br/>"
                                            + (this.shouldCoruptInstance()
                                                    ? Language.INSTANCE.localize("instance.nolongerplayable")
                                                    : "")
                                            + "<br/><br/>" + Language.INSTANCE.localize("instance.checkerrorlogs")
                                            + "!";
                                    title = pack.getName() + " " + version.getVersion() + " "
                                            + Language.INSTANCE.localize("common.not") + " "
                                            + Language.INSTANCE.localize("common.reinstalled");
                                    if (this.shouldCoruptInstance()) {
                                        App.settings.setInstanceUnplayable(instance);
                                    }
                                } else {
                                    // Install failed so delete the folder and clear Temp Dir
                                    Utils.delete(this.getRootDirectory());
                                    type = DialogManager.ERROR;
                                    text = pack.getName() + " " + version.getVersion() + " "
                                            + Language.INSTANCE.localize("common.wasnt") + " "
                                            + Language.INSTANCE.localize("common.installed") + "<br/><br/>"
                                            + Language.INSTANCE.localize("instance.checkerrorlogs") + "!";
                                    title = pack.getName() + " " + version.getVersion() + " "
                                            + Language.INSTANCE.localize("common.not") + " "
                                            + Language.INSTANCE.localize("common.installed");
                                }
                            }
                        }

                        dialog.dispose();

                        Utils.cleanTempDirectory();

                        DialogManager.okDialog().setTitle(title).setContent(HTMLUtils.centerParagraph(text))
                                .setType(type).show();
                    }

                };
                instanceInstaller.addPropertyChangeListener(evt -> {
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
        cancel = new JButton(Language.INSTANCE.localize("common.cancel"));
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
        versionLabel = new JLabel(Language.INSTANCE.localize("instance.versiontoinstall") + ": ");
        middle.add(versionLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        versionsDropDown = new JComboBox<>();
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

        // ensures that font width is taken into account
        for (PackVersion version : versions) {
            versionLength = Math.max(versionLength,
                    getFontMetrics(Utils.getFont()).stringWidth(version.toString()) + 25);
        }

        // ensures that the dropdown is at least 200 px wide
        versionLength = Math.max(200, versionLength);

        // ensures that there is a maximum width of 250 px to prevent overflow
        versionLength = Math.min(250, versionLength);

        versionsDropDown.setPreferredSize(new Dimension(versionLength, 25));
        middle.add(versionsDropDown, gbc);

        versionsDropDown.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateLoaderVersions((PackVersion) e.getItem());
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
        loaderVersionsDropDown.setEnabled(false);
        loaderVersions.clear();

        loaderVersionsDropDown.removeAllItems();
        loaderVersionsDropDown.addItem(new LoaderVersion(Language.INSTANCE.localize("instance.gettingloaderversions")));

        loaderVersionLabel.setVisible(true);
        loaderVersionsDropDown.setVisible(true);

        if (!item.hasLoader() || !item.hasChoosableLoader()) {
            loaderVersionLabel.setVisible(false);
            loaderVersionsDropDown.setVisible(false);
            return;
        }

        install.setEnabled(false);
        versionsDropDown.setEnabled(false);

        Runnable r = new Runnable() {
            public void run() {
                Version jsonVersion = Gsons.DEFAULT.fromJson(pack.getJSON(item.getVersion()), Version.class);

                loaderVersions.clear();
                loaderVersions.addAll(jsonVersion.getLoader().getChoosableVersions(jsonVersion.getMinecraft()));

                // ensures that font width is taken into account
                for (LoaderVersion version : loaderVersions) {
                    loaderVersionLength = Math.max(loaderVersionLength,
                            getFontMetrics(Utils.getFont()).stringWidth(version.toString()) + 25);
                }

                loaderVersionsDropDown.removeAllItems();

                loaderVersions.stream().forEach(version -> {
                    loaderVersionsDropDown.addItem(version);
                });

                if (isReinstall && instance.installedWithLoaderVersion()) {
                    loaderVersionsDropDown.setSelectedItem(instance.getLoaderVersion());
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
            }
        };

        new Thread(r).start();
    }

    private GridBagConstraints setupLoaderVersionsDropdown(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        loaderVersionLabel = new JLabel(Language.INSTANCE.localize("instance.loaderversion") + ": ");
        middle.add(loaderVersionLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        loaderVersionsDropDown = new JComboBox<>();
        this.updateLoaderVersions((PackVersion) this.versionsDropDown.getSelectedItem());
        middle.add(loaderVersionsDropDown, gbc);

        return gbc;
    }
}
