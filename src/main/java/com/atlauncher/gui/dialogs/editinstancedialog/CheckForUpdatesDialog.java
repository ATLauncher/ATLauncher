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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.gui.card.ModUpdatesChooserCard;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.gui.layouts.WrapLayout;
import com.atlauncher.gui.panels.CenteredTextPanel;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;

public class CheckForUpdatesDialog extends JDialog {
    private final Instance instance;
    private final List<DisableableMod> mods;
    private final List<ModUpdatesChooserCard> modUpdateCards = new ArrayList<>();

    private boolean checking = false;

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JComboBox<ComboItem<ModPlatform>> platformComboBox = new JComboBox<>();
    private ExecutorService modCheckExecutor;
    private final JButton updateButton = new JButton(GetText.tr("Update"));
    private JButton closeButton = new JButton(GetText.tr("Close"));
    private int modsToUpdate = 0;

    public CheckForUpdatesDialog(Window parent, Instance instance, List<DisableableMod> mods) {
        super(parent);

        this.instance = instance;
        this.mods = mods;

        Analytics.sendScreenView("Check For Updates Dialog");

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
        if (modCheckExecutor != null && !modCheckExecutor.isShutdown()) {
            modCheckExecutor.shutdownNow();
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
                        "The mod platform to use when checking for updates.<br/>The preferred platform when searching all platforms can be set in the launchers Mods settings tab."))
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

        updateButton.setEnabled(false);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ModUpdatesChooserCard modUpdateCard : modUpdateCards) {
                    if (modUpdateCard.isCurseForgeMod()) {
                        CurseForgeFile currentVersion = (CurseForgeFile) modUpdateCard.getCurrentVersion();
                        CurseForgeFile updateVersion = (CurseForgeFile) modUpdateCard.getVersionUpdatingTo();

                        if (modUpdateCard.isUpdating() && currentVersion.id != updateVersion.id) {
                            System.out.println(String.format("CurseForge Mod %s: Updating from %s to %s",
                                    modUpdateCard.mod.name,
                                    currentVersion.displayName,
                                    updateVersion.displayName));
                        } else {
                            System.out.println(String.format("CurseForge Mod %s: Not updating",
                                    modUpdateCard.mod.name));
                        }
                    } else if (modUpdateCard.isModrinthMod()) {
                        ModrinthVersion currentVersion = (ModrinthVersion) modUpdateCard.getCurrentVersion();
                        ModrinthVersion updateVersion = (ModrinthVersion) modUpdateCard.getVersionUpdatingTo();

                        if (modUpdateCard.isUpdating() && !currentVersion.id.equals(updateVersion.id)) {
                            System.out.println(String.format("Modrinth Mod %s: Updating from %s to %s",
                                    modUpdateCard.mod.name, currentVersion.name,
                                    updateVersion.name));
                        } else {
                            System.out.println(String.format("Modrinth Mod %s: Not updating", modUpdateCard.mod.name));
                        }
                    }
                }
            }
        });
        bottomPanel.add(updateButton);
        bottomPanel.add(Box.createHorizontalGlue());

        add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void addLoadingPanel() {
        mainPanel.removeAll();
        mainPanel.revalidate();
        mainPanel.repaint();
        mainPanel.add(new LoadingPanel(GetText.tr("Checking For Updates")), BorderLayout.CENTER);
        setTitle(GetText.tr("Checking For Updates"));
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
                    addLoadingPanel();
                });

                ModPlatform platform = ((ComboItem<ModPlatform>) platformComboBox.getSelectedItem()).getValue();

                Map<DisableableMod, Pair<Object, Object>> modUpdates = Collections.synchronizedMap(new HashMap<>());

                // check all mods for update
                modCheckExecutor = Executors.newFixedThreadPool(10);
                for (DisableableMod mod : mods) {
                    modCheckExecutor.execute(() -> {
                        Pair<Boolean, Pair<Object, Object>> update = mod.checkForUpdate(instance, platform);

                        if (update.left()) {
                            modUpdates.put(mod, update.right());
                        }
                    });
                }
                modCheckExecutor.shutdown();

                try {
                    if (!modCheckExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                        modCheckExecutor.shutdownNow();
                    }
                } catch (InterruptedException ex) {
                    modCheckExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }

                // load in mods panel
                SwingUtilities.invokeLater(() -> {
                    if (modUpdates.size() == 0) {
                        mainPanel.removeAll();
                        mainPanel.revalidate();
                        mainPanel.repaint();
                        mainPanel.add(new CenteredTextPanel(GetText.tr("No Updates Found")), BorderLayout.CENTER);
                        setTitle(GetText.tr("No Updates Found"));
                    } else {
                        JPanel modsPanel = new JPanel(new WrapLayout());
                        for (Map.Entry<DisableableMod, Pair<Object, Object>> entry : modUpdates.entrySet()) {
                            modUpdateCards
                                    .add(new ModUpdatesChooserCard(this, instance, entry.getKey(), entry.getValue(),
                                            (boolean checked) -> {
                                                if (checked) {
                                                    modsToUpdate += 1;
                                                } else {
                                                    modsToUpdate -= 1;
                                                }

                                                SwingUtilities.invokeLater(() -> {
                                                    updateButton.setEnabled(modsToUpdate != 0);
                                                    updateButton.setToolTipText(modsToUpdate != 0 ? null
                                                            : GetText.tr("No Mods Selected For Update"));
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
                        setTitle(GetText.tr("Select Updates For {0} Mods", modUpdates.size()));
                    }

                    platformComboBox.setEnabled(true);
                    updateButton.setEnabled(modUpdates.size() != 0);
                    updateButton.setToolTipText(modUpdates.size() != 0 ? null
                            : GetText.tr("No Mods To Update"));
                    closeButton.setText(GetText.tr("Close"));
                });
                checking = false;
            }).start();
        }
    }
}
