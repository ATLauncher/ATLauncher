package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.gui.CustomLineBorder;
import com.atlauncher.utils.Utils;

import java.awt.Cursor;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolTip;
import javax.swing.border.Border;

public class SMButton
extends JButton{
    private static final Cursor hand = new Cursor(Cursor.HAND_CURSOR);

    public SMButton(ImageIcon i, String tooltip){
        super(i);
        this.setToolTipText(tooltip);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setContentAreaFilled(false);
        this.setCursor(hand);
    }

    public SMButton(String i, String t){
        this(Utils.getIconImage(i), t);
    }

    public JToolTip createToolTip(){
        JToolTip tip = super.createToolTip();
        Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);
        tip.setBorder(border);
        return tip;
    }
}