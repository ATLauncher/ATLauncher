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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Type;
import com.atlauncher.managers.LogManager;
import com.formdev.flatlaf.icons.FlatSearchIcon;

public class ResourcePacksSection extends SectionPanel {
    private JTextField searchField = new JTextField(16);
    private TableModel tableModel;

    public ResourcePacksSection(Window parent, Instance instance) {
        super(parent, instance);

        setupComponents();
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

        List<Path> modPaths = instance.getModPathsFromFilesystem(
                Arrays.asList(instance.ROOT.resolve("resourcepacks"), instance.ROOT.resolve("texturepacks")));

        Object[][] modsData = modPaths.stream().map(path -> {
            DisableableMod mod = instance.launcher.mods.parallelStream()
                    .filter(m -> (m.type == Type.resourcepack || m.type == Type.texturepack)
                            && m.file.equals(path.getFileName().toString()))
                    .findFirst()
                    .orElseGet(() -> {
                        LogManager
                                .warn(String.format("Failed to find mod for file %s. Generating temporary mod for it.",
                                        path.getFileName().toString()));
                        return DisableableMod.generateMod(path.toFile(), Type.resourcepack,
                                !path.endsWith(".disabled"));
                    });

            return new Object[] { instance.ROOT.relativize(path).toString(), !mod.disabled, mod.name, mod.version };
        }).toArray(Object[][]::new);

        tableModel = new DefaultTableModel(modsData, new String[] {
                "", GetText.tr("Enabled?"), GetText.tr("Name"), GetText.tr("Version")
        }) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) {
                    return Boolean.class;
                }

                return String.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == 1;
            }
        };
        JTable modsTable = new JTable(tableModel);

        modsTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int column = e.getColumn();

                if (column == 1) {
                    int row = e.getFirstRow();
                    TableModel model = (TableModel) e.getSource();
                    Boolean data = (Boolean) model.getValueAt(row, column);
                    String filename = (String) model.getValueAt(row, 0);

                    System.out.println(filename);
                    System.out.println(data);
                }
            }
        });
        modsTable.setShowVerticalLines(false);
        modsTable.setDefaultRenderer(String.class, new TableCellRenderer());
        modsTable.getTableHeader().setReorderingAllowed(false);

        TableColumnModel cm = modsTable.getColumnModel();
        cm.getColumn(1).setMaxWidth(65);
        cm.removeColumn(cm.getColumn(0));

        JScrollPane tableScrollPane = new JScrollPane(modsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            {
                this.getVerticalScrollBar().setUnitIncrement(8);
            }
        };

        splitPane.setLeftComponent(tableScrollPane);

        JToolBar sideBar = new JToolBar();
        sideBar.setMinimumSize(new Dimension(160, 0));
        sideBar.setPreferredSize(new Dimension(160, 0));
        sideBar.setOrientation(SwingConstants.VERTICAL);
        sideBar.setFloatable(false);

        sideBar.addSeparator();
        sideBar.add(new SideBarButton(GetText.tr("Download Resource Packs")));
        sideBar.add(new SideBarButton(GetText.tr("Add File")));
        sideBar.addSeparator();
        sideBar.add(new SideBarButton(GetText.tr("Check For Updates")));
        sideBar.add(new SideBarButton(GetText.tr("Reinstall")));
        sideBar.addSeparator();
        sideBar.add(new SideBarButton(GetText.tr("Enable")));
        sideBar.add(new SideBarButton(GetText.tr("Disable")));
        sideBar.addSeparator();
        sideBar.add(new SideBarButton(GetText.tr("Enable All")));
        sideBar.add(new SideBarButton(GetText.tr("Disable All")));
        sideBar.addSeparator();

        splitPane.setRightComponent(sideBar);

        add(splitPane, BorderLayout.CENTER);
    }
}
