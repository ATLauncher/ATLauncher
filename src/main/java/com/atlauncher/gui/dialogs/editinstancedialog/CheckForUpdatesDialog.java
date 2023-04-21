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
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.gui.card.ModUpdatesChooserCard;
import com.atlauncher.gui.layouts.WrapLayout;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ComboItem;

public class CheckForUpdatesDialog extends JDialog {
    private final Instance instance;
    private final List<DisableableMod> mods;

    private boolean checking = false;

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JComboBox<ComboItem<ModPlatform>> platformComboBox = new JComboBox<>();

    public CheckForUpdatesDialog(Window parent, Instance instance, List<DisableableMod> mods) {
        super(parent);

        this.instance = instance;
        this.mods = mods;

        Analytics.sendScreenView("Check For Updates Dialog");

        setLayout(new BorderLayout());
        setResizable(true);
        setTitle(GetText.tr("Checking For Updates For {0} Mods", mods.size()));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        setMinimumSize(new Dimension(950, 650));

        setupComponents();

        checkForUpdates();

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void close() {
        dispose();
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")),
                new EmptyBorder(5, 5, 5, 5)));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        platformComboBox.addItem(new ComboItem<ModPlatform>(ModPlatform.CURSEFORGE, "CurseForge"));
        platformComboBox.addItem(new ComboItem<ModPlatform>(ModPlatform.MODRINTH, "Modrinth"));
        platformComboBox.setMaximumSize(new Dimension(100, 23));
        platformComboBox.setPreferredSize(new Dimension(100, 23));

        platformComboBox.setSelectedIndex(App.settings.defaultModPlatform == ModPlatform.CURSEFORGE ? 0 : 1);

        platformComboBox.addActionListener(e -> {
            checkForUpdates();
        });

        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(new JLabel(GetText.tr("Platform:")));
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(platformComboBox);

        add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")),
                new EmptyBorder(5, 5, 5, 5)));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));

        bottomPanel.add(Box.createHorizontalGlue());

        JButton cancelButton = new JButton(GetText.tr("Cancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        bottomPanel.add(cancelButton);

        bottomPanel.add(Box.createHorizontalStrut(20));

        JButton updateButton = new JButton(GetText.tr("Update"));
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
    }

    private void checkForUpdates() {
        if (!checking) {
            new Thread(() -> {
                checking = true;

                SwingUtilities.invokeLater(() -> {
                    platformComboBox.setEnabled(false);
                    addLoadingPanel();
                });

                // check for updates
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }

                // load in mods panel
                SwingUtilities.invokeLater(() -> {
                    JPanel modsPanel = new JPanel(new WrapLayout());
                    for (DisableableMod mod : mods) {
                        modsPanel.add(new ModUpdatesChooserCard(instance, mod));
                    }

                    JScrollPane modsScrollPane = new JScrollPane(modsPanel,
                            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
                        {
                            this.getVerticalScrollBar().setUnitIncrement(8);
                        }
                    };

                    mainPanel.removeAll();
                    mainPanel.revalidate();
                    mainPanel.repaint();
                    mainPanel.add(modsScrollPane, BorderLayout.CENTER);
                    platformComboBox.setEnabled(true);
                });
                checking = false;
            }).start();
        }
    }
}
