package com.atlauncher.gui.components;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JPanel;

public final class ImagePanel
extends JPanel {
    private static final Cursor HAND = new Cursor(Cursor.HAND_CURSOR);

    private volatile Image image;

    public ImagePanel(Image image) {
        this.image = image;
        this.setCursor(HAND);
        this.setPreferredSize(new Dimension(Math.min(image.getWidth(null), 300), Math.min(image.getWidth(null), 150)));
    }

    public void setImage(Image img){
        this.image = img;
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(this.image, 0, (this.getHeight() - 150) / 2, 300, 150, null);
    }
}