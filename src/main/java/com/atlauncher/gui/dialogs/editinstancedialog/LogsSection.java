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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.data.ModManagement;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ATLauncherApi;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.OS;
import com.formdev.flatlaf.icons.FlatSearchIcon;

public class LogsSection extends SectionPanel {
    private final JComboBox<ComboItem<Path>> logsComboBox = new JComboBox<>();
    private final JTextField searchField = new JTextField();
    private final JTextArea logTextArea = new JTextArea();
    private final Highlighter highlighter = new DefaultHighlighter();
    private int lastSearchIndex = 0;
    private JScrollPane logScrollPane = new JScrollPane();

    private final SideBarButton uploadSideBarButton = new SideBarButton(GetText.tr("Upload"));
    private final SideBarButton copyToClipboardSideBarButton = new SideBarButton(GetText.tr("Copy To Clipboard"));
    private final SideBarButton showInFileExplorerSideBarButton = new SideBarButton(
        GetText.tr("Show In File Explorer"));
    private final SideBarButton deleteSideBarButton = new SideBarButton(GetText.tr("Delete"));

    public LogsSection(EditDialog parent, ModManagement instanceOrServer) {
        super(parent, instanceOrServer);

        setupComponents();

        loadLogFiles();
    }

    private void executeSearch() {
        highlighter.removeAllHighlights();

        String searchTerm = searchField.getText().toLowerCase();
        String text = logTextArea.getText().toLowerCase();
        int index = text.indexOf(searchTerm, lastSearchIndex + 1);

        // loop back to start if nothing found and not at the start
        if (index == -1 && lastSearchIndex != 0) {
            index = text.indexOf(searchTerm, 0);
        }

        if (index >= 0) {
            lastSearchIndex = index;

            try {
                highlighter.addHighlight(index, lastSearchIndex + searchTerm.length(),
                    DefaultHighlighter.DefaultPainter);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        logsComboBox.addActionListener(e -> {
            if (logsComboBox.getSelectedItem() != null) {
                Path selectedPath = ((ComboItem<Path>) logsComboBox.getSelectedItem()).getValue();

                searchField.setText("");
                logTextArea.setText(null);
                lastSearchIndex = 0;

                uploadSideBarButton.setEnabled(selectedPath != null);
                copyToClipboardSideBarButton.setEnabled(selectedPath != null);
                showInFileExplorerSideBarButton.setEnabled(selectedPath != null);
                deleteSideBarButton.setEnabled(selectedPath != null);

                if (selectedPath != null) {
                    try {
                        String contents;

                        if (selectedPath.toString().endsWith(".gz")) {
                            StringBuilder sb = new StringBuilder();
                            try (InputStream fileStream = new FileInputStream(selectedPath.toFile());
                                InputStream gzipStream = new GZIPInputStream(fileStream);
                                Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
                                BufferedReader buffered = new BufferedReader(decoder)) {
                                String line;
                                while ((line = buffered.readLine()) != null) {
                                    sb.append(line);
                                    sb.append("\n");
                                }
                            } catch (Exception e2) {
                                LogManager.logStackTrace("Failed to read file " + selectedPath.getFileName().toString(),
                                    e2,
                                    false);
                            }
                            contents = sb.toString();
                        } else {
                            contents = new String(Files.readAllBytes(selectedPath));
                        }

                        Document doc = logTextArea.getDocument();
                        doc.insertString(0, contents, null);

                        logTextArea.setCaretPosition(0);
                    } catch (Exception e2) {
                        LogManager.logStackTrace("Failed to read file " + selectedPath.getFileName().toString(), e2,
                            false);
                    }
                }
            }
        });

        searchField.setPreferredSize(new Dimension(250, 23));
        searchField.setMinimumSize(new Dimension(250, 23));
        searchField.setMaximumSize(new Dimension(250, 23));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeSearch();
                }
            }
        });
        searchField.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        searchField.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            searchField.setText("");
            lastSearchIndex = 0;
            executeSearch();
        });

        topPanel.add(logsComboBox);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(searchField);

        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(1.0);
        splitPane.setEnabled(false);

        logTextArea.setHighlighter(highlighter);
        logTextArea.setText("Select a log from the dropdown above to view.");
        logTextArea.setEditable(false);
        logTextArea.setEnabled(true);
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setBackground(UIManager.getColor("TextArea.background"));

        logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        logScrollPane.setViewportView(logTextArea);

        splitPane.setLeftComponent(logScrollPane);

        JToolBar sideBar = new JToolBar();
        sideBar.setMinimumSize(new Dimension(160, 0));
        sideBar.setPreferredSize(new Dimension(160, 0));
        sideBar.setOrientation(SwingConstants.VERTICAL);
        sideBar.setFloatable(false);

        uploadSideBarButton.setEnabled(false);
        uploadSideBarButton.addActionListener(e -> {
            Analytics.trackEvent(AnalyticsEvent.simpleEvent("logs_upload"));
            ATLauncherApi.uploadLog(this.parent, logTextArea.getText());
        });

        copyToClipboardSideBarButton.setEnabled(false);
        copyToClipboardSideBarButton.addActionListener(e -> {
            String text = logTextArea.getText();
            OS.copyToClipboard(text);
        });

        showInFileExplorerSideBarButton.setEnabled(false);
        showInFileExplorerSideBarButton.addActionListener(e -> {
            Path selectedPath = ((ComboItem<Path>) logsComboBox.getSelectedItem()).getValue();
            if (selectedPath != null) {
                OS.openFileExplorer(selectedPath, true);
            }
        });

        deleteSideBarButton.setEnabled(false);
        deleteSideBarButton.addActionListener(e -> {
            Path selectedPath = ((ComboItem<Path>) logsComboBox.getSelectedItem()).getValue();
            if (selectedPath != null) {
                FileUtils.delete(selectedPath);
                loadLogFiles();
            }
        });

        SideBarButton deleteAllButton = new SideBarButton(GetText.tr("Delete All"));
        deleteAllButton.addActionListener(e -> {
            instanceOrServer.getLogPathsFromFilesystem(Arrays.asList(instanceOrServer.getRoot().resolve("logs")))
                .forEach(path -> {
                    FileUtils.delete(path);
                });

            loadLogFiles();
        });

        SideBarButton openFolderButton = new SideBarButton(GetText.tr("Open Folder"));
        openFolderButton.addActionListener(e -> {
            OS.openFileExplorer(instanceOrServer.getRoot().resolve("logs"));
        });

        sideBar.addSeparator();
        sideBar.add(uploadSideBarButton);
        sideBar.add(copyToClipboardSideBarButton);
        sideBar.add(showInFileExplorerSideBarButton);
        sideBar.addSeparator();
        sideBar.add(deleteSideBarButton);
        sideBar.addSeparator();
        sideBar.add(Box.createVerticalGlue());
        sideBar.addSeparator();
        sideBar.add(deleteAllButton);
        sideBar.addSeparator();
        sideBar.add(openFolderButton);

        splitPane.setRightComponent(sideBar);
        add(splitPane, BorderLayout.CENTER);
    }

    private void loadLogFiles() {
        logsComboBox.removeAllItems();
        logsComboBox.addItem(new ComboItem<>(null, GetText.tr("Select Log To View")));

        List<Path> logPaths = instanceOrServer.getLogPathsFromFilesystem(
            Arrays.asList(instanceOrServer.getRoot().resolve("logs")));

        logPaths.forEach(path -> {
            logsComboBox.addItem(new ComboItem<>(path, instanceOrServer.getRoot().relativize(path).toString()));
        });
    }

    @Override
    public void updateUIState() {
    }
}
