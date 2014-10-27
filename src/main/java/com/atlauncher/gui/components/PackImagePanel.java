package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.data.Pack;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.JPanel;

public final class PackImagePanel
extends JPanel{
    private final Image image;
    private final boolean isPrivate;

    public PackImagePanel(Pack pack) {
        this.isPrivate = pack.isPrivate();
        this.image = pack.getImage().getImage();
        this.setPreferredSize(new Dimension(Math.min(image.getWidth(null), 300), Math.min(image.getWidth(null), 150)));
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int y = (this.getHeight() - 150) / 2;
        g2.drawImage(this.image, 0, y, 300, 150, null);

        if(App.settings.enabledPackTags()){
            String text = this.isPrivate ? "Private" : "Public";

            g2.setColor(this.isPrivate ? Color.red : Color.green);
            g2.fillRect(0, y, g2.getFontMetrics().stringWidth(text) + 10, g2.getFontMetrics().getHeight() + 5);
            g2.setColor(Color.black);
            g2.drawString(text, 5, y + g2.getFontMetrics().getHeight());
        }
    }
}