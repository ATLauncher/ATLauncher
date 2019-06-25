/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.workers.InstanceInstaller;

@Json
public class Loader {
    private String type;
    private boolean choose = false;
    private Map<String, Object> metadata;
    private String className;
    private String chooseClassName;
    private String chooseMethod;

    public String getType() {
        return this.type;
    }

    public boolean canChoose() {
        return this.choose;
    }

    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    public String getClassName() {
        return this.className;
    }

    public String getChooseClassName() {
        return this.chooseClassName;
    }

    public String getChooseMethod() {
        return this.chooseMethod;
    }

    public com.atlauncher.data.loaders.Loader getLoader(File tempDir, InstanceInstaller instanceInstaller,
            com.atlauncher.data.loaders.LoaderVersion loaderVersion)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        com.atlauncher.data.loaders.Loader instance = (com.atlauncher.data.loaders.Loader) Class.forName(this.className)
                .newInstance();

        instance.set(this.metadata, tempDir, instanceInstaller, loaderVersion);

        return instance;
    }

    public com.atlauncher.data.minecraft.loaders.Loader getNewLoader(File tempDir, InstanceInstaller instanceInstaller,
            LoaderVersion loaderVersion) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        com.atlauncher.data.minecraft.loaders.Loader instance = (com.atlauncher.data.minecraft.loaders.Loader) Class
                .forName(this.className).newInstance();

        instance.set(this.metadata, tempDir, instanceInstaller, loaderVersion);

        return instance;
    }

    public List<com.atlauncher.data.loaders.LoaderVersion> getChoosableVersions(String minecraft) {
        try {
            Method method = Class.forName(this.chooseClassName).getDeclaredMethod(this.chooseMethod, String.class);

            return (List<com.atlauncher.data.loaders.LoaderVersion>) method.invoke(null, minecraft);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public List<LoaderVersion> getNewChoosableVersions(String minecraft) {
        try {
            Method method = Class.forName(this.chooseClassName).getDeclaredMethod(this.chooseMethod, String.class);

            return (List<LoaderVersion>) method.invoke(null, minecraft);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }
}
