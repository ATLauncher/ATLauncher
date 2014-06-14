/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.components;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

public class NetworkCheckerToolPanel extends AbstractToolPanel {

    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 4811953376698111667L;

    private final JLabel TITLE_LABEL = new JLabel(
            App.settings.getLocalizedString("tools.networkchecker"));

    private final JLabel INFO_LABEL = new JLabel("<html><p align=\"center\">"
            + Utils.splitMultilinedString(
                    App.settings.getLocalizedString("tools.networkchecker.info"), 60, "<br>")
            + "</p></html>");

    public NetworkCheckerToolPanel() {
        TITLE_LABEL.setFont(BOLD_FONT);
        TOP_PANEL.add(TITLE_LABEL);
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    }

}