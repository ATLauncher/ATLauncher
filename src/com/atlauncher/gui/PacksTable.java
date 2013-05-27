/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.gui;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.atlauncher.data.Pack;

public class PacksTable extends JTable {

    PackTableModel packTableModel;
    
    public PacksTable() {
        packTableModel = new PackTableModel(LauncherFrame.settings.getPacks());
        setModel(packTableModel);
        setRowHeight(50);
        setSelectionBackground(Color.GRAY);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setResizingAllowed(false);
        getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(400);
        getColumnModel().getColumn(getColumnCount() - 1).setMaxWidth(400);
        getColumnModel().getColumn(getColumnCount() - 1).setCellRenderer(new TableCellLongTextRenderer());
    }

    public Pack getSelectedPack() {
        return (Pack) getValueAt(getSelectedRow(), -1);
    }
    
    public void reload() {
        packTableModel.fireTableDataChanged();
    }

}
