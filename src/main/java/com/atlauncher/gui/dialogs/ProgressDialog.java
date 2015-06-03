/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.gui.dialogs;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Utils;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgressDialog extends JDialog {
    private static final long serialVersionUID = -4665490255300884927L;
    private String labelText; // The text to add to the JLabel
    private JProgressBar progressBar; // The Progress Bar
    private int max; // The maximum the progress bar should get to
    private Thread thread = null; // The Thread were optionally running
    private String closedLogMessage; // The message to log to the console when dialog closed
    private Object returnValue = null; // The value returned
    private int tasksToDo;
    private int tasksDone;

    public ProgressDialog(String title, int initMax, String initLabelText, String initClosedLogMessage) {
        super(App.settings.getParent(), ModalityType.APPLICATION_MODAL);
        this.labelText = initLabelText;
        this.max = initMax;
        this.closedLogMessage = initClosedLogMessage;
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setSize(300, 80);
        setTitle(title);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        progressBar = new JProgressBar();
        if (max <= 0) {
            progressBar.setIndeterminate(true);
        }
        JLabel label = new JLabel(this.labelText, SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
        if (this.closedLogMessage != null) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    LogManager.error(closedLogMessage);
                    if (thread != null) {
                        if (thread.isAlive()) {
                            thread.interrupt();
                        }
                    }
                    close(); // Close the dialog
                }
            });
        }
    }

    public void addThread(Thread thread) {
        this.thread = thread;
    }

    public void start() {
        if (this.thread != null) {
            thread.start();
        }
        setVisible(true);
    }

    public void setTotalTasksToDo(int tasksToDo) {
        this.tasksToDo = tasksToDo;
        this.tasksDone = 0;
        this.progressBar.setString("0/" + this.tasksToDo + " " + Language.INSTANCE.localize("common.tasksdone"));
        this.progressBar.setStringPainted(true);
        this.progressBar.setMaximum(this.tasksToDo);
    }

    public void doneTask() {
        this.progressBar.setString(++this.tasksDone + "/" + tasksToDo + " " + Language.INSTANCE.localize("common" + "" +
                ".tasksdone"));
        this.progressBar.setValue(this.tasksDone);
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Object getReturnValue() {
        return this.returnValue;
    }

    public void close() {
        setVisible(false); // Remove the dialog
        dispose(); // Dispose the dialog
    }
}
