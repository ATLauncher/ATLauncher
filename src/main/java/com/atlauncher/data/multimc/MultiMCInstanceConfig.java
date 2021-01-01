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
package com.atlauncher.data.multimc;

import java.util.Properties;

public class MultiMCInstanceConfig {
    public String name;

    public Integer initialMemory;
    public Integer maximumMemory;
    public Integer permGen;
    public String javaPath;
    public String javaArguments;

    public MultiMCInstanceConfig(Properties props) {
        name = props.getProperty("name");

        if (props.getProperty("MinMemAlloc") != null) {
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
    }
}
