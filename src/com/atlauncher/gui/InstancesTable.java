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

import com.atlauncher.data.Instance;
import com.atlauncher.data.Instances;

@SuppressWarnings("serial")
public class InstancesTable extends JTable {
    
    InstancesTableModel instancesTableModel;

    public InstancesTable(Instances instances) {
        instancesTableModel = new InstancesTableModel(instances);
        setModel(instancesTableModel);
        setRowHeight(50);
        setSelectionBackground(Color.GRAY);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setResizingAllowed(false);
        getColumnModel().getColumn(0).setMinWidth(100);
        getColumnModel().getColumn(1).setMinWidth(100);
    }

    public Instance getSelectedInstance() {
        return (Instance) getValueAt(getSelectedRow(), -1);
    }
    
    public void reload() {
        instancesTableModel.fireTableDataChanged();
    }

}
