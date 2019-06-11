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
package com.atlauncher.managers;

import com.atlauncher.data.Constants;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

public final class DialogManager {
    public static final int OPTION = 0;

    public static final int ERROR = 0;
    public static final int INFO = 1;
    public static final int WARNING = 2;
    public static final int QUESTION = 3;

    public int dialogType;
    public String title;
    public String content;
    public List<String> options = new LinkedList<String>();
    public int defaultOption;
    public int type;

    private DialogManager(int dialogType) {
        this.dialogType = dialogType;
    }

    public static DialogManager optionDialog() {
        return new DialogManager(DialogManager.OPTION);
    }

    public DialogManager setTitle(String title) {
        this.title = title;
        return this;
    }

    public DialogManager setContent(String content) {
        this.content = content;
        return this;
    }

    public DialogManager setType(int type) {
        this.type = type;
        return this;
    }

    public DialogManager addOption(String option, boolean isDefault) {
        this.options.add(option);

        if (isDefault) {
            this.defaultOption = this.options.size() - 1;
        }

        return this;
    }

    public DialogManager addOption(String option) {
        return this.addOption(option, false);
    }

    public int show() {
        if (this.dialogType == DialogManager.OPTION) {
            return JOptionPane.showOptionDialog(null, this.content, this.title, JOptionPane.DEFAULT_OPTION, this.type,
                    null, this.options.toArray(), this.defaultOption);
        }

        return -1;
    }
}
