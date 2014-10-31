/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.gui;

import com.atlauncher.data.Account;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.awt.Dimension;

@SuppressWarnings("serial")
public class AccountsDropDownRenderer extends JLabel implements ListCellRenderer<Account> {
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
    @SuppressWarnings("rawtypes")
    public Component getListCellRendererComponent(JList list, Account account, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        if (account == null) {
            return this;
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        ImageIcon icon = account.getMinecraftHead();
        String username = account.getMinecraftUsername();
        setIcon(icon);
        setText(username);
        setFont(list.getFont());
        return this;
    }
}
