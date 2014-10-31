/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
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
