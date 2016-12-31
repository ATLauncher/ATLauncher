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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.atlauncher.data.Account;

@SuppressWarnings("serial")
public class AccountsDropDownRenderer extends JLabel implements ListCellRenderer {
    public AccountsDropDownRenderer() {
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setPreferredSize(new Dimension(200, 40));
        setIconTextGap(10);
    }

    /**
     * This finds the image and text corresponding to the selected value and returns the label to be displayed in the
     * bottom accounts selection dropdown.
     *
     * @param list The JList we're painting
     * @param object the account we're rendering
     * @param index The cells index
     * @param isSelected True if the specified cell was selected
     * @param cellHasFocus True if the specified cell has the focus
     * @return A component whose paint() method will render the specified value
     */
    @SuppressWarnings("rawtypes")
    public Component getListCellRendererComponent(JList list, Object object, int index, boolean isSelected, boolean
            cellHasFocus) {
        if (object == null) {
            return this;
        }
        
        Account account = (Account)object;

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
