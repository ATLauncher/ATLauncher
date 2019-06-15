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
        DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.aboutyourcrash"))
                .setContent(HTMLUtils.centerParagraph(
                        Language.INSTANCE.localizeWithReplace("instancecrash.outofmemory", "<br/><br/>")))
                .setType(DialogManager.INFO).show();
    }

    static void showConcurrentModificationError16() {
        DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.aboutyourcrash"))
                .setContent(HTMLUtils.centerParagraph(Language.INSTANCE
                        .localizeWithReplace("instancecrash.concurrentmodificationerror16", "<br/><br/>")))
                .setType(DialogManager.INFO).show();
    }
}
