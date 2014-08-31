/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs.settings;

import com.atlauncher.App;
import com.atlauncher.gui.CustomLineBorder;
import com.atlauncher.gui.tabs.Tab;
import com.atlauncher.utils.Utils;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

@SuppressWarnings("serial")
public abstract class AbstractSettingsTab extends JPanel implements Tab {
    final Insets LABEL_INSETS = new Insets(5, 0, 5, 10);
    final Insets FIELD_INSETS = new Insets(5, 0, 5, 0);
    final Insets LABEL_INSETS_SMALL = new Insets(0, 0, 0, 10);
    final Insets FIELD_INSETS_SMALL = new Insets(0, 0, 0, 0);

    final ImageIcon HELP_ICON = Utils.getIconImage("/assets/image/Help.png");
    final ImageIcon ERROR_ICON = Utils.getIconImage("/assets/image/Error.png");
    final ImageIcon WARNING_ICON = Utils.getIconImage("/assets/image/Warning.png");

    final Border RESTART_BORDER = BorderFactory.createEmptyBorder(0, 0, 0, 5);
    final Border HOVER_BORDER = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);

    final GridBagConstraints gbc;

    public AbstractSettingsTab() {
        setLayout(new GridBagLayout());
        this.gbc = new GridBagConstraints();
    }
}
