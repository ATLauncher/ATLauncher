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
package com.atlauncher.gui.dialogs.editinstancedialog;

import java.util.Arrays;

import com.atlauncher.data.Instance;
import com.atlauncher.data.Type;

public class ShaderPacksSection extends DisableableModsSection {
    public ShaderPacksSection(EditInstanceDialog parent, Instance instance) {
        super(parent, instance,
                Arrays.asList(instance.ROOT.resolve("shaderpacks")), Arrays.asList(Type.shaderpack), false);
    }
}
