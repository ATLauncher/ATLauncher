/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.atlauncher.App;
import com.atlauncher.gui.dialogs.AddServerForCheckerDialog;
import com.atlauncher.utils.Utils;

public class ServerCheckerToolPanel extends AbstractToolPanel implements ActionListener {

    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 1964636496849129267L;

    private final JLabel TITLE_LABEL = new JLabel(
            App.settings.getLocalizedString("tools.serverchecker"));

    private final JLabel INFO_LABEL = new JLabel("<html><p align=\"center\">"
            + Utils.splitMultilinedString(
                    App.settings.getLocalizedString("tools.serverchecker.info"), 60, "<br>")
            + "</p></html>");

    protected final JButton ADD_EDIT_SERVERS_BUTTON = new JButton(
            App.settings.getLocalizedString("tools.serverchecker.addeditservers"));

    public ServerCheckerToolPanel() {
        TITLE_LABEL.setFont(BOLD_FONT);
        TOP_PANEL.add(TITLE_LABEL);
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(ADD_EDIT_SERVERS_BUTTON);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        ADD_EDIT_SERVERS_BUTTON.addActionListener(this);
        LAUNCH_BUTTON.addActionListener(this);
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ADD_EDIT_SERVERS_BUTTON) {
            new AddServerForCheckerDialog();
        } else if (e.getSource() == LAUNCH_BUTTON) {
            System.out.println("Launched ServerCheckerTool");
        }
    }

}