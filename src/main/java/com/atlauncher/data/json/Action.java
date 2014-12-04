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
package com.atlauncher.data.json;

import com.atlauncher.annot.Json;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import java.io.File;
import java.util.List;

@Json
public class Action {
    private List<String> mod;
    private List<Mod> mods;
    private TheAction action;
    private ActionType type;
    private ActionAfter after;
    private String saveAs;
    private boolean client;
    private boolean server;

    public TheAction getAction() {
        return this.action;
    }

    public ActionType getType() {
        return this.type;
    }

    public ActionAfter getAfter() {
        return this.after;
    }

    public String getSaveAs() {
        return this.saveAs;
    }

    public boolean isForClient() {
        return this.client;
    }

    public boolean isForServer() {
        return this.server;
    }

    public void convertMods(InstanceInstaller instanceInstaller) {
        Mod toAdd = null;
        for (String name : this.mod) {
            toAdd = instanceInstaller.getJsonModByName(name);
            if (toAdd != null) {
                addMod(toAdd);
            }
        }
    }

    public void addMod(Mod mod) {
        if (!this.mods.contains(mod)) {
            this.mods.add(mod);
        }
    }

    public void execute(InstanceInstaller instanceInstaller) {
        if ((instanceInstaller.isServer() && !server) || (!instanceInstaller.isServer() && !client)) {
            return;
        }
        convertMods(instanceInstaller);
        Utils.deleteContents(instanceInstaller.getTempActionsDirectory());
        instanceInstaller.fireTask("Executing Action");
        instanceInstaller.fireSubProgressUnknown();
        if (this.action == TheAction.createZip) {
            if (mod.size() >= 2) {
                for (Mod mod : this.mods) {
                    Utils.unzip(mod.getInstalledFile(instanceInstaller), instanceInstaller.getTempActionsDirectory());
                }
                switch (this.type) {
                    case mods:
                        Utils.zip(instanceInstaller.getTempActionsDirectory(), new File(instanceInstaller
                                .getModsDirectory(), saveAs));
                        break;
                    case coremods:
                        if (instanceInstaller.getVersion().getMinecraftVersion().usesCoreMods()) {
                            Utils.zip(instanceInstaller.getTempActionsDirectory(), new File(instanceInstaller
                                    .getCoreModsDirectory(), saveAs));
                        } else {
                            Utils.zip(instanceInstaller.getTempActionsDirectory(), new File(instanceInstaller
                                    .getModsDirectory(), saveAs));
                        }
                        break;
                    case jar:
                        Utils.zip(instanceInstaller.getTempActionsDirectory(), new File(instanceInstaller
                                .getJarModsDirectory(), saveAs));
                        instanceInstaller.addToJarOrder(this.saveAs);
                        break;
                    default:
                        break;
                }
            }
        } else if (this.action == TheAction.rename) {
            if (mods.size() == 1) {
                File from = mods.get(0).getInstalledFile(instanceInstaller);
                File to = new File(from.getParentFile(), saveAs);
                Utils.moveFile(from, to, true);
            }
        }
        if (this.after == ActionAfter.delete) {
            for (Mod mod : this.mods) {
                Utils.delete(mod.getInstalledFile(instanceInstaller));
            }
        }
    }
}