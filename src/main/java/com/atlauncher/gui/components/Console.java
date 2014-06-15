package com.atlauncher.gui.components;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public final class Console extends JTextPane{
    private final SimpleAttributeSet attrs = new SimpleAttributeSet();

    public Console(){
        this.setEditable(false);
    }

    public Console setColor(Color c){
        StyleConstants.setForeground(this.attrs, c);
        return this;
    }

    public void write(String str){
        try{
            this.getDocument().insertString(this.getDocument().getLength(), str, this.attrs);
        } catch(Exception ex){
            ex.printStackTrace(System.err);
        }
    }
}