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
package com.atlauncher.data;

import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.HTMLUtils;

import org.mini2Dx.gettext.GetText;

public class MinecraftError {
    static final int OUT_OF_MEMORY = 1;
    static final int CONCURRENT_MODIFICATION_ERROR_1_6 = 2;

    static void showInformationPopup(int error) {
        switch (error) {
        case MinecraftError.OUT_OF_MEMORY:
            MinecraftError.showOutOfMemoryPopup();
        case MinecraftError.CONCURRENT_MODIFICATION_ERROR_1_6:
            MinecraftError.showConcurrentModificationError16();
        }
    }

    static void showOutOfMemoryPopup() {
        DialogManager.okDialog().setTitle(GetText.tr("About Your Crash"))
                .setContent(HTMLUtils.centerParagraph(
                        GetText.tr("Minecraft has crashed due to insufficent memory being allocated.<br/><br/>Please go to the settings tab and increase the maximum memory option and then try launching the instance again.")))
                .setType(DialogManager.INFO).show();
    }

    static void showConcurrentModificationError16() {
        DialogManager.okDialog().setTitle(GetText.tr("About Your Crash"))
                .setContent(HTMLUtils.centerParagraph(GetText.tr("Minecraft has crashed due to an incompatability with Forge and your version of Java.<br/><br/>Please reinstall the instance to automatically fix the problem, and then try launching the instance again.")))
                .setType(DialogManager.INFO).show();
    }
}
