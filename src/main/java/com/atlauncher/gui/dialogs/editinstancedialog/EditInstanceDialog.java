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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.Instance;
import com.atlauncher.evnt.listener.MinecraftLaunchListener;
import com.atlauncher.evnt.manager.MinecraftLaunchManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Utils;

public class EditInstanceDialog extends JFrame implements MinecraftLaunchListener {
    private Instance instance;
    private Process runningProcess = null;
    private boolean launching = false;

    public JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);

    private final ModsSection modsSection;
    private final ResourcePacksSection resourcePacksSection;
    private final ShaderPacksSection shaderPacksSection;
    private final NotesSection notesSection;

    private final JButton endProcessButton = new JButton(GetText.tr("End Process"));
    private final JButton playOfflineButton = new JButton(GetText.tr("Play Offline"));
    private final JButton playButton = new JButton(GetText.tr("Play"));

    public EditInstanceDialog(Instance instance) {
        this(App.launcher.getParent(), instance);
    }

    public EditInstanceDialog(Window parent, Instance instance) {
        super(GetText.tr("Editing Instance {0}", instance.launcher.name));

        this.instance = instance;

        MinecraftLaunchManager.addListener(this);

        Analytics.sendScreenView("Edit Instance Dialog");

        setIconImage(Utils.getImage("/assets/image/icon.png"));
        setLayout(new BorderLayout());
        setResizable(true);
        setMinimumSize(new Dimension(950, 600));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                MinecraftLaunchManager.removeListener(EditInstanceDialog.this);
                close();
            }
        });

        modsSection = new ModsSection(this, instance);
        resourcePacksSection = new ResourcePacksSection(this, instance);
        shaderPacksSection = new ShaderPacksSection(this, instance);
        notesSection = new NotesSection(this, instance);

        setupTabbedPane();
        setupBottomPanel();
        updateUIState();

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

        endProcessButton.addActionListener(e -> {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("End Process?"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "Are you sure you want to end the Minecraft process?<br/><br/>Doing so can cause corruption of your instance including corruption of your worlds."))
                            .build())
                    .setType(DialogManager.QUESTION).show();
            if (ret == DialogManager.YES_OPTION) {
                Analytics.sendEvent("EndProcess", "EditInstanceDialog");
                App.launcher.killMinecraft(runningProcess);
            }
        });

        playButton.addActionListener(e -> {
            instance.launch(false);
        });

        playOfflineButton.addActionListener(e -> {
            instance.launch(true);
        });

        JButton closeButton = new JButton(GetText.tr("Close"));
        closeButton.addActionListener(e -> {
            close();
        });

        bottomActionsPanel.add(Box.createHorizontalGlue());
        bottomActionsPanel.add(endProcessButton);
        bottomActionsPanel.add(Box.createHorizontalStrut(5));
        bottomActionsPanel.add(playButton);
        bottomActionsPanel.add(Box.createHorizontalStrut(5));
        bottomActionsPanel.add(playOfflineButton);
        bottomActionsPanel.add(Box.createHorizontalStrut(5));
        bottomActionsPanel.add(closeButton);

        add(bottomActionsPanel, BorderLayout.SOUTH);
    }

    private void updateUIState() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            JLabel titleLabel = new JLabel(tabbedPane.getTitleAt(i));
            titleLabel.setFont(App.THEME.getBoldFont().deriveFont(14f));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            tabbedPane.setTabComponentAt(i, titleLabel);
        }

        boolean isLaunchingOrLaunched = launching || runningProcess != null;

        endProcessButton.setVisible(isLaunchingOrLaunched);

        playButton.setEnabled(!isLaunchingOrLaunched);
        playButton.setToolTipText(!isLaunchingOrLaunched ? null : GetText.tr("Minecraft is already running"));

        playOfflineButton.setEnabled(!isLaunchingOrLaunched);
        playOfflineButton.setToolTipText(!isLaunchingOrLaunched ? null : GetText.tr("Minecraft is already running"));
    }

    public void refreshTableModels() {
        modsSection.refreshTableModel();
        resourcePacksSection.refreshTableModel();
        shaderPacksSection.refreshTableModel();
    }

    @Override
    public void minecraftLaunching(Instance instance) {
        if (instance == this.instance) {
            launching = true;
            updateUIState();
        } else {
            close();
        }
    }

    @Override
    public void minecraftLaunchFailed(Instance instance, String reason) {
        if (instance == this.instance) {
            launching = false;
            updateUIState();
        }
    }

    @Override
    public void minecraftLaunched(Instance instance, AbstractAccount account, Process process) {
        if (instance == this.instance) {
            launching = false;
            runningProcess = process;
            updateUIState();
        }
    }

    @Override
    public void minecraftClosed(Instance instance) {
        if (instance == this.instance) {
            launching = false;
            runningProcess = null;
            updateUIState();
        }
    }
}
