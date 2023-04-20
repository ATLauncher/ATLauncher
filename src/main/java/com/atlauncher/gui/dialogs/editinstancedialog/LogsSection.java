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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.data.Instance;

public class LogsSection extends SectionPanel {
    public LogsSection(Window parent, Instance instance) {
        super(parent, instance);

        setupComponents();
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        JComboBox<String> logsComboBox = new JComboBox<>();
        logsComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
                "Select A Log To View",
                "logs/latest.log",
                "logs/2023-04-14-3.log.gz",
                "logs/2023-04-14-2.log.gz",
                "logs/2023-04-14-1.log.gz",
                "logs/rei.log",
                "logs/rei-issues.log"
        }));
        topPanel.add(logsComboBox);
        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(1.0);
        splitPane.setEnabled(false);

        JScrollPane logScrollPane = new JScrollPane();
        logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JEditorPane logEditorPane = new JEditorPane();
        logEditorPane.setText("Select a log from the dropdown above to view.");
        logEditorPane.setEditable(false);
        logEditorPane.setEnabled(false);
        logScrollPane.setViewportView(logEditorPane);

        splitPane.setLeftComponent(logScrollPane);

        JToolBar sideBar = new JToolBar();
        sideBar.setMinimumSize(new Dimension(160, 0));
        sideBar.setPreferredSize(new Dimension(160, 0));
        sideBar.setOrientation(SwingConstants.VERTICAL);
        sideBar.setFloatable(false);

        sideBar.addSeparator();

        SideBarButton uploadButton = new SideBarButton(GetText.tr("Upload"));
        uploadButton.setEnabled(false);
        sideBar.add(uploadButton);

        SideBarButton copyToClipboardButton = new SideBarButton(GetText.tr("Copy To Clipboard"));
        copyToClipboardButton.setEnabled(false);
        sideBar.add(copyToClipboardButton);

        SideBarButton showInExplorerButton = new SideBarButton(GetText.tr("Show In File Explorer"));
        showInExplorerButton.setEnabled(false);
        sideBar.add(showInExplorerButton);
        sideBar.addSeparator();

        SideBarButton deleteButton = new SideBarButton(GetText.tr("Delete"));
        sideBar.add(deleteButton);

        SideBarButton deleteAllButton = new SideBarButton(GetText.tr("Delete All"));
        sideBar.add(deleteAllButton);
        sideBar.addSeparator();
        sideBar.add(Box.createVerticalGlue());
        sideBar.addSeparator();

        SideBarButton openFolderButton = new SideBarButton(GetText.tr("Open Folder"));
        sideBar.add(openFolderButton);
        splitPane.setRightComponent(sideBar);
        add(splitPane, BorderLayout.CENTER);
    }
}
