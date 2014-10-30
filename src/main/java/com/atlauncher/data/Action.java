/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
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
