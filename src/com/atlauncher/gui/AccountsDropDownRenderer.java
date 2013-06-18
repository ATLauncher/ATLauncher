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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.atlauncher.data.Account;

public class AccountsDropDownRenderer extends JLabel implements ListCellRenderer {

    public AccountsDropDownRenderer() {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setPreferredSize(new Dimension(200, 40));
        setIconTextGap(10);
    }

    /*
     * This method finds the image and text corresponding to the selected value and returns the
     * label, set up to display the text and image.
     */
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        Account account = (Account) value;

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        // Set the icon and text. If icon was null, say so.
        ImageIcon icon = account.getMinecraftHead();
        String username = account.getMinecraftUsername();
        setIcon(icon);
        setText(username);
        setFont(list.getFont());

        return this;
    }
}
