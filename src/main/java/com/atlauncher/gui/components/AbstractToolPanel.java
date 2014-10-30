/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.utils.Utils;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;

public abstract class AbstractToolPanel extends JPanel {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = -7755529465856056647L;

    protected final Font BOLD_FONT = new Font(App.THEME.getDefaultFont().getFontName(), Font.BOLD,
            App.THEME.getDefaultFont().getSize()).deriveFont(Utils.getBaseFontSize());
    protected final JPanel TOP_PANEL = new JPanel();
    protected final JPanel MIDDLE_PANEL = new JPanel();
    protected final JPanel BOTTOM_PANEL = new JPanel();

    protected final JButton LAUNCH_BUTTON = new JButton(Language.INSTANCE.localize("tools.launch"));

    public AbstractToolPanel() {
        setLayout(new BorderLayout());
        add(TOP_PANEL, BorderLayout.NORTH);
        add(MIDDLE_PANEL, BorderLayout.CENTER);
        add(BOTTOM_PANEL, BorderLayout.SOUTH);
    }
}