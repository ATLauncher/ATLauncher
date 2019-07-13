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
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.gui.card.CurseModCard;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.gui.panels.NoCurseModsPanel;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseApi;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public final class AddModsDialog extends JDialog {
    private Instance instance;
    private InstanceV2 instanceV2;

    private JPanel contentPanel = new JPanel(new GridLayout(Constants.CURSE_PAGINATION_SIZE / 2, 2));
    private JPanel topPanel = new JPanel(new BorderLayout());
    private JTextField searchField = new JTextField(16);
    private JButton searchButton = new JButton(GetText.tr("Search"));
    private JButton installFabricApiButton = new JButton("Install Fabric API");
    private JScrollPane jscrollPane;
    private JButton nextButton;
    private JButton prevButton;
    private JPanel mainPanel = new JPanel(new BorderLayout());
    private int page = 0;

    public AddModsDialog(Instance instance) {
        // #. {0} is the name of the mod we're installing
        super(App.settings.getParent(), GetText.tr("Adding Mods For {0}", instance.getName()),
                ModalityType.APPLICATION_MODAL);
        this.instance = instance;

        this.setPreferredSize(new Dimension(550, 450));
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setupComponents();

        this.loadDefaultMods();

        this.pack();
        this.setLocationRelativeTo(App.settings.getParent());
        this.setVisible(true);
    }

    public AddModsDialog(InstanceV2 instanceV2) {
        // #. {0} is the name of the mod we're installing
        super(App.settings.getParent(), GetText.tr("Adding Mods For {0}", instanceV2.launcher.name),
                ModalityType.APPLICATION_MODAL);
        this.instanceV2 = instanceV2;

        this.setPreferredSize(new Dimension(550, 450));
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setupComponents();

        this.loadDefaultMods();

        this.pack();
        this.setLocationRelativeTo(App.settings.getParent());
        this.setVisible(true);
    }

    private void setupComponents() {
        JPanel searchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchButtonsPanel.add(new JLabel(GetText.tr("Search") + ": "));
        searchButtonsPanel.add(this.searchField);
        searchButtonsPanel.add(this.searchButton);

        this.installFabricApiButton.addActionListener(e -> {
            CurseMod mod = CurseApi.getModById(Constants.CURSE_FABRIC_MOD_ID);

            Analytics.sendEvent("AddFabricApi", "CurseMod");
            if (this.instanceV2 != null) {
                new CurseModFileSelectorDialog(mod, instanceV2);
            } else {
                new CurseModFileSelectorDialog(mod, instance);
            }
        });

        if ((this.instanceV2 != null ? this.instanceV2.launcher.loaderVersion : this.instance.getLoaderVersion())
                .isFabric()
                && (this.instanceV2 != null ? instanceV2.launcher.mods : instance.getInstalledMods()).stream()
                        .filter(mod -> mod.isFromCurse() && mod.getCurseModId() == Constants.CURSE_FABRIC_MOD_ID)
                        .count() == 0) {
            searchButtonsPanel.add(this.installFabricApiButton);

            JLabel fabricApiWarningLabel = new JLabel(
                    "<html><p align=\"center\" style=\"color: yellow\">Before installing Fabric mods, you should install Fabric API first!</p></html>");
            this.topPanel.add(fabricApiWarningLabel, BorderLayout.CENTER);
        }

        this.topPanel.add(searchButtonsPanel, BorderLayout.NORTH);

        this.jscrollPane = new JScrollPane(this.contentPanel) {
            {
                this.getVerticalScrollBar().setUnitIncrement(16);
            }
        };

        this.jscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(this.topPanel, BorderLayout.NORTH);
        mainPanel.add(this.jscrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());

        prevButton = new JButton("<<");
        prevButton.setEnabled(false);
        prevButton.addActionListener(e -> goToPreviousPage());

        nextButton = new JButton(">>");
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> goToNextPage());

        bottomPanel.add(prevButton);
        bottomPanel.add(nextButton);

        this.add(mainPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        this.searchField.addActionListener(e -> searchForMods());

        this.searchButton.addActionListener(e -> searchForMods());
    }

    private void setLoading(boolean loading) {
        if (loading) {
            contentPanel.removeAll();
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new LoadingPanel(), BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    private void goToPreviousPage() {
        if (page > 0) {
            page -= 1;
        }

        Analytics.sendEvent(page, "Previous", "Navigation", "CurseMod");

        getMods();
    }

    private void goToNextPage() {
        if (contentPanel.getComponentCount() != 0) {
            page += 1;
        }

        Analytics.sendEvent(page, "Next", "Navigation", "CurseMod");

        getMods();
    }

    private void getMods() {
        setLoading(true);
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);

        Runnable r = () -> {
            if ((this.instanceV2 != null ? this.instanceV2.launcher.loaderVersion : this.instance.getLoaderVersion())
                    .isFabric()) {
                setMods(CurseApi.searchModsForFabric(
                        this.instanceV2 != null ? this.instanceV2.id : this.instance.getMinecraftVersion(), "", page));
            } else {
                setMods(CurseApi.searchMods(
                        this.instanceV2 != null ? this.instanceV2.id : this.instance.getMinecraftVersion(), "", page));
            }

            setLoading(false);
        };

        new Thread(r).start();
    }

    private void loadDefaultMods() {
        getMods();
    }

    private void searchForMods() {
        setLoading(true);
        page = 0;

        Runnable r = () -> {
            String query = searchField.getText();

            List<CurseMod> mods = CurseApi.searchMods(
                    this.instanceV2 != null ? this.instanceV2.id : this.instance.getMinecraftVersion(), query, page);

            setMods(mods);

            Analytics.sendEvent(mods.size(), query, "Search", "CurseMod");

            setLoading(false);
        };

        new Thread(r).start();
    }

    private void setMods(List<CurseMod> mods) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.set(2, 2, 2, 2);

        contentPanel.removeAll();

        prevButton.setEnabled(page > 0);
        nextButton.setEnabled(mods.size() == Constants.CURSE_PAGINATION_SIZE);

        if (mods.size() == 0) {
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new NoCurseModsPanel(), BorderLayout.CENTER);
        } else {
            contentPanel.setLayout(new GridLayout(mods.size() / 2, 2));

            mods.stream().forEach(curseMod -> {
                if (this.instanceV2 != null) {
                    contentPanel.add(new CurseModCard(curseMod, this.instanceV2), gbc);
                } else {
                    contentPanel.add(new CurseModCard(curseMod, this.instance), gbc);
                }
                gbc.gridy++;
            });
        }

        SwingUtilities.invokeLater(() -> jscrollPane.getVerticalScrollBar().setValue(0));

        revalidate();
        repaint();
    }
}
