package com.atlauncher.gui.components;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.FlowLayout;

public final class ToolsPanel extends JPanel {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 4409533883142880167L;

    public ToolsPanel() {
        super(new FlowLayout(FlowLayout.CENTER));
    }

    public ToolsPanel add(JButton button) {
        super.add(button);
        return this;
    }
}