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

package com.atlauncher.gui.components;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.Color;

public final class Console extends JTextPane {
    /**
     * Auto generate serial.
     */
    private static final long serialVersionUID = 5325985090210097809L;
    private final SimpleAttributeSet attrs = new SimpleAttributeSet();

    public Console() {
        this.setEditable(false);
    }

    public Console setColor(Color c) {
        StyleConstants.setForeground(this.attrs, c);
        return this;
    }

    public Console setBold(boolean b) {
        StyleConstants.setBold(this.attrs, b);
        return this;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true; // Word Wrapping
    }

    public void write(String str) {
        try {
            this.getDocument().insertString(this.getDocument().getLength(), str, this.attrs);
            this.setCaretPosition(this.getDocument().getLength());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
}