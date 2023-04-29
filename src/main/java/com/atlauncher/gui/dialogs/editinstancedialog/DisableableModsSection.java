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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.joda.time.Instant;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Type;
import com.atlauncher.gui.dialogs.AddModsDialog;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;
import com.formdev.flatlaf.icons.FlatSearchIcon;

public abstract class DisableableModsSection extends SectionPanel {
    private final JTextField searchField = new JTextField(16);
    private final JPopupMenu tableContextMenu = new JPopupMenu();
    private DefaultTableModel tableModel;
    private final JTable table = new JTable();
    private boolean ignoreTableEvents = false;
    private TableRowSorter<TableModel> sorter;

    private final List<Path> filePaths;
    private final List<Type> modTypes;
    private final boolean showVersionColumn;

    public DisableableModsSection(Window parent, Instance instance, List<Path> filePaths, List<Type> modTypes,
            boolean showVersionColumn) {
        super(parent, instance);

        this.filePaths = filePaths;
        this.modTypes = modTypes;
        this.showVersionColumn = showVersionColumn;

        setupComponents();

        setupTableContextMenu();
    }

    private void executeSearch() {
        sorter.setRowFilter(RowFilter.regexFilter(searchField.getText(), 2));
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setBorder(new EmptyBorder(0, 0, 5, 5));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(Box.createHorizontalGlue());

        searchField.setMaximumSize(new Dimension(100, 23));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                executeSearch();
            }
        });
        searchField.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        searchField.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            searchField.setText("");
            executeSearch();
        });
        topPanel.add(searchField);

        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(0);
        splitPane.setBorder(null);
        splitPane.setEnabled(false);
        splitPane.setResizeWeight(1.0);

        tableModel = new DefaultTableModel(new Object[][] {}, new String[] {
                "", GetText.tr("Enabled?"), GetText.tr("Name"), GetText.tr("Version"), GetText.tr("Last Change")
        }) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return DisableableMod.class;
                }

                if (columnIndex == 1) {
                    return Boolean.class;
                }

                if (columnIndex == 4) {
                    return Date.class;
                }

                return String.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 1;
            }
        };

        refreshTableModel();

        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (!ignoreTableEvents) {
                    int column = e.getColumn();

                    if (column == 1) {
                        int row = e.getFirstRow();
                        disableEnableRows(new int[] { row }, false, true);
                    }
                }
            }
        });

        table.setModel(tableModel);
        table.setShowVerticalLines(false);
        table.setDefaultRenderer(String.class, new TableCellRenderer());
        table.getTableHeader().setReorderingAllowed(false);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point point = e.getPoint();

                    int row = table.rowAtPoint(point);

                    if (row >= 0) {
                        if (!table.isRowSelected(row)) {
                            table.setRowSelectionInterval(row, row);
                        }

                        tableContextMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(1).setMaxWidth(70);
        cm.getColumn(1).setPreferredWidth(70);
        cm.getColumn(4).setMaxWidth(160);
        cm.getColumn(4).setPreferredWidth(160);

        cm.getColumn(4).setCellRenderer(new TableCellRenderer() {
            SimpleDateFormat formatter = new SimpleDateFormat(App.settings.dateFormat + " HH:mm:ss a");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setValue(formatter.format((Date) value));
                return this;
            }
        });

        // remove the 0th column so it's not visible
        cm.removeColumn(cm.getColumn(0));

        if (!showVersionColumn) {
            cm.removeColumn(cm.getColumn(2));
        }

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        sorter.setSortable(4, true);
        sorter.setComparator(4, Comparator.comparing(o -> (Date) o));

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        JScrollPane tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            {
                this.getVerticalScrollBar().setUnitIncrement(8);
            }
        };

        splitPane.setLeftComponent(tableScrollPane);

        SideBarButton downloadMoreSideBarButton = new SideBarButton(GetText.tr("Download More"));
        if (instance.launcher.loaderVersion == null && modTypes.contains(Type.mods)) {
            downloadMoreSideBarButton.setEnabled(false);
            downloadMoreSideBarButton
                    .setToolTipText(GetText.tr("In order to download mods, you must have a modloader installed"));
        } else {
            downloadMoreSideBarButton.setEnabled(true);
            downloadMoreSideBarButton.setToolTipText(null);
        }
        downloadMoreSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "AddMods",
                        instance.getAnalyticsCategory());
                new AddModsDialog(parent, instance, modTypes.get(0));
                refreshTableModel();
            }
        });

        SideBarButton addFileSideBarButton = new SideBarButton(GetText.tr("Add File"));

        SideBarButton checkForUpdatesSideBarButton = new SideBarButton(GetText.tr("Check For Updates"));
        checkForUpdatesSideBarButton
                .setToolTipText(GetText.tr("Select one or more items in the table to the left"));
        checkForUpdatesSideBarButton.setEnabled(false);
        checkForUpdatesSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkForUpdates(Arrays.stream(table.getSelectedRows())
                        .map(row -> table.convertRowIndexToModel(row)).toArray());
            }
        });

        SideBarButton reinstallSideBarButton = new SideBarButton(GetText.tr("Reinstall"));
        reinstallSideBarButton
                .setToolTipText(GetText.tr("Select one or more items in the table to the left"));
        reinstallSideBarButton.setEnabled(false);
        reinstallSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reinstallMods(Arrays.stream(table.getSelectedRows())
                        .map(row -> table.convertRowIndexToModel(row)).toArray());
            }
        });

        SideBarButton enableSideBarButton = new SideBarButton(GetText.tr("Enable"));
        enableSideBarButton.setToolTipText(GetText.tr("Select one or more items in the table to the left"));
        enableSideBarButton.setEnabled(false);
        enableSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableEnableRows(Arrays.stream(table.getSelectedRows())
                        .map(row -> table.convertRowIndexToModel(row)).toArray(), false, false);
            }
        });

        SideBarButton disableSideBarButton = new SideBarButton(GetText.tr("Disable"));
        disableSideBarButton.setToolTipText(GetText.tr("Select one or more items in the table to the left"));
        disableSideBarButton.setEnabled(false);
        disableSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableEnableRows(Arrays.stream(table.getSelectedRows())
                        .map(row -> table.convertRowIndexToModel(row)).toArray(), true, false);
            }
        });

        SideBarButton deleteSideBarButton = new SideBarButton(GetText.tr("Delete"));
        deleteSideBarButton.setToolTipText(GetText.tr("Select one or more items in the table to the left"));
        deleteSideBarButton.setEnabled(false);
        deleteSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteRows(Arrays.stream(table.getSelectedRows())
                        .map(row -> table.convertRowIndexToModel(row)).toArray());
            }
        });

        SideBarButton refreshSideBarButton = new SideBarButton(GetText.tr("Refresh List"));
        refreshSideBarButton.setToolTipText(GetText.tr("Refreshes the table with any changes from the filesystem"));
        refreshSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTableModel();
            }
        });

        SideBarButton openFolderSideBarButton = new SideBarButton(GetText.tr("Open Folder"));
        openFolderSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OS.openFileExplorer(filePaths.get(0).getParent());
            }
        });

        JToolBar sideBar = new JToolBar();
        sideBar.setMinimumSize(new Dimension(160, 0));
        sideBar.setPreferredSize(new Dimension(160, 0));
        sideBar.setOrientation(SwingConstants.VERTICAL);
        sideBar.setFloatable(false);

        sideBar.addSeparator();
        sideBar.add(downloadMoreSideBarButton);
        sideBar.add(addFileSideBarButton);
        sideBar.addSeparator();
        sideBar.add(checkForUpdatesSideBarButton);
        sideBar.add(reinstallSideBarButton);
        sideBar.addSeparator();
        sideBar.add(enableSideBarButton);
        sideBar.add(disableSideBarButton);
        sideBar.addSeparator();
        sideBar.add(deleteSideBarButton);
        sideBar.addSeparator();
        sideBar.add(Box.createVerticalGlue());
        sideBar.addSeparator();
        sideBar.add(refreshSideBarButton);
        sideBar.addSeparator();
        sideBar.add(openFolderSideBarButton);

        splitPane.setRightComponent(sideBar);

        // change enabled/disabled for sideBar buttons
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean hasSomethingSelected = table.getSelectedRowCount() != 0;

                    checkForUpdatesSideBarButton.setEnabled(hasSomethingSelected);
                    reinstallSideBarButton.setEnabled(hasSomethingSelected);
                    enableSideBarButton.setEnabled(hasSomethingSelected);
                    disableSideBarButton.setEnabled(hasSomethingSelected);
                    deleteSideBarButton.setEnabled(hasSomethingSelected);

                    if (hasSomethingSelected) {
                        checkForUpdatesSideBarButton
                                .setToolTipText(GetText.tr("Check for updates to the selected item/s"));
                        reinstallSideBarButton.setToolTipText(
                                GetText.tr("Reinstall the selected item/s, choosing which version to install"));
                        enableSideBarButton
                                .setToolTipText(
                                        GetText.tr("Enable the selected item/s for play in the current instance"));
                        disableSideBarButton
                                .setToolTipText(
                                        GetText.tr("Disable the selected item/s for play in the current instance"));
                        deleteSideBarButton
                                .setToolTipText(
                                        GetText.tr("Delete the selected item/s completely from the current instance"));
                    } else {
                        checkForUpdatesSideBarButton
                                .setToolTipText(GetText.tr("Select one or more items in the table to the left"));
                        reinstallSideBarButton
                                .setToolTipText(GetText.tr("Select one or more items in the table to the left"));
                        enableSideBarButton
                                .setToolTipText(GetText.tr("Select one or more items in the table to the left"));
                        disableSideBarButton
                                .setToolTipText(GetText.tr("Select one or more items in the table to the left"));
                        deleteSideBarButton
                                .setToolTipText(GetText.tr("Select one or more items in the table to the left"));
                    }
                }
            }
        });

        add(splitPane, BorderLayout.CENTER);
    }

    private void refreshTableModel() {
        List<Path> modPaths = instance.getModPathsFromFilesystem(this.filePaths);

        List<DisableableMod> newMods = new ArrayList<>();

        List<Pair<Path, DisableableMod>> modsData = modPaths.stream().map(path -> {
            DisableableMod mod = instance.launcher.mods.parallelStream()
                    .filter(m -> {
                        try {
                            return modTypes.contains(m.type) && (m.getActualFile(instance).toPath().equals(path)
                                    || m.file.equals(path.getFileName().toString().toLowerCase())
                                    || m.file.equals(path.getFileName().toString().toUpperCase()));
                        } catch (InvalidPathException e) {
                            LogManager.warn("Invalid path for mod " + m.name);
                            return false;
                        }
                    })
                    .findFirst()
                    .orElseGet(() -> {
                        LogManager
                                .warn(String.format(
                                        "Failed to find DisableableMod for file %s. Generating DisableableMod for it.",
                                        path.getFileName().toString()));

                        int indexOfModPath = modPaths.indexOf(path.getParent());
                        DisableableMod generatedMod = DisableableMod.generateMod(path.toFile(),
                                modTypes.get(indexOfModPath == -1 ? 0 : indexOfModPath), !path.endsWith(".disabled"));
                        generatedMod.scanInternalModMetadata(path);
                        newMods.add(generatedMod);
                        return generatedMod;
                    });

            return new Pair<Path, DisableableMod>(path, mod);
        }).collect(Collectors.toList());

        instance.launcher.mods.addAll(newMods);
        instance.save();

        ignoreTableEvents = true;

        // clear current table model
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            tableModel.removeRow(i);
        }

        // add all mods in
        modsData.forEach(pair -> {
            BasicFileAttributes attrs = null;
            try {
                attrs = Files.readAttributes(pair.left(), BasicFileAttributes.class);
            } catch (IOException ignored) {
            }

            tableModel.addRow(new Object[] { pair.right(), !pair.right().disabled,
                    pair.right().getNameFromFile(instance, pair.left()),
                    pair.right().getVersionFromFile(instance, pair.left()),
                    attrs == null ? Instant.now().toDate() : new Date(attrs.lastModifiedTime().toMillis()) });
        });

        ignoreTableEvents = false;
    }

    private void reloadRows(int[] rows) {
        ignoreTableEvents = true;
        for (int row : rows) {
            DisableableMod mod = (DisableableMod) tableModel.getValueAt(row, 0);
            Path actualPath = mod.getActualFile(instance).toPath();
            BasicFileAttributes attrs = null;
            try {
                attrs = Files.readAttributes(actualPath, BasicFileAttributes.class);
            } catch (IOException ignored) {
            }

            tableModel.setValueAt(!mod.disabled, row, 1);
            tableModel.setValueAt(mod.getNameFromFile(instance), row, 2);
            tableModel.setValueAt(mod.getVersionFromFile(instance), row, 3);
            tableModel.setValueAt(
                    attrs == null ? Instant.now().toDate() : new Date(attrs.lastModifiedTime().toMillis()),
                    row, 4);
        }
        ignoreTableEvents = false;
    }

    private void disableEnableRows(int[] rows, boolean disable, boolean toggle) {
        for (int row : rows) {
            DisableableMod mod = (DisableableMod) tableModel.getValueAt(row, 0);

            boolean shouldEnable = toggle ? mod.disabled : !disable;
            if (shouldEnable) {
                mod.enable(instance);
            } else {
                mod.disable(instance);
            }
        }
        instance.save();
        reloadRows(rows);
    }

    private void deleteRows(int[] rows) {
        ignoreTableEvents = true;
        Integer[] sortedArr = Arrays.stream(rows).boxed()
                .sorted(Comparator.reverseOrder()).toArray(Integer[]::new);

        for (int row : sortedArr) {
            DisableableMod mod = (DisableableMod) tableModel.getValueAt(row, 0);

            instance.launcher.mods.remove(mod);
            Utils.delete(mod.getActualFile(instance));
            tableModel.removeRow(row);
        }
        instance.save();
        ignoreTableEvents = false;
    }

    private void checkForUpdates(int[] rows) {
        List<DisableableMod> mods = Arrays.stream(rows).boxed().map(row -> {
            return (DisableableMod) tableModel.getValueAt(row, 0);
        }).collect(Collectors.toList());

        new CheckForUpdatesDialog(this.parent, this.instance, mods, false,
                (Map<DisableableMod, DisableableMod> updatedMods) -> {
                    saveAndReloadAfterUpdatingMods(rows, updatedMods);
                });
    }

    private void reinstallMods(int[] rows) {
        List<DisableableMod> mods = Arrays.stream(rows).boxed().map(row -> {
            return (DisableableMod) tableModel.getValueAt(row, 0);
        }).collect(Collectors.toList());

        new CheckForUpdatesDialog(this.parent, this.instance, mods, true,
                (Map<DisableableMod, DisableableMod> updatedMods) -> {
                    saveAndReloadAfterUpdatingMods(rows, updatedMods);
                });
    }

    private void saveAndReloadAfterUpdatingMods(int[] rows, Map<DisableableMod, DisableableMod> updatedMods) {
        for (Entry<DisableableMod, DisableableMod> entry : updatedMods.entrySet()) {
            DisableableMod oldMod = entry.getKey();
            DisableableMod newMod = entry.getValue();

            if (oldMod == null || newMod == null || !instance.launcher.mods.remove(oldMod)) {
                continue;
            }

            // update the mods list
            instance.launcher.mods.remove(oldMod);
            instance.launcher.mods.add(newMod);

            // then update the tables Mod to be correct
            Optional<Integer> foundRow = Arrays.stream(rows).boxed()
                    .filter(row -> tableModel.getValueAt(row, 0) == oldMod).findFirst();

            if (foundRow.isPresent()) {
                tableModel.setValueAt(newMod, foundRow.get(), 0);
            }
        }

        instance.save();

        reloadRows(rows);
    }

    private Optional<DisableableMod> getFirstSelectedMod() {
        if (table.getSelectedRow() == -1) {
            return Optional.empty();
        }

        int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
        DisableableMod mod = (DisableableMod) tableModel.getValueAt(selectedRow, 0);

        return Optional.of(mod);
    }

    private void setupTableContextMenu() {
        JMenuItem fileNameMenuItem = new JMenuItem();
        fileNameMenuItem.setEnabled(false);

        JMenuItem checkForUpdateMenuItem = new JMenuItem(GetText.tr("Check For Updates"));
        checkForUpdateMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                checkForUpdates(new int[] { selectedRow });
            }
        });

        JMenuItem reinstallMenuItem = new JMenuItem(GetText.tr("Reinstall"));
        reinstallMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                reinstallMods(new int[] { selectedRow });
            }
        });

        JMenuItem enableMenuItem = new JMenuItem(GetText.tr("Enable"));
        enableMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                disableEnableRows(new int[] { selectedRow }, false, false);
            }
        });

        JMenuItem disableMenuItem = new JMenuItem(GetText.tr("Disable"));
        disableMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                disableEnableRows(new int[] { selectedRow }, true, false);
            }
        });

        JMenuItem deleteMenuItem = new JMenuItem(GetText.tr("Delete"));
        deleteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                deleteRows(new int[] { selectedRow });
            }
        });

        JPopupMenu.Separator lastSeparator = new JPopupMenu.Separator();

        JMenuItem showInExplorerMenuItem = new JMenuItem(GetText.tr("Show In File Explorer"));
        showInExplorerMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> mod = getFirstSelectedMod();

                if (mod.isPresent()) {
                    OS.openFileExplorer(mod.get().getFile(instance).toPath(), true);
                } else {
                    OS.openFileExplorer(instance.ROOT.resolve("mods"), false);
                }
            }
        });

        JMenuItem openDiscordMenuItem = new JMenuItem(GetText.tr("Open Discord"));
        openDiscordMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> selectedMod = getFirstSelectedMod();
                if (selectedMod.isPresent() && selectedMod.get().getDiscordInviteUrl() != null) {
                    OS.openWebBrowser(selectedMod.get().getDiscordInviteUrl());
                }
            }
        });

        JMenuItem openSourceMenuItem = new JMenuItem(GetText.tr("Open Source"));
        openSourceMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> selectedMod = getFirstSelectedMod();
                if (selectedMod.isPresent() && selectedMod.get().getSourceUrl() != null) {
                    OS.openWebBrowser(selectedMod.get().getSourceUrl());
                }
            }
        });

        JMenuItem openSupportMenuItem = new JMenuItem(GetText.tr("Open Support"));
        openSupportMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> selectedMod = getFirstSelectedMod();
                if (selectedMod.isPresent() && selectedMod.get().getSupportUrl() != null) {
                    OS.openWebBrowser(selectedMod.get().getSupportUrl());
                }
            }
        });

        JMenuItem openWebsiteMenuItem = new JMenuItem(GetText.tr("Open Website"));
        openWebsiteMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> selectedMod = getFirstSelectedMod();
                if (selectedMod.isPresent() && selectedMod.get().getWebsiteUrl() != null) {
                    OS.openWebBrowser(selectedMod.get().getWebsiteUrl());
                }
            }
        });

        JMenuItem openWikiMenuItem = new JMenuItem(GetText.tr("Open Wiki"));
        openWikiMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> selectedMod = getFirstSelectedMod();
                if (selectedMod.isPresent() && selectedMod.get().getWikiUrl() != null) {
                    OS.openWebBrowser(selectedMod.get().getWikiUrl());
                }
            }
        });

        // change visibility based on row selection
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !ignoreTableEvents) {
                    boolean hasExactlyOneModSelected = table.getSelectedRowCount() == 1;

                    lastSeparator.setVisible(hasExactlyOneModSelected);
                    showInExplorerMenuItem.setVisible(hasExactlyOneModSelected);

                    Optional<DisableableMod> selectedMod = getFirstSelectedMod();

                    if (table.getSelectedRow() != -1) {
                        int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
                        DisableableMod mod = (DisableableMod) tableModel.getValueAt(selectedRow, 0);
                        fileNameMenuItem.setText(mod.getActualFile(instance).getName());

                        openDiscordMenuItem.setVisible(hasExactlyOneModSelected && selectedMod.isPresent()
                                && selectedMod.get().getDiscordInviteUrl() != null);
                        openSourceMenuItem.setVisible(hasExactlyOneModSelected && selectedMod.isPresent()
                                && selectedMod.get().getSourceUrl() != null);
                        openSupportMenuItem.setVisible(hasExactlyOneModSelected && selectedMod.isPresent()
                                && selectedMod.get().getSupportUrl() != null);
                        openWebsiteMenuItem.setVisible(hasExactlyOneModSelected && selectedMod.isPresent()
                                && selectedMod.get().getWebsiteUrl() != null);
                        openWikiMenuItem.setVisible(hasExactlyOneModSelected && selectedMod.isPresent()
                                && selectedMod.get().getWikiUrl() != null);
                    }
                }
            }
        });

        // add menu items to the context menu
        tableContextMenu.add(fileNameMenuItem);
        tableContextMenu.addSeparator();
        tableContextMenu.add(checkForUpdateMenuItem);
        tableContextMenu.add(reinstallMenuItem);
        tableContextMenu.addSeparator();
        tableContextMenu.add(enableMenuItem);
        tableContextMenu.add(disableMenuItem);
        tableContextMenu.addSeparator();
        tableContextMenu.add(deleteMenuItem);
        tableContextMenu.add(lastSeparator);
        tableContextMenu.add(showInExplorerMenuItem);
        tableContextMenu.add(openDiscordMenuItem);
        tableContextMenu.add(openSourceMenuItem);
        tableContextMenu.add(openSupportMenuItem);
        tableContextMenu.add(openWebsiteMenuItem);
        tableContextMenu.add(openWikiMenuItem);
    }
}
