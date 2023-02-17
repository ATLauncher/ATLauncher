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
package com.atlauncher.data.multimc;

import java.util.Properties;

import com.atlauncher.managers.ConfigManager;

public class MultiMCInstanceConfig {
    public String name;

    public Integer initialMemory;
    public Integer maximumMemory;
    public Integer permGen;
    public String javaPath;
    public String javaArguments;
    public String preLaunchCommand;
    public String postExitCommand;
    public String wrapperCommand;

    public MultiMCInstanceConfig(Properties props) {
        name = props.getProperty("name");

        if (name == null) {
            name = "MultiMC Import";
        }

        if (props.getProperty("MinMemAlloc") != null
                && ConfigManager.getConfigItem("removeInitialMemoryOption", false) == false) {
            initialMemory = Integer.parseInt(props.getProperty("MinMemAlloc"));
        }

        if (props.getProperty("MaxMemAlloc") != null) {
            maximumMemory = Integer.parseInt(props.getProperty("MaxMemAlloc"));
        }

        if (props.getProperty("PermGen") != null) {
            permGen = Integer.parseInt(props.getProperty("PermGen"));
        }

        if (props.getProperty("JavaPath") != null) {
            javaPath = props.getProperty("JavaPath");
        }

        if (props.getProperty("JvmArgs") != null) {
            javaArguments = props.getProperty("JvmArgs");
        }

        if (props.getProperty("PreLaunchCommand") != null) {
            preLaunchCommand = props.getProperty("PreLaunchCommand");
        }

        if (props.getProperty("PostExitCommand") != null) {
            postExitCommand = props.getProperty("PostExitCommand");
        }
        if (props.getProperty("WrapperCommand") != null) {
            wrapperCommand = props.getProperty("WrapperCommand");
        }
    }
}
