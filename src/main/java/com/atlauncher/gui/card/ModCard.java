package com.atlauncher.gui.card;

import com.atlauncher.data.Mod;
import com.atlauncher.utils.Utils;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class ModCard extends JPanel {
    public final Mod mod;

    public ModCard(Mod mod) {
        this.mod = mod;
        if (this.mod.hasWebsite()) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (ModCard.this.mod.hasWebsite()) {
                    Utils.openBrowser(ModCard.this.mod.getWebsite());
                }
            }


        });
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);
        g2.drawString(this.mod.getName(), 10, 10);
        g2.setColor(this.mod.isOptional() ? Color.GREEN : Color.RED);
        g2.drawString(this.mod.isOptional() ? "Optional" : "Required", g2.getFontMetrics().stringWidth(this.mod
                .getName()) + g2.getFontMetrics().charWidth('M') * 2, 10);
    }
}