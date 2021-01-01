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
package com.atlauncher.managers;

import java.awt.Window;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.atlauncher.App;

import org.mini2Dx.gettext.GetText;

public final class DialogManager {
    public static final int OPTION_TYPE = 0;
    public static final int CONFIRM_TYPE = 1;
    public static final int OK_TYPE = 1;

    public static final int ERROR = JOptionPane.ERROR_MESSAGE;
    public static final int INFO = JOptionPane.INFORMATION_MESSAGE;
    public static final int WARNING = JOptionPane.WARNING_MESSAGE;
    public static final int QUESTION = JOptionPane.QUESTION_MESSAGE;

    public static final int DEFAULT_OPTION = JOptionPane.DEFAULT_OPTION;
    public static final int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
    public static final int YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
    public static final int OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;

    public static final int YES_OPTION = JOptionPane.YES_OPTION;
    public static final int NO_OPTION = JOptionPane.NO_OPTION;
    public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
    public static final int OK_OPTION = JOptionPane.OK_OPTION;
    public static final int CLOSED_OPTION = JOptionPane.CLOSED_OPTION;

    public int dialogType;
    public Window parent;
    public String title;
    public Object content;
    public List<String> options = new LinkedList<>();
    public Icon icon = null;
    public int lookAndFeel = DialogManager.DEFAULT_OPTION;
    public Integer defaultOption = null;
    public int type = DialogManager.QUESTION;

    private DialogManager(int dialogType) {
        this.dialogType = dialogType;
    }

    public static DialogManager optionDialog() {
        return new DialogManager(DialogManager.OPTION_TYPE);
    }

    public static DialogManager confirmDialog() {
        return new DialogManager(DialogManager.CONFIRM_TYPE);
    }

    public static DialogManager okDialog() {
        DialogManager dialog = new DialogManager(DialogManager.CONFIRM_TYPE);

        dialog.addOption(GetText.tr("Ok"), true);

        return dialog;
    }

    public static DialogManager okCancelDialog() {
        DialogManager dialog = new DialogManager(DialogManager.CONFIRM_TYPE);

        dialog.addOption(GetText.tr("Ok"), true);
        dialog.addOption(GetText.tr("Cancel"));

        return dialog;
    }

    public static DialogManager yesNoDialog() {
        DialogManager dialog = new DialogManager(DialogManager.CONFIRM_TYPE);

        dialog.addOption(GetText.tr("Yes"), true);
        dialog.addOption(GetText.tr("No"));

        return dialog;
    }

    public DialogManager setParent(Window parent) {
        this.parent = parent;
        return this;
    }

    public DialogManager setLookAndFeel(int lookAndFeel) {
        this.lookAndFeel = lookAndFeel;
        return this;
    }

    public DialogManager setTitle(String title) {
        this.title = title;
        return this;
    }

    public DialogManager setContent(Object content) {
        this.content = content;
        return this;
    }

    public DialogManager setType(int type) {
        this.type = type;
        return this;
    }

    public DialogManager setDefaultOption(int defaultOption) {
        this.defaultOption = defaultOption;
        return this;
    }

    public DialogManager setIcon(Icon icon) {
        this.icon = icon;
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

    public Object[] getOptions() {
        if (this.options.size() == 0) {
            return null;
        }

        return this.options.toArray();
    }

    public Window getParent() {
        if (this.parent != null) {
            return this.parent;
        }

        if (App.settings != null && App.launcher != null && App.launcher.getParent() != null) {
            return App.launcher.getParent();
        }

        return null;
    }

    public int show() {
        try {
            return JOptionPane.showOptionDialog(this.getParent(), this.content, this.title, this.lookAndFeel, this.type,
                    this.icon, this.getOptions(), this.defaultOption);
        } catch (Exception e) {
            LogManager.logStackTrace(e, false);
        }

        return -1;
    }
}
