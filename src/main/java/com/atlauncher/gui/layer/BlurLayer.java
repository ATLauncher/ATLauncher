package com.atlauncher.gui.layer;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;

public final class BlurLayer
extends LayerUI<JPanel>{
    private boolean blur = false;

    public void setBlur(boolean b){
        this.firePropertyChange("blur", this.blur, b);
    }

    @Override
    public void applyPropertyChange(PropertyChangeEvent pce, JLayer l) {
        if(pce.getPropertyName().equalsIgnoreCase("blur")){
            this.blur = (Boolean) pce.getNewValue();
            l.repaint();
        }
    }

    @Override
    public void paint(Graphics g, JComponent comp){
        Graphics2D g2 = (Graphics2D) g;
        super.paint(g2, comp);

        if(this.blur){
            g2.setComposite(alpha(0.5F));
            g2.fillRect(0, 0, comp.getWidth(), comp.getHeight());
        }
    }

    private Composite alpha(float f){
        return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f);
    }
}