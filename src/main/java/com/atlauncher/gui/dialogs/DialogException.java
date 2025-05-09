package com.atlauncher.gui.dialogs;

import javax.swing.SwingUtilities;

import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.OS;

public class DialogException extends Exception {
    private DialogManager dialog;
    private String link;

    public DialogException(DialogManager dialog, String message) {
        this(dialog, message, null);
    }

    public DialogException(DialogManager dialog, String message, String link) {
        super(message);

        this.dialog = dialog;
        this.link = link;
    }

    public void showDialog() {
        SwingUtilities.invokeLater(() -> {
            dialog.show();

            if (link != null) {
                OS.openWebBrowser(link);
            }
        });
    }
}
