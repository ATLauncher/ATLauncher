/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2025 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.atlauncher.gui.tabs.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.viewmodel.impl.settings.EnvironmentVariablesViewModel;
import com.formdev.flatlaf.ui.FlatScrollPaneBorder;

public class EnvironmentVariablesTab extends AbstractSettingsTab {

    private final EnvironmentVariablesViewModel viewModel;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, deleteButton, clearButton;

    public EnvironmentVariablesTab(EnvironmentVariablesViewModel viewModel) {
        this.viewModel = viewModel;
        setLayout(new BorderLayout());
    }

    @Override
    protected void onShow() {
        tableModel = new DefaultTableModel(new Object[] { GetText.tr("Name"), GetText.tr("Value") }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int row = table.getSelectedRow();
                    int col = table.getSelectedColumn();
                    table.editCellAt(row, col);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && table.rowAtPoint(e.getPoint()) != -1) {
                    table.setRowSelectionInterval(table.rowAtPoint(e.getPoint()), table.rowAtPoint(e.getPoint()));
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem deleteItem = new JMenuItem(GetText.tr("Delete"));
                    deleteItem.addActionListener(ae -> deleteSelected());
                    menu.add(deleteItem);
                    menu.show(table, e.getX(), e.getY());
                }
            }
        });

        JLabel infoLabel = new JLabel(new HTMLBuilder()
                .center()
                .text(GetText.tr(
                        "This page allows you to manage environment variables for your Minecraft instances."
                                + "<br/>Double-click a variable name or value in the table below to edit it."))
                .build());
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton(GetText.tr("Add"));
        addButton.addActionListener(e -> {
            String key = DialogManager.okDialog().setTitle(GetText.tr("Environment Variable Name"))
                    .setContent(GetText.tr("Environment Variable Name:")).showInput("ENV_VARIABLE");
            String value = DialogManager.okDialog().setTitle(GetText.tr("Environment Variable Value"))
                    .setContent(GetText.tr("Environment Variable Value:")).showInput("ENV_VARIABLE_VALUE");

            if (key != null && value != null) {
                boolean added = viewModel.addVariable(key, value);
                if (!added) {
                    DialogManager.okDialog().setType(DialogManager.ERROR).setTitle(GetText.tr("Duplicate Variable"))
                            .setContent(GetText.tr(
                                    "A variable with this name already exists. You can edit the existing value by double clicking the value."))
                            .show();
                }
            }
        });
        deleteButton = new JButton(GetText.tr("Delete"));
        deleteButton.addActionListener(e -> deleteSelected());
        clearButton = new JButton(GetText.tr("Clear"));
        clearButton.addActionListener(e -> clearAll());
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // Stack info label and buttons vertically at the top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(infoLabel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        scrollPane.setMaximumSize(new Dimension(600, 400));
        scrollPane.setMinimumSize(new Dimension(600, 200));
        scrollPane.setBorder(new FlatScrollPaneBorder());
        scrollPane.setViewportView(table);

        // Fixed-width content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setPreferredSize(new Dimension(600, 480));
        contentPanel.setMaximumSize(new Dimension(600, 1000));
        contentPanel.setMinimumSize(new Dimension(600, 200));
        contentPanel.add(topPanel);
        contentPanel.add(scrollPane);

        // Center the content panel in the tab
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 20));
        add(contentPanel);

        // Set column widths: Name = 1/3, Value = 2/3 of 600px
        table.getColumnModel().getColumn(0).setPreferredWidth(600 / 3);
        table.getColumnModel().getColumn(1).setPreferredWidth(600 - (600 / 3));

        addDisposable(viewModel.getEnvironmentVariablesObservable().subscribe(vars -> {
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                for (Map.Entry<String, String> entry : vars.entrySet()) {
                    tableModel.addRow(new Object[] { entry.getKey(), entry.getValue() });
                }
            });
        }));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String name = (String) tableModel.getValueAt(row, 0);
            viewModel.removeVariable(name);
        }
    }

    private void clearAll() {
        int confirm = JOptionPane.showConfirmDialog(this, GetText.tr("Clear all environment variables?"),
                GetText.tr("Confirm"), JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            viewModel.clearAll();
        }
    }

    @Override
    public String getTitle() {
        return GetText.tr("Environment Variables");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Environment Variables";
    }

    @Override
    protected void createViewModel() {}

    @Override
    protected void onDestroy() {
        removeAll();
        table = null;
        tableModel = null;
        addButton = null;
        deleteButton = null;
        clearButton = null;
    }
}
