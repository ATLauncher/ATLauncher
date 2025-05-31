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
package com.atlauncher.thread;

import java.util.concurrent.Callable;

import com.atlauncher.App;
import com.atlauncher.constants.Constants;
import com.atlauncher.utils.Utils;

public final class PasteUpload implements Callable<String> {
    private final String log;

    public PasteUpload() {
        super();
        this.log = App.console.getLog().replace(System.getProperty("line.separator"), "\n");
    }

    public PasteUpload(String log) {
        super();
        this.log = log.replace(System.getProperty("line.separator"), "\n");
    }

    @Override
    public String call() {
        return Utils.uploadPaste(Constants.LAUNCHER_NAME + " - Log", log);
    }
}
