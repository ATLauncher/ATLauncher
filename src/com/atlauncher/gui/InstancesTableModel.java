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

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.atlauncher.data.Instance;

@SuppressWarnings("serial")
public class InstancesTableModel extends AbstractTableModel {

    ArrayList<Instance> instances;

    public InstancesTableModel() {
        this.instances = LauncherFrame.settings.getInstances();
    }

    @Override
    public int getRowCount() {
        return instances.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case -1:
                return instances.get(rowIndex);
            case 0:
                return instances.get(rowIndex).getName();
            case 1:
                return instances.get(rowIndex).getPackName();
            case 2:
                return instances.get(rowIndex).getLatestVersion();
            case 3:
            default:
                return instances.get(rowIndex).getLatestVersion();
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Name";
            case 1:
                return "Pack";
            case 2:
                return "Installed Version";
            case 3:
            default:
                return "Latest Version";
        }
    }

}
