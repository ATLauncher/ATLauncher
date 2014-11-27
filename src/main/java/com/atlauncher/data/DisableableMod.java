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

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.utils.Utils;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class DisableableMod implements Serializable {
    private static final long serialVersionUID = 8429405767313518704L;
    private String name;
    private String version;
    private boolean optional;
    private String file;
    private Type type;
    private Color colour;
    private String description;
    private boolean disabled;
    private boolean userAdded = false; // Default to not being user added

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
                          String description, boolean disabled, boolean userAdded) {
        this.name = name;
        this.version = version;
        this.optional = optional;
        this.file = file;
        this.type = type;
        this.colour = colour;
        this.description = description;
        this.disabled = disabled;
        this.userAdded = userAdded;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public boolean hasColour() {
        return this.colour != null;
    }

    public Color getColour() {
        return this.colour;
    }

    public String getDescription() {
        if (this.description == null) {
            return "";
        }
        return this.description;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean isUserAdded() {
        return this.userAdded;
    }

    public String getFilename() {
        return this.file;
    }

    public boolean enable(Instance instance) {
        if (this.disabled) {
            if (!getFile(instance).getParentFile().exists()) {
                getFile(instance).getParentFile().mkdir();
            }
            if (Utils.moveFile(getDisabledFile(instance), getFile(instance), true)) {
                if (this.type == Type.jar) {
                    File inputFile = instance.getMinecraftJar();
                    File outputTmpFile = new File(App.settings.getTempDir(), instance.getSafeName() + "-minecraft.jar");
                    if (Utils.hasMetaInf(inputFile)) {
                        try {
                            JarInputStream input = new JarInputStream(new FileInputStream(inputFile));
                            JarOutputStream output = new JarOutputStream(new FileOutputStream(outputTmpFile));
                            JarEntry entry;

                            while ((entry = input.getNextJarEntry()) != null) {
                                if (entry.getName().contains("META-INF")) {
                                    continue;
                                }
                                output.putNextEntry(entry);
                                byte buffer[] = new byte[1024];
                                int amo;
                                while ((amo = input.read(buffer, 0, 1024)) != -1) {
                                    output.write(buffer, 0, amo);
                                }
                                output.closeEntry();
                            }

                            input.close();
                            output.close();

                            inputFile.delete();
                            outputTmpFile.renameTo(inputFile);
                        } catch (IOException e) {
                            App.settings.logStackTrace(e);
                        }
                    }
                }
                this.disabled = false;
                return true;
            }
        }
        return false;
    }

    public boolean disable(Instance instance) {
        if (!this.disabled) {
            if (Utils.moveFile(getFile(instance), instance.getDisabledModsDirectory(), false)) {
                this.disabled = true;
                return true;
            }
        }
        return false;
    }

    public File getDisabledFile(Instance instance) {
        return new File(instance.getDisabledModsDirectory(), this.file);
    }

    public File getFile(Instance instance) {
        File dir = null;
        switch (type) {
            case jar:
            case forge:
            case mcpc:
                dir = instance.getJarModsDirectory();
                break;
            case texturepack:
                dir = instance.getTexturePacksDirectory();
                break;
            case resourcepack:
                dir = instance.getResourcePacksDirectory();
                break;
            case mods:
                dir = instance.getModsDirectory();
                break;
            case ic2lib:
                dir = instance.getIC2LibDirectory();
                break;
            case denlib:
                dir = instance.getDenLibDirectory();
                break;
            case coremods:
                dir = instance.getCoreModsDirectory();
                break;
            case shaderpack:
                dir = instance.getShaderPacksDirectory();
                break;
            default:
                LogManager.warn("Unsupported mod for enabling/disabling " + this.name);
                break;
        }
        if (dir == null) {
            return null;
        }
        return new File(dir, file);
    }

    public Type getType() {
        return this.type;
    }

}
