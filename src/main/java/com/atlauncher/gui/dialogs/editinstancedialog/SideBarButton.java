/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
