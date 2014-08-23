package com.atlauncher.gui.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JPanel;

public final class ImagePanel
extends JPanel{
    private final Image image;

    public ImagePanel(Image image) {
        this.image = image;
        this.setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(this.image, 0, (this.getHeight() - 150) / 2, 300, 150, null);
    }
}