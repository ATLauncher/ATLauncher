/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

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

    public DisableableMod(String name, String version, boolean optional, String file, Type type,
            Color colour, String description, boolean disabled) {
        this.name = name;
        this.version = version;
        this.optional = optional;
        this.file = file;
        this.type = type;
        this.colour = colour;
        this.description = description;
        this.disabled = disabled;
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
        if (this.colour == null) {
            return false;
        } else {
            return true;
        }
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

    public boolean enable(Instance instance) {
        if (this.disabled) {
            if (!getFile(instance).getParentFile().exists()) {
                getFile(instance).getParentFile().mkdir();
            }
            if (Utils.moveFile(getDisabledFile(instance), getFile(instance), true)) {
                if (this.type == Type.jar) {
                    File inputFile = instance.getMinecraftJar();
                    File outputTmpFile = new File(App.settings.getTempDir(), instance.getSafeName()
                            + "-minecraft.jar");
                    if (Utils.hasMetaInf(inputFile)) {
                        try {
                            JarInputStream input = new JarInputStream(
                                    new FileInputStream(inputFile));
                            JarOutputStream output = new JarOutputStream(new FileOutputStream(
                                    outputTmpFile));
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
                App.settings.log("Unsupported mod for enabling/disabling " + this.name,
                        LogMessageType.warning, false);
                break;
        }
        if (dir == null) {
            return null;
        }
        return new File(dir, file);
    }

}
