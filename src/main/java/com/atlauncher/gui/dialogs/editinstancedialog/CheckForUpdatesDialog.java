/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
package com.atlauncher.gui.dialogs.editinstancedialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Network;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.gui.card.ModUpdatesChooserCard;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.gui.layouts.WrapLayout;
import com.atlauncher.gui.panels.CenteredTextPanel;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;

import okhttp3.OkHttpClient;

public class CheckForUpdatesDialog extends JDialog {
    private final Instance instance;
    private final List<DisableableMod> mods;
    private final List<ModUpdatesChooserCard> modUpdateCards = new ArrayList<>();
    public final Map<DisableableMod, DisableableMod> updatedMods = Collections.synchronizedMap(new HashMap<>());

    private boolean checking = false;
    private final boolean reinstalling;
    private final ModUpdatesComplete modUpdatesCompleteRunnable;

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JComboBox<ComboItem<ModPlatform>> platformComboBox = new JComboBox<>();
    private ExecutorService executorService;
    private final JButton updateButton = new JButton();
    private JButton closeButton = new JButton(GetText.tr("Close"));
    private int modsToUpdate = 0;

    public CheckForUpdatesDialog(Window parent, Instance instance, List<DisableableMod> mods, boolean reinstalling,
            ModUpdatesComplete modUpdatesCompleteRunnable) {
        super(parent);

        this.instance = instance;
        this.mods = mods;
        this.reinstalling = reinstalling;
        this.modUpdatesCompleteRunnable = modUpdatesCompleteRunnable;

        Analytics.sendScreenView("Check For Updates Dialog");

        setModal(true);
        setLayout(new BorderLayout());
        setResizable(true);
        setTitle(GetText.tr("Checking For Updates"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        setMinimumSize(new Dimension(330, 350));
        setSize(new Dimension(950, 650));

        setupComponents();

        checkForUpdates();

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }

        dispose();
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")),
                new EmptyBorder(5, 5, 5, 5)));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        JLabelWithHover platformLabel = new JLabelWithHover(GetText.tr("Platform") + ":",
                Utils.getIconImage(App.THEME.getIconPath("question")),
                new HTMLBuilder().text(GetText.tr(
                        "The mod platform to use when querying versions for a mod.<br/>The preferred platform when searching all platforms can be set in the launchers Mods settings tab."))
                        .center().build());

        platformComboBox.addItem(
                new ComboItem<ModPlatform>(null, GetText.tr("All ({0} Preferred)", App.settings.defaultModPlatform)));
        platformComboBox.addItem(new ComboItem<ModPlatform>(ModPlatform.CURSEFORGE, "CurseForge"));
        platformComboBox.addItem(new ComboItem<ModPlatform>(ModPlatform.MODRINTH, "Modrinth"));
        platformComboBox.setMaximumSize(new Dimension(platformComboBox.getPreferredSize().width, 23));

        platformComboBox.addActionListener(e -> {
            checkForUpdates();
        });

        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(platformLabel);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(platformComboBox);

        add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")),
                new EmptyBorder(5, 5, 5, 5)));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        bottomPanel.add(Box.createHorizontalGlue());

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        bottomPanel.add(closeButton);

        bottomPanel.add(Box.createHorizontalStrut(20));

        updateButton.setText(reinstalling ? GetText.tr("Reinstall") : GetText.tr("Update"));
        updateButton.setEnabled(false);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    List<Pair<DisableableMod, Pair<Object, Object>>> modsUpdating = modUpdateCards.parallelStream()
                            .filter(modUpdateCard -> {
                                if (modUpdateCard.isCurseForgeMod()) {
                                    CurseForgeFile currentVersion = (CurseForgeFile) modUpdateCard.getCurrentVersion();
                                    CurseForgeFile updateVersion = (CurseForgeFile) modUpdateCard
                                            .getVersionUpdatingTo();

                                    return modUpdateCard.isUpdating() && currentVersion.id != updateVersion.id;
                                } else if (modUpdateCard.isModrinthMod()) {
                                    ModrinthVersion currentVersion = (ModrinthVersion) modUpdateCard
                                            .getCurrentVersion();
                                    ModrinthVersion updateVersion = (ModrinthVersion) modUpdateCard
                                            .getVersionUpdatingTo();

                                    return modUpdateCard.isUpdating() && !currentVersion.id.equals(updateVersion.id);
                                }

                                return false;
                            }).map(modUpdateCard -> {
                                return new Pair<>(modUpdateCard.mod,
                                        new Pair<>(modUpdateCard.getModProject(),
                                                modUpdateCard.getVersionUpdatingTo()));
                            }).collect(Collectors.toList());

                    // update all mods
                    long totalBytes = modsUpdating.stream().mapToLong(mod -> {
                        if (mod.right().left() instanceof CurseForgeProject) {
                            CurseForgeFile updateVersion = (CurseForgeFile) mod.right().right();

                            // these are downloaded externally so not included in the total bytes
                            if (updateVersion.downloadUrl == null) {
                                return 0;
                            }

                            return (long) updateVersion.fileLength;
                        } else if (mod.right().left() instanceof ModrinthProject) {
                            ModrinthVersion updateVersion = (ModrinthVersion) mod.right().right();

                            return updateVersion.getPrimaryFile().size;
                        }

                        return 0;
                    }).sum();

                    // load in panel to show update happening
                    String text = GetText.tr("Downloading {0} Mods", modsUpdating.size());
                    LoadingPanel loadingPanel = new LoadingPanel(text);
                    loadingPanel.setTotalBytes(totalBytes);
                    OkHttpClient progressClient = Network.createProgressClient(loadingPanel);
                    SwingUtilities.invokeLater(() -> {
                        platformComboBox.setEnabled(false);
                        updateButton.setEnabled(false);
                        closeButton.setText(GetText.tr("Cancel"));
                        addLoadingPanel(loadingPanel, text);
                    });

                    executorService = Executors.newFixedThreadPool(10);
                    for (Pair<DisableableMod, Pair<Object, Object>> mod : modsUpdating) {
                        executorService.execute(() -> {
                            DisableableMod newMod = null;

                            if (mod.right().left() instanceof CurseForgeProject) {
                                CurseForgeProject project = (CurseForgeProject) mod.right().left();
                                CurseForgeFile updateVersion = (CurseForgeFile) mod.right().right();

                                newMod = instance.reinstallModFromCurseForge(mod.left(), project, updateVersion,
                                        progressClient);
                            } else if (mod.right().left() instanceof ModrinthProject) {
                                ModrinthProject project = (ModrinthProject) mod.right().left();
                                ModrinthVersion updateVersion = (ModrinthVersion) mod.right().right();

                                newMod = instance.reinstallModFromModrinth(mod.left(),
                                        project, updateVersion, progressClient);
                            }

                            updatedMods.put(mod.left(), newMod);
                        });
                    }
                    executorService.shutdown();

                    try {
                        if (!executorService.awaitTermination(5, TimeUnit.MINUTES)) {
                            executorService.shutdownNow();
                        }
                    } catch (InterruptedException ex) {
                        executorService.shutdownNow();
                        Thread.currentThread().interrupt();
                    }

                    // process complete, only show close button and disable everything else
                    SwingUtilities.invokeLater(() -> {
                        String text1 = reinstalling ? GetText.tr("{0} Mods Have Been Installed", updatedMods.size())
                                : GetText.tr("{0} Mods Have Been Updated", updatedMods.size());
                        mainPanel.removeAll();
                        mainPanel.revalidate();
                        mainPanel.repaint();
                        mainPanel.add(new CenteredTextPanel(text1), BorderLayout.CENTER);
                        setTitle(text1);

                        topPanel.setVisible(false);
                        updateButton.setVisible(false);
                        closeButton.setText(GetText.tr("Close"));

                        modUpdatesCompleteRunnable.modsInstalled(updatedMods);
                    });
                }).start();
            }
        });
        bottomPanel.add(updateButton);
        bottomPanel.add(Box.createHorizontalGlue());

        add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void addLoadingPanel(String text) {
        addLoadingPanel(new LoadingPanel(text), text);
    }

    private void addLoadingPanel(LoadingPanel loadingPanel, String text) {
        mainPanel.removeAll();
        mainPanel.revalidate();
        mainPanel.repaint();
        mainPanel.add(loadingPanel, BorderLayout.CENTER);

        setTitle(text);

        revalidate();
        repaint();
    }

    private void checkForUpdates() {
        if (!checking) {
            new Thread(() -> {
                checking = true;
                modUpdateCards.clear();

                SwingUtilities.invokeLater(() -> {
                    platformComboBox.setEnabled(false);
                    updateButton.setEnabled(false);
                    closeButton.setText(GetText.tr("Cancel"));
                    addLoadingPanel(
                            reinstalling ? GetText.tr("Fetching Versions") : GetText.tr("Checking For Updates"));
                });

                ModPlatform platform = ((ComboItem<ModPlatform>) platformComboBox.getSelectedItem()).getValue();

                Map<DisableableMod, Pair<Object, Object>> modUpdates = Collections.synchronizedMap(new HashMap<>());

                List<DisableableMod> modsCheckedForUpdates = new ArrayList<>();

                executorService = Executors.newFixedThreadPool(10);

                if (platform == ModPlatform.MODRINTH
                        || (platform == null && App.settings.defaultModPlatform == ModPlatform.MODRINTH)) {
                    checkForUpdatesOnModrinth(mods, modUpdates, modsCheckedForUpdates);
                }

                if (platform == ModPlatform.CURSEFORGE
                        || (platform == null && App.settings.defaultModPlatform == ModPlatform.CURSEFORGE)) {
                    checkForUpdatesOnCurseForge(mods, modUpdates, modsCheckedForUpdates);
                }

                // download the mods from the specific platform
                executorService.shutdown();

                try {
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException ex) {
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                }

                // if the platform is set to null then we need to fill in the blanks with the users non default mod
                // platform
                if (platform == null) {
                    executorService = Executors.newFixedThreadPool(10);

                    List<DisableableMod> modsToCheck = mods.stream().filter(m -> !modsCheckedForUpdates.contains(m))
                            .collect(Collectors.toList());

                    if (App.settings.defaultModPlatform == ModPlatform.CURSEFORGE) {
                        checkForUpdatesOnModrinth(modsToCheck, modUpdates, modsCheckedForUpdates);
                    }

                    if (App.settings.defaultModPlatform == ModPlatform.MODRINTH) {
                        checkForUpdatesOnCurseForge(modsToCheck, modUpdates, modsCheckedForUpdates);
                    }

                    executorService.shutdown();

                    try {
                        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                            executorService.shutdownNow();
                        }
                    } catch (InterruptedException ex) {
                        executorService.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                }

                // load in mods panel
                SwingUtilities.invokeLater(() -> {
                    if (modUpdates.size() == 0) {
                        String text = reinstalling ? GetText.tr("No Versions Found") : GetText.tr("No Updates Found");
                        mainPanel.removeAll();
                        mainPanel.revalidate();
                        mainPanel.repaint();
                        mainPanel.add(new CenteredTextPanel(text), BorderLayout.CENTER);
                        setTitle(text);
                    } else {
                        JPanel modsPanel = new JPanel(new WrapLayout());
                        for (Map.Entry<DisableableMod, Pair<Object, Object>> entry : modUpdates.entrySet()) {
                            modUpdateCards
                                    .add(new ModUpdatesChooserCard(this, instance, entry.getKey(), entry.getValue(),
                                            reinstalling, (boolean checked) -> {
                                                if (checked) {
                                                    modsToUpdate += 1;
                                                } else {
                                                    modsToUpdate -= 1;
                                                }

                                                SwingUtilities.invokeLater(() -> {
                                                    updateButton.setEnabled(modsToUpdate != 0);
                                                    updateButton.setToolTipText(modsToUpdate != 0 ? null
                                                            : GetText.tr("No Mods Selected"));
                                                });
                                            }));
                        }

                        for (ModUpdatesChooserCard modUpdateCard : modUpdateCards) {
                            modsPanel.add(modUpdateCard);
                        }

                        JScrollPane modsScrollPane = new JScrollPane(modsPanel,
                                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
                            {
                                this.getVerticalScrollBar().setUnitIncrement(8);
                            }
                        };

                        modsToUpdate = modUpdates.size();

                        mainPanel.removeAll();
                        mainPanel.revalidate();
                        mainPanel.repaint();
                        mainPanel.add(modsScrollPane, BorderLayout.CENTER);
                        setTitle(GetText.tr("Select Versions For {0} Mods", modUpdates.size()));
                    }

                    platformComboBox.setEnabled(true);
                    updateButton.setEnabled(modUpdates.size() != 0);
                    updateButton.setToolTipText(modUpdates.size() != 0 ? null
                            : GetText.tr("No Mods Selected"));
                    closeButton.setText(GetText.tr("Close"));
                });
                checking = false;
            }).start();
        }
    }

    // Request heirachy:
    // 1. Get latest versions of all mods - /v2/version_files/update
    // 2. Get all projects that have updates from api - /v2/projects?ids=[]
    // 3. In parallel get all files for those projects matching loader/mc version - /v2/project/???/version
    private void checkForUpdatesOnModrinth(List<DisableableMod> mods,
            Map<DisableableMod, Pair<Object, Object>> modUpdates,
            List<DisableableMod> modsCheckedForUpdates) {
        Map<DisableableMod, String> sha1Hashes = new HashMap<>();
        mods.stream()
                .filter(dm -> dm.getActualFile(instance) != null).forEach(dm -> {
                    try {
                        sha1Hashes.put(dm, Hashing
                                .sha1(dm.getActualFile(instance).toPath()).toString());
                    } catch (Throwable t) {
                        LogManager.logStackTrace(t);
                    }
                });

        Collection<String> values = sha1Hashes.values();
        Map<String, ModrinthVersion> latestVersionsFromHash = reinstalling ? ModrinthApi.getVersionsFromSha1Hashes(
                values.toArray(new String[values.size()]))
                : ModrinthApi.getLatestVersionFromSha1Hashes(
                        values.toArray(new String[values.size()]), instance.id,
                        instance.launcher.loaderVersion);

        List<DisableableMod> modsWithNewerVersions = mods.stream().filter(dm -> {
            ModrinthVersion modrinthVersion = latestVersionsFromHash.get(sha1Hashes.get(dm));

            if (modrinthVersion == null) {
                return false;
            }

            modsCheckedForUpdates.add(dm);

            // if there is no mod info for the mod from Modrinth, but we have found it on there, add it
            if ((dm.modrinthProject == null || dm.modrinthVersion == null) && !dm.dontScanOnModrinth) {
                ModrinthProject modrinthProject = ModrinthApi.getProject(modrinthVersion.projectId);

                if (modrinthProject != null) {
                    ModrinthVersion currentModrinthVersion = ModrinthApi.getVersionFromSha1Hash(sha1Hashes.get(dm));

                    if (currentModrinthVersion != null) {
                        dm.modrinthProject = modrinthProject;
                        dm.modrinthVersion = currentModrinthVersion;
                    } else {
                        dm.dontScanOnModrinth = true;
                    }
                }
            }

            return reinstalling || !dm.modrinthVersion.id.equals(modrinthVersion.id);
        }).collect(Collectors.toList());

        Map<String, ModrinthProject> projectIdsToProjects = ModrinthApi.getProjectsAsMap(
                modsWithNewerVersions.parallelStream().map(mv -> mv.modrinthProject.id)
                        .toArray(String[]::new));

        modsWithNewerVersions.forEach(dm -> {
            executorService.execute(() -> {
                Pair<Boolean, Pair<Object, Object>> update = dm.checkForUpdateOnModrinth(instance,
                        projectIdsToProjects.get(dm.modrinthProject.id), reinstalling);

                if (update.left()) {
                    modUpdates.put(dm, update.right());
                }
            });
        });
    }

    // Request heirachy:
    // 1. Match files to CurseForge files by hash - /v1/fingerprints/432
    // 2. Get all projects that were found by their file hash - /v1/mods
    // 3. In parallel get all files for those projects and filter loader/mc version - /v1/mods/???/files
    private void checkForUpdatesOnCurseForge(List<DisableableMod> mods,
            Map<DisableableMod, Pair<Object, Object>> modUpdates,
            List<DisableableMod> modsCheckedForUpdates) {
        Map<Long, DisableableMod> murmurHashes = new HashMap<>();

        mods.stream()
                .forEach(dm -> {
                    try {
                        long hash = Hashing.murmur(dm.getActualFile(instance).toPath());
                        murmurHashes.put(hash, dm);
                    } catch (Throwable t) {
                        LogManager.logStackTrace(t);
                    }
                });

        if (murmurHashes.size() != 0) {
            CurseForgeFingerprint fingerprintResponse = CurseForgeApi
                    .checkFingerprints(murmurHashes.keySet().stream().toArray(Long[]::new));

            if (fingerprintResponse != null && fingerprintResponse.exactMatches != null) {
                int[] projectIdsFound = fingerprintResponse.exactMatches.stream().mapToInt(em -> em.id)
                        .toArray();

                if (projectIdsFound.length != 0) {
                    Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi
                            .getProjectsAsMap(projectIdsFound);

                    if (foundProjects != null) {
                        fingerprintResponse.exactMatches.stream()
                                .filter(em -> em != null && em.file != null
                                        && murmurHashes.containsKey(em.file.packageFingerprint))
                                .forEach(foundMod -> {
                                    DisableableMod dm = murmurHashes
                                            .get(foundMod.file.packageFingerprint);

                                    CurseForgeProject curseForgeProject = foundProjects
                                            .get(foundMod.id);

                                    if (curseForgeProject != null) {
                                        modsCheckedForUpdates.add(dm);
                                        executorService.execute(() -> {
                                            Pair<Boolean, Pair<Object, Object>> update = dm
                                                    .checkforUpdateOnCurseForge(instance,
                                                            curseForgeProject, reinstalling);

                                            if (update.left()) {
                                                modUpdates.put(dm, update.right());
                                            }
                                        });
                                    }
                                });
                    }
                }
            }
        }
    }
}
