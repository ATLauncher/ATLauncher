package com.atlauncher.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ModDescriptionJLabel extends JLabel implements MouseListener {

    private static final long serialVersionUID = 1L;
    private String description;

    public ModDescriptionJLabel(String description) {
        super("<html><center><font color=\"#2277AA\"><sup>[?]</sup></font></center></html>");
        this.description = description;
        super.addMouseListener(this);
    }

    public void mouseClicked(MouseEvent me) {
        Object[] options = { "Ok" };
        JOptionPane.showOptionDialog(null,
                "<html><center>" + this.description + "</center></html>", "ATLauncher",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options,
                options[0]);
    }

    public void mousePressed(MouseEvent me) {
    }

    public void mouseReleased(MouseEvent me) {
    }

    public void mouseEntered(MouseEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void mouseExited(MouseEvent e) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

}
