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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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

import com.atlauncher.data.Instance;
import com.formdev.flatlaf.icons.FlatSearchIcon;

public class ModsSection extends JPanel {
    private Instance instance;

    private JTextField searchField = new JTextField(16);
    private TableModel modsTableModel;

    public ModsSection(Instance instance) {
        super();

        this.instance = instance;

        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());

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

        Object[][] modsData = new Object[][] {
                { "test-mod-1.jar", false, "Test", "1.0.0" },
                { "test-mod-2.jar", true, "Test", "2.2.2.2" },
                { "test-mod-3.jar", false, "Test", "fabric-5.5.5-1.19.2" },
                { "test-mod-4.jar", false, "Test", "Unknown" },
        };

        modsTableModel = new DefaultTableModel(modsData, new String[] {
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
        JTable modsTable = new JTable(modsTableModel);

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

        JScrollPane modsTableScrollPane = new JScrollPane(modsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            {
                this.getVerticalScrollBar().setUnitIncrement(8);
            }
        };

        splitPane.setLeftComponent(modsTableScrollPane);

        JToolBar modsSideBar = new JToolBar();
        modsSideBar.setOrientation(SwingConstants.VERTICAL);
        modsSideBar.addSeparator();
        modsSideBar.add(new JButton(GetText.tr("Download Mods")));
        modsSideBar.add(new JButton(GetText.tr("Add File")));
        modsSideBar.addSeparator();
        modsSideBar.add(new JButton(GetText.tr("Check For Updates")));
        modsSideBar.add(new JButton(GetText.tr("Reinstall")));
        modsSideBar.addSeparator();
        modsSideBar.add(new JButton(GetText.tr("Enable")));
        modsSideBar.add(new JButton(GetText.tr("Disable")));
        modsSideBar.addSeparator();
        modsSideBar.add(new JButton(GetText.tr("Enable All")));
        modsSideBar.add(new JButton(GetText.tr("Disable All")));
        modsSideBar.addSeparator();
        splitPane.setRightComponent(modsSideBar);

        add(splitPane, BorderLayout.CENTER);
    }
}
