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