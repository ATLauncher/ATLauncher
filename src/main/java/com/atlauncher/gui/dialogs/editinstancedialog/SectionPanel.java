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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.Instance;
import com.atlauncher.data.ModManagement;
import com.atlauncher.evnt.listener.MinecraftLaunchListener;
import com.atlauncher.evnt.manager.MinecraftLaunchManager;

public abstract class SectionPanel extends JPanel implements MinecraftLaunchListener {
    protected EditDialog parent;
    protected ModManagement instanceOrServer;
    protected boolean isLaunchingOrLaunched = false;

    public SectionPanel(EditDialog parent, ModManagement instanceOrServer) {
        super();

        this.instanceOrServer = instanceOrServer;
        this.parent = parent;

        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout());

        MinecraftLaunchManager.addListener(this);
    }

    public abstract void updateUIState();

    @Override
    public void minecraftLaunching(Instance instance) {
        if (instance == this.instanceOrServer) {
            isLaunchingOrLaunched = true;
            updateUIState();
        }
    }

    @Override
    public void minecraftLaunchFailed(Instance instance, String reason) {
        if (instance == this.instanceOrServer) {
            isLaunchingOrLaunched = false;
            updateUIState();
        }
    }

    @Override
    public void minecraftLaunched(Instance instance, AbstractAccount account, Process process) {
        if (instance == this.instanceOrServer) {
            isLaunchingOrLaunched = true;
            updateUIState();
        }
    }

    @Override
    public void minecraftClosed(Instance instance) {
        if (instance == this.instanceOrServer) {
            isLaunchingOrLaunched = false;
            updateUIState();
        }
    }
}
