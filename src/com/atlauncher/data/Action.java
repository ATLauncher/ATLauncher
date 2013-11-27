/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

import java.io.File;
import java.util.ArrayList;

import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

public class Action {

    private ArrayList<Mod> mod;
    private String action;
    private Type type;
    private String after;
    private String saveAs;
    private boolean client;
    private boolean server;

    public Action(String action, Type type, String after, String saveAs, boolean client,
            boolean server) {
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
                    Utils.unzip(mod.getInstalledFile(instanceInstaller),
                            instanceInstaller.getTempActionsDirectory());
                }
                switch (this.type) {
                    case mods:
                        Utils.zip(instanceInstaller.getTempActionsDirectory(), new File(
                                instanceInstaller.getModsDirectory(), saveAs));
                        break;
                    case coremods:
                        if (instanceInstaller.getMinecraftVersion().usesCoreMods()) {
                            Utils.zip(instanceInstaller.getTempActionsDirectory(), new File(
                                    instanceInstaller.getCoreModsDirectory(), saveAs));
                        } else {
                            Utils.zip(instanceInstaller.getTempActionsDirectory(), new File(
                                    instanceInstaller.getModsDirectory(), saveAs));
                        }
                        break;
                    case jar:
                        Utils.zip(instanceInstaller.getTempActionsDirectory(), new File(
                                instanceInstaller.getJarModsDirectory(), saveAs));
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
