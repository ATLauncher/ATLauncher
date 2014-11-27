/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.data;

import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import java.io.File;
import java.util.ArrayList;

public class Action {
    private ArrayList<Mod> mod;
    private String action;
    private Type type;
    private String after;
    private String saveAs;
    private boolean client;
    private boolean server;

    public Action(String action, Type type, String after, String saveAs, boolean client, boolean server) {
        this.mod = new ArrayList<Mod>();
        this.action = action;
        this.type = type;
        this.after = after;
        this.saveAs = saveAs;
        this.client = client;
        this.server = server;
    }

    public Action(String action, String after, String saveAs, boolean client, boolean server) {
        this.mod = new ArrayList<Mod>();
        this.action = action;
        this.after = after;
        this.saveAs = saveAs;
        this.client = client;
        this.server = server;
    }

    public void addMod(Mod mod) {
        if (!this.mod.contains(mod)) {
            this.mod.add(mod);
        }
    }

    public void execute(InstanceInstaller instanceInstaller) {
        if ((instanceInstaller.isServer() && !server) || (!instanceInstaller.isServer() && !client)) {
            return;
        }
        Utils.deleteContents(instanceInstaller.getTempActionsDirectory());
        instanceInstaller.fireTask("Executing Action");
        instanceInstaller.fireSubProgressUnknown();
        if (this.action.equalsIgnoreCase("createzip")) {
            if (mod.size() >= 2) {
                for (Mod mod : this.mod) {
                    Utils.unzip(mod.getInstalledFile(instanceInstaller), instanceInstaller.getTempActionsDirectory());
                }
                switch (this.type) {
                    case mods:
                        Utils.zip(instanceInstaller.getTempActionsDirectory(),
                                new File(instanceInstaller.getModsDirectory(), saveAs));
                        break;
                    case coremods:
                        if (instanceInstaller.getVersion().getMinecraftVersion().usesCoreMods()) {
                            Utils.zip(instanceInstaller.getTempActionsDirectory(),
                                    new File(instanceInstaller.getCoreModsDirectory(), saveAs));
                        } else {
                            Utils.zip(instanceInstaller.getTempActionsDirectory(),
                                    new File(instanceInstaller.getModsDirectory(), saveAs));
                        }
                        break;
                    case jar:
                        Utils.zip(instanceInstaller.getTempActionsDirectory(),
                                new File(instanceInstaller.getJarModsDirectory(), saveAs));
                        instanceInstaller.addToJarOrder(this.saveAs);
                        break;
                    default:
                        break;
                }
            }
        } else if (this.action.equalsIgnoreCase("rename")) {
            if (mod.size() == 1) {
                File from = mod.get(0).getInstalledFile(instanceInstaller);
                File to = new File(from.getParentFile(), saveAs);
                Utils.moveFile(from, to, true);
            }
        }
        if (this.after.equalsIgnoreCase("delete")) {
            for (Mod mod : this.mod) {
                Utils.delete(mod.getInstalledFile(instanceInstaller));
            }
        }
    }
}
