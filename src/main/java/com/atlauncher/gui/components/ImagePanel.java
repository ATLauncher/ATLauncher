package com.atlauncher.gui.components;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

public final class ImagePanel extends JPanel {
    private volatile Image image;

    public ImagePanel(Image image) {
        this.image = image;
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