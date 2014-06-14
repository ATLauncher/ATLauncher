package com.atlauncher.gui.components;

import javax.swing.*;
import java.awt.*;

public final class ToolsPanel extends JPanel {
    public ToolsPanel(){
        super(new FlowLayout(FlowLayout.CENTER));
    }

    public ToolsPanel add(JButton button){
        super.add(button);
        return this;
    }
}