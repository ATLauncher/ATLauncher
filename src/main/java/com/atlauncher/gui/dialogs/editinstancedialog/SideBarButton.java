package com.atlauncher.gui.dialogs.editinstancedialog;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.SwingConstants;

public class SideBarButton extends JButton {
    public SideBarButton() {
        this(null);
    }

    public SideBarButton(String title) {
        super(title);

        setHorizontalAlignment(SwingConstants.LEFT);
        setPreferredSize(new Dimension(160, 25));
        setMinimumSize(new Dimension(160, 25));
        setMaximumSize(new Dimension(160, 25));
    }
}
