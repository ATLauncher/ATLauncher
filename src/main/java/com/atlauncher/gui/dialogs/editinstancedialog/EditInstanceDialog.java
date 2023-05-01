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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.network.Analytics;

public class EditInstanceDialog extends JDialog {
    private Instance instance;

    public JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

    private final ModsSection modsSection;
    private final ResourcePacksSection resourcePacksSection;
    private final ShaderPacksSection shaderPacksSection;
    private final NotesSection notesSection;

    public EditInstanceDialog(Instance instance) {
        this(App.launcher.getParent(), instance);
    }

    public EditInstanceDialog(Window parent, Instance instance) {
        super(parent, GetText.tr("Editing Instance {0}", instance.launcher.name), ModalityType.DOCUMENT_MODAL);

        this.instance = instance;

        Analytics.sendScreenView("Edit Instance Dialog");

        setLayout(new BorderLayout());
        setResizable(true);
        setMinimumSize(new Dimension(950, 600));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        modsSection = new ModsSection(this, instance);
        resourcePacksSection = new ResourcePacksSection(this, instance);
        shaderPacksSection = new ShaderPacksSection(this, instance);
        notesSection = new NotesSection(this, instance);

        setupTabbedPane();
        setupBottomPanel();

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void close() {
        final String notes = notesSection.getNotes();

        if (!instance.launcher.notes.equals(notes)
                || instance.launcher.wrapNotes != notesSection.wrapCheckBox.isSelected()) {
            instance.launcher.notes = notes;
            instance.launcher.wrapNotes = notesSection.wrapCheckBox.isSelected();
            instance.save();
        }

        dispose();
    }

    private void setupTabbedPane() {
        tabbedPane.setFont(App.THEME.getNormalFont().deriveFont(14.0F));
        tabbedPane.addTab(GetText.tr("Information"), new InformationSection(this, instance));
        tabbedPane.addTab(GetText.tr("Mods"), modsSection);
        tabbedPane.addTab(GetText.tr("Resource Packs"), resourcePacksSection);
        tabbedPane.addTab(GetText.tr("Shader Packs"), shaderPacksSection);
        tabbedPane.addTab(GetText.tr("Logs"), new LogsSection(this, instance));
        tabbedPane.addTab(GetText.tr("Notes"), notesSection);
        tabbedPane.addTab(GetText.tr("Settings"), new SettingsSection(this, instance));
        tabbedPane.setOpaque(true);

        tabbedPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void setupBottomPanel() {
        JPanel bottomActionsPanel = new JPanel();

        bottomActionsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        bottomActionsPanel.setLayout(new BoxLayout(bottomActionsPanel, BoxLayout.X_AXIS));

        JButton playButton = new JButton(GetText.tr("Play"));
        playButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                close();
            });
            instance.launch(false);
        });

        JButton playOfflineButton = new JButton(GetText.tr("Play Offline"));
        playOfflineButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                close();
            });
            instance.launch(true);
        });

        JButton closeButton = new JButton(GetText.tr("Close"));
        closeButton.addActionListener(e -> {
            close();
        });

        bottomActionsPanel.add(Box.createHorizontalGlue());
        bottomActionsPanel.add(playButton);
        bottomActionsPanel.add(Box.createHorizontalStrut(5));
        bottomActionsPanel.add(playOfflineButton);
        bottomActionsPanel.add(Box.createHorizontalStrut(5));
        bottomActionsPanel.add(closeButton);

        add(bottomActionsPanel, BorderLayout.SOUTH);
    }

    public void refreshTableModels() {
        modsSection.refreshTableModel();
        resourcePacksSection.refreshTableModel();
        shaderPacksSection.refreshTableModel();
    }
}
