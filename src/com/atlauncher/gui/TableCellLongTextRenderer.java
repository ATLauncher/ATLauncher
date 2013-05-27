package com.atlauncher.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

public class TableCellLongTextRenderer extends JTextArea implements
        TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        this.setText((String) value);
        this.setWrapStyleWord(true);
        this.setLineWrap(true);
        table.setRowHeight(row, 75);
        return this;
    }

}