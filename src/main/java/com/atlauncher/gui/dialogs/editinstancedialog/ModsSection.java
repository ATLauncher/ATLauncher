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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
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
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;
import com.formdev.flatlaf.icons.FlatSearchIcon;

public class ModsSection extends SectionPanel {
    private JTextField searchField = new JTextField(16);
    private JPopupMenu tableContextMenu = new JPopupMenu();
    private DefaultTableModel tableModel;
    private JTable modsTable = new JTable();
    private boolean ignoreTableEvents = false;

    public ModsSection(Instance instance) {
        super(instance);

        setupComponents();

        setupTableContextMenu();
    }

    private void executeSearch() {
        System.out.println(searchField.getText());
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setBorder(new EmptyBorder(0, 0, 5, 5));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(Box.createHorizontalGlue());

        searchField.setMaximumSize(new Dimension(100, 23));
        searchField.addKeyListener(new KeyAdapter() {
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

        List<Path> modPaths = instance.getModPathsFromFilesystem(Arrays.asList(instance.ROOT.resolve("mods")));

        List<Pair<Path, DisableableMod>> modsData = modPaths.stream().map(path -> {
            DisableableMod mod = instance.launcher.mods.parallelStream()
                    .filter(m -> m.type == Type.mods && (m.getActualFile(instance).toPath().equals(path)
                            || m.file.equals(path.getFileName().toString().toLowerCase())
                            || m.file.equals(path.getFileName().toString().toUpperCase())))
                    .findFirst()
                    .orElseGet(() -> {
                        LogManager
                                .warn(String.format("Failed to find mod for file %s. Generating temporary mod for it.",
                                        path.getFileName().toString()));
                        return DisableableMod.generateMod(path.toFile(), Type.mods, !path.endsWith(".disabled"));
                    });

            return new Pair<Path, DisableableMod>(path, mod);
        }).collect(Collectors.toList());

        Object[][] initialTableData = modsData.stream().map(pair -> {
            BasicFileAttributes attrs = null;
            try {
                attrs = Files.readAttributes(pair.left(), BasicFileAttributes.class);
            } catch (IOException ignored) {
            }

            return new Object[] { instance.ROOT.relativize(pair.left()).toString(), !pair.right().disabled,
                    pair.right().getNameFromFile(instance, pair.left()),
                    pair.right().getVersionFromFile(instance, pair.left()),
                    attrs == null ? Instant.now().toDate() : new Date(attrs.lastModifiedTime().toMillis()) };
        }).toArray(Object[][]::new);

        tableModel = new DefaultTableModel(initialTableData, new String[] {
                "", GetText.tr("Enabled?"), GetText.tr("Name"), GetText.tr("Version"), GetText.tr("Last Change")
        }) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
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

        modsTable.setModel(tableModel);
        modsTable.setShowVerticalLines(false);
        modsTable.setDefaultRenderer(String.class, new TableCellRenderer());
        modsTable.getTableHeader().setReorderingAllowed(false);

        modsTable.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point point = e.getPoint();

                    int row = modsTable.rowAtPoint(point);

                    if (row >= 0) {
                        if (!modsTable.isRowSelected(row)) {
                            modsTable.setRowSelectionInterval(row, row);
                        }

                        tableContextMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        TableColumnModel cm = modsTable.getColumnModel();
        cm.getColumn(1).setMaxWidth(70);

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

        // remove the filename column so it's not visible
        cm.removeColumn(cm.getColumn(0));

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(tableModel);
        modsTable.setRowSorter(sorter);

        sorter.setSortable(4, true);
        sorter.setComparator(4, Comparator.comparing(o -> (Date) o));

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        JScrollPane tableScrollPane = new JScrollPane(modsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            {
                this.getVerticalScrollBar().setUnitIncrement(8);
            }
        };

        splitPane.setLeftComponent(tableScrollPane);

        JButton downloadModsSideBarButton = new JButton(GetText.tr("Download Mods"));
        JButton addFileSideBarButton = new JButton(GetText.tr("Add File"));

        JButton checkForUpdatesSideBarButton = new JButton(GetText.tr("Check For Updates"));
        checkForUpdatesSideBarButton
                .setToolTipText(GetText.tr("Select one or more mods in the table to the left"));
        checkForUpdatesSideBarButton.setEnabled(false);
        checkForUpdatesSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkForUpdates(Arrays.stream(modsTable.getSelectedRows())
                        .map(row -> modsTable.convertRowIndexToModel(row)).toArray());
            }
        });

        JButton reinstallSideBarButton = new JButton(GetText.tr("Reinstall"));
        reinstallSideBarButton
                .setToolTipText(GetText.tr("Select one or more mods in the table to the left"));
        reinstallSideBarButton.setEnabled(false);
        reinstallSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reinstallMods(Arrays.stream(modsTable.getSelectedRows())
                        .map(row -> modsTable.convertRowIndexToModel(row)).toArray());
            }
        });

        JButton enableSideBarButton = new JButton(GetText.tr("Enable"));
        enableSideBarButton.setToolTipText(GetText.tr("Select one or more mods in the table to the left"));
        enableSideBarButton.setEnabled(false);
        enableSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableEnableRows(Arrays.stream(modsTable.getSelectedRows())
                        .map(row -> modsTable.convertRowIndexToModel(row)).toArray(), false, false);
            }
        });

        JButton disableSideBarButton = new JButton(GetText.tr("Disable"));
        disableSideBarButton.setToolTipText(GetText.tr("Select one or more mods in the table to the left"));
        disableSideBarButton.setEnabled(false);
        disableSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableEnableRows(Arrays.stream(modsTable.getSelectedRows())
                        .map(row -> modsTable.convertRowIndexToModel(row)).toArray(), true, false);
            }
        });

        JButton deleteSideBarButton = new JButton(GetText.tr("Delete"));
        deleteSideBarButton.setToolTipText(GetText.tr("Select one or more mods in the table to the left"));
        deleteSideBarButton.setEnabled(false);
        deleteSideBarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteRows(Arrays.stream(modsTable.getSelectedRows())
                        .map(row -> modsTable.convertRowIndexToModel(row)).toArray());
            }
        });

        JToolBar sideBar = new JToolBar();
        sideBar.setOrientation(SwingConstants.VERTICAL);
        sideBar.addSeparator();
        sideBar.add(downloadModsSideBarButton);
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
        splitPane.setRightComponent(sideBar);

        // change enabled/disabled for sideBar buttons
        modsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean hasSomethingSelected = modsTable.getSelectedRowCount() != 0;

                    checkForUpdatesSideBarButton.setEnabled(hasSomethingSelected);
                    reinstallSideBarButton.setEnabled(hasSomethingSelected);
                    enableSideBarButton.setEnabled(hasSomethingSelected);
                    disableSideBarButton.setEnabled(hasSomethingSelected);
                    deleteSideBarButton.setEnabled(hasSomethingSelected);

                    if (hasSomethingSelected) {
                        checkForUpdatesSideBarButton
                                .setToolTipText(GetText.tr("Check for updates to the selected mod/s"));
                        reinstallSideBarButton.setToolTipText(
                                GetText.tr("Reinstall the selected mod/s, choosing which version to install"));
                        enableSideBarButton
                                .setToolTipText(GetText.tr("Enable the mod/s for play in the current instance"));
                        disableSideBarButton
                                .setToolTipText(GetText.tr("Disable the mod/s for play in the current instance"));
                        deleteSideBarButton
                                .setToolTipText(GetText.tr("Delete the mod/s completely from the current instance"));
                    } else {
                        checkForUpdatesSideBarButton
                                .setToolTipText(GetText.tr("Select one or more mods in the table to the left"));
                        reinstallSideBarButton
                                .setToolTipText(GetText.tr("Select one or more mods in the table to the left"));
                        enableSideBarButton
                                .setToolTipText(GetText.tr("Select one or more mods in the table to the left"));
                        disableSideBarButton
                                .setToolTipText(GetText.tr("Select one or more mods in the table to the left"));
                        deleteSideBarButton
                                .setToolTipText(GetText.tr("Select one or more mods in the table to the left"));
                    }
                }
            }
        });

        add(splitPane, BorderLayout.CENTER);
    }

    private void reloadRows(int[] rows) {
        ignoreTableEvents = true;
        for (int row : rows) {
            String filename = (String) tableModel.getValueAt(row, 0);

            Optional<DisableableMod> mod = instance.launcher.mods.stream()
                    .filter(m -> m.type == Type.mods
                            && (m.getFile(instance).toPath().equals(instance.ROOT.resolve(filename))
                                    || m.getDisabledFile(instance).toPath().equals(instance.ROOT.resolve(filename))))
                    .findFirst();

            if (mod.isPresent()) {
                Path actualPath = mod.get().getActualFile(instance).toPath();
                BasicFileAttributes attrs = null;
                try {
                    attrs = Files.readAttributes(actualPath, BasicFileAttributes.class);
                } catch (IOException ignored) {
                }

                tableModel.setValueAt(instance.ROOT.relativize(actualPath).toString(),
                        row, 0);
                tableModel.setValueAt(!mod.get().disabled, row, 1);
                tableModel.setValueAt(
                        attrs == null ? Instant.now().toDate() : new Date(attrs.lastModifiedTime().toMillis()),
                        row, 4);
            }
        }
        ignoreTableEvents = false;
    }

    private void disableEnableRows(int[] rows, boolean disable, boolean toggle) {
        for (int row : rows) {
            String filename = (String) tableModel.getValueAt(row, 0);

            Optional<DisableableMod> modOptional = instance.launcher.mods.stream()
                    .filter(m -> m.type == Type.mods
                            && m.getActualFile(instance).toPath().equals(instance.ROOT.resolve(filename)))
                    .findFirst();

            if (modOptional.isPresent()) {
                DisableableMod mod = modOptional.get();

                boolean shouldEnable = toggle ? mod.disabled : !disable;
                if (shouldEnable) {
                    LogManager.debug(String.format("Enabling mod %s", mod.name));
                    mod.enable(instance);
                } else {
                    LogManager.debug(String.format("Disabling mod %s", mod.name));
                    mod.disable(instance);
                }
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
            String filename = (String) tableModel.getValueAt(row, 0);

            Optional<DisableableMod> modOptional = instance.launcher.mods.stream()
                    .filter(m -> m.type == Type.mods
                            && m.getActualFile(instance).toPath().equals(instance.ROOT.resolve(filename)))
                    .findFirst();

            if (modOptional.isPresent()) {
                DisableableMod mod = modOptional.get();

                instance.launcher.mods.remove(mod);
                Utils.delete(mod.getActualFile(instance));
                tableModel.removeRow(row);
            }
        }
        instance.save();
        ignoreTableEvents = false;
    }

    private void checkForUpdates(int[] rows) {
        ignoreTableEvents = true;

        List<DisableableMod> mods = Arrays.stream(rows).boxed().map(row -> {
            String filename = (String) tableModel.getValueAt(row, 0);

            return instance.launcher.mods.stream()
                    .filter(m -> m.type == Type.mods
                            && m.getActualFile(instance).toPath().equals(instance.ROOT.resolve(filename)))
                    .findFirst();
        }).filter(m -> m.isPresent()).map(m -> m.get()).collect(Collectors.toList());

        instance.save();
        ignoreTableEvents = false;
    }

    private void reinstallMods(int[] rows) {
        ignoreTableEvents = true;

        List<DisableableMod> mods = Arrays.stream(rows).boxed().map(row -> {
            String filename = (String) tableModel.getValueAt(row, 0);

            return instance.launcher.mods.stream()
                    .filter(m -> m.type == Type.mods
                            && m.getActualFile(instance).toPath().equals(instance.ROOT.resolve(filename)))
                    .findFirst();
        }).filter(m -> m.isPresent()).map(m -> m.get()).collect(Collectors.toList());

        instance.save();
        ignoreTableEvents = false;
    }

    private Optional<DisableableMod> getFirstSelectedMod() {
        int selectedRow = modsTable.convertRowIndexToModel(modsTable.getSelectedRow());
        String filename = (String) tableModel.getValueAt(selectedRow, 0);

        return instance.launcher.mods.stream()
                .filter(m -> m.type == Type.mods
                        && m.getActualFile(instance).toPath().equals(instance.ROOT.resolve(filename)))
                .findFirst();
    }

    private void setupTableContextMenu() {
        JMenuItem fileNameMenuItem = new JMenuItem();
        fileNameMenuItem.setEnabled(false);

        JMenuItem checkForUpdateMenuItem = new JMenuItem(GetText.tr("Check For Updates"));
        checkForUpdateMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = modsTable.convertRowIndexToModel(modsTable.getSelectedRow());
                checkForUpdates(new int[] { selectedRow });
            }
        });

        JMenuItem reinstallMenuItem = new JMenuItem(GetText.tr("Reinstall"));
        reinstallMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = modsTable.convertRowIndexToModel(modsTable.getSelectedRow());
                reinstallMods(new int[] { selectedRow });
            }
        });

        JMenuItem enableMenuItem = new JMenuItem(GetText.tr("Enable"));
        enableMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = modsTable.convertRowIndexToModel(modsTable.getSelectedRow());
                disableEnableRows(new int[] { selectedRow }, false, false);
            }
        });

        JMenuItem disableMenuItem = new JMenuItem(GetText.tr("Disable"));
        disableMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = modsTable.convertRowIndexToModel(modsTable.getSelectedRow());
                disableEnableRows(new int[] { selectedRow }, true, false);
            }
        });

        JMenuItem deleteMenuItem = new JMenuItem(GetText.tr("Delete"));
        deleteMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = modsTable.convertRowIndexToModel(modsTable.getSelectedRow());
                deleteRows(new int[] { selectedRow });
            }
        });

        JPopupMenu.Separator lastSeparator = new JPopupMenu.Separator();

        JMenuItem showInExplorerMenuItem = new JMenuItem(GetText.tr("Show In File Explorer"));
        showInExplorerMenuItem.addActionListener(new ActionListener() {
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
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> selectedMod = getFirstSelectedMod();
                if (selectedMod.isPresent() && selectedMod.get().getDiscordInviteUrl() != null) {
                    OS.openWebBrowser(selectedMod.get().getDiscordInviteUrl());
                }
            }
        });

        JMenuItem openSourceMenuItem = new JMenuItem(GetText.tr("Open Source"));
        openSourceMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> selectedMod = getFirstSelectedMod();
                if (selectedMod.isPresent() && selectedMod.get().getSourceUrl() != null) {
                    OS.openWebBrowser(selectedMod.get().getSourceUrl());
                }
            }
        });

        JMenuItem openSupportMenuItem = new JMenuItem(GetText.tr("Open Support"));
        openSupportMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> selectedMod = getFirstSelectedMod();
                if (selectedMod.isPresent() && selectedMod.get().getSupportUrl() != null) {
                    OS.openWebBrowser(selectedMod.get().getSupportUrl());
                }
            }
        });

        JMenuItem openWebsiteMenuItem = new JMenuItem(GetText.tr("Open Website"));
        openWebsiteMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> selectedMod = getFirstSelectedMod();
                if (selectedMod.isPresent() && selectedMod.get().getWebsiteUrl() != null) {
                    OS.openWebBrowser(selectedMod.get().getWebsiteUrl());
                }
            }
        });

        JMenuItem openWikiMenuItem = new JMenuItem(GetText.tr("Open Wiki"));
        openWikiMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Optional<DisableableMod> selectedMod = getFirstSelectedMod();
                if (selectedMod.isPresent() && selectedMod.get().getWikiUrl() != null) {
                    OS.openWebBrowser(selectedMod.get().getWikiUrl());
                }
            }
        });

        // change visibility based on row selection
        modsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !ignoreTableEvents) {
                    boolean hasExactlyOneModSelected = modsTable.getSelectedRowCount() == 1;

                    lastSeparator.setVisible(hasExactlyOneModSelected);
                    showInExplorerMenuItem.setVisible(hasExactlyOneModSelected);

                    Optional<DisableableMod> selectedMod = getFirstSelectedMod();

                    int selectedRow = modsTable.convertRowIndexToModel(modsTable.getSelectedRow());
                    String filename = (String) tableModel.getValueAt(selectedRow, 0);
                    fileNameMenuItem.setText(filename);

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
