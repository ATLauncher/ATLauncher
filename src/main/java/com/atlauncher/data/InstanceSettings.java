/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

/**
 * @deprecated
 */
public class InstanceSettings {
    public Integer initialMemory = null;

    public Integer maximumMemory = null;

    public Integer permGen = null;

    public String javaPath = null;

    public String javaArguments = null;

    /**
     * @return the initialMemory
     */
    public Integer getInitialMemory() {
        return this.initialMemory;
    }

    /**
     * @param initialMemory the initialMemory to set
     */
    public void setInitialMemory(Integer initialMemory) {
        this.initialMemory = initialMemory;
    }

    /**
     * @return the maximumMemory
     */
    public Integer getMaximumMemory() {
        return this.maximumMemory;
    }

    /**
     * @param maximumMemory the maximumMemory to set
     */
    public void setMaximumMemory(Integer maximumMemory) {
        this.maximumMemory = maximumMemory;
    }

    /**
     * @return the permGen
     */
    public Integer getPermGen() {
        return this.permGen;
    }

    /**
     * @param permGen the permGen to set
     */
    public void setPermGen(Integer permGen) {
        this.permGen = permGen;
    }

    /**
     * @return the javaPath
     */
    public String getJavaPath() {
        return this.javaPath;
    }

    /**
     * @param javaPath the javaPath to set
     */
    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    /**
     * @return the javaArguments
     */
    public String getJavaArguments() {
        return this.javaArguments;
    }

    /**
     * @param javaArguments the javaArguments to set
     */
    public void setJavaArguments(String javaArguments) {
        this.javaArguments = javaArguments;
    }
}
