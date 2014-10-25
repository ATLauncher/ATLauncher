package com.atlauncher.gui.card;

import com.atlauncher.data.Mod;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public final class ModCard
extends JPanel{
    public final Mod mod;

    public ModCard(Mod mod){
        this.setPreferredSize(new Dimension(this.getPreferredSize().width, (int) (this.getPreferredSize().height * 1.5)));
        this.mod = mod;
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);
        g2.drawString(this.mod.getName(), 10, 10);
        g2.setColor(mod.isOptional() ? Color.GREEN : Color.RED);
        g2.drawString(mod.isOptional() ? "Optional" : "Required", g2.getFontMetrics().stringWidth(mod.getName()) + g2.getFontMetrics().charWidth('M') * 2, 10);
    }
}