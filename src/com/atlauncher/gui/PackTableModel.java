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

import com.atlauncher.data.Pack;

@SuppressWarnings("serial")
public class PackTableModel extends AbstractTableModel {

    ArrayList<Pack> packs;

    public PackTableModel() {
        this.packs = LauncherFrame.settings.getPacks();
    }

    @Override
    public int getRowCount() {
        return packs.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case -1:
                return packs.get(rowIndex);
            case 0:
                return packs.get(rowIndex).getName();
            case 1:
            default:
                return packs.get(rowIndex).getDescription();
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Name";
            case 1:
            default:
                return "Description";
        }
    }

}
