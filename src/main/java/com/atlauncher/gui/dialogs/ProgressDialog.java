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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.atlauncher.App;
import com.atlauncher.interfaces.NetworkProgressable;
import com.atlauncher.managers.LogManager;
import com.atlauncher.strings.Noun;
import com.atlauncher.strings.Sentence;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class ProgressDialog<T> extends JDialog implements NetworkProgressable {
    private final String labelText; // The text to add to the JLabel
    private final JProgressBar progressBar; // The Progress Bar
    private final JProgressBar subProgressBar; // The Progress Bar
    private Thread thread = null; // The Thread were optionally running
    private final String closedLogMessage; // The message to log to the console when dialog closed
    private T returnValue = null; // The value returned
    public boolean wasClosed = false; // If the dialog was closed by the user
    private final JLabel label = new JLabel();
    private int tasksToDo;
    private int tasksDone;
    private double totalBytes = 0; // Total number of bytes to download
    private double downloadedBytes = 0; // Total number of bytes downloaded

    public ProgressDialog(CharSequence title, int initMax, CharSequence initLabelText, CharSequence initClosedLogMessage,
                          boolean showProgressBar, Window parent) {
        super(parent, ModalityType.DOCUMENT_MODAL);
        this.labelText = initLabelText.toString();
        this.closedLogMessage = initClosedLogMessage.toString();
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setIconImage(Utils.getImage("/assets/image/icon.png"));
        setSize(300, 100);
        setTitle(title.toString());
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        label.setText(initLabelText.toString());
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, initMax);
        if (initMax <= 0) {
            progressBar.setIndeterminate(true);
        }
        progressBar.setVisible(showProgressBar);
        bottomPanel.add(progressBar, BorderLayout.NORTH);

        subProgressBar = new JProgressBar(0, 10000);
        subProgressBar.setVisible(false);
        bottomPanel.add(subProgressBar, BorderLayout.SOUTH);

        add(label, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                wasClosed = true;
                if (closedLogMessage != null) {
                    LogManager.error(closedLogMessage);
                }
                if (thread != null) {
                    if (thread.isAlive()) {
                        thread.interrupt();
                    }
                }
                close(); // Close the dialog
            }
        });
    }

    public ProgressDialog(CharSequence title, int initMax, CharSequence initLabelText, CharSequence initClosedLogMessage,
            boolean showProgressBar) {
        this(title, initMax, initLabelText, initClosedLogMessage, showProgressBar, App.launcher.getParent());
    }

    public ProgressDialog(CharSequence title, int initMax, CharSequence initLabelText, CharSequence initClosedLogMessage, Window parent) {
        this(title, initMax, initLabelText, initClosedLogMessage, true, parent);
    }

    public ProgressDialog(CharSequence title, int initMax, CharSequence initLabelText, CharSequence initClosedLogMessage) {
        this(title, initMax, initLabelText, initClosedLogMessage, true);
    }

    public ProgressDialog(CharSequence title, int initMax, CharSequence initLabelText, Window parent) {
        this(title, initMax, initLabelText, null, true, parent);
    }

    public ProgressDialog(CharSequence title, int initMax, CharSequence initLabelText) {
        this(title, initMax, initLabelText, null, true);
    }

    public ProgressDialog(CharSequence title) {
        this(title, 0, title, null, true);
    }

    public ProgressDialog(CharSequence title, boolean showProgressBar, Window parent) {
        this(title, 0, title, null, showProgressBar, parent);
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

    public void doneTask() {
        this.progressBar.setString(Sentence.PRT_X_OF_Y.capitalize()
            .insert(String.valueOf(++this.tasksDone))
            .insert(Noun.TASK, tasksToDo)
            .append(Noun.DONE)
            .toString());
        this.progressBar.setValue(this.tasksDone);
        this.clearDownloadedBytes();
        this.label.setText(this.labelText);
    }

    public void setReturnValue(T returnValue) {
        this.returnValue = returnValue;
    }

    public T getReturnValue() {
        return this.returnValue;
    }

    public void close() {
        setVisible(false); // Remove the dialog
        dispose(); // Dispose the dialog
    }

    public void setLabel(String text) {
        this.label.setText(text);
    }

    private void updateProgressBar() {
        double progress;
        if (this.totalBytes > 0) {
            progress = (this.downloadedBytes / this.totalBytes) * 100.0;
        } else {
            progress = 0.0;
        }
        double done = this.downloadedBytes / 1024.0 / 1024.0;
        double toDo = this.totalBytes / 1024.0 / 1024.0;
        if (done > toDo) {
            setSubProgress(100.0, String.format("%.2f MB", done));
        } else {
            setSubProgress(progress, String.format("%.2f MB / %.2f MB", done, toDo));
        }
    }

    public void setSubProgress(double percent, String label) {
        if (!subProgressBar.isVisible()) {
            subProgressBar.setVisible(true);
        }

        if (subProgressBar.isIndeterminate()) {
            subProgressBar.setIndeterminate(false);
        }

        if (percent < 0.0) {
            if (subProgressBar.isStringPainted()) {
                subProgressBar.setStringPainted(false);
            }
            subProgressBar.setVisible(false);
        } else {
            if (!subProgressBar.isStringPainted()) {
                subProgressBar.setStringPainted(true);
            }
            if (label != null) {
                subProgressBar.setString(label);
            }
        }

        if (label == null && percent > 0.0) {
            subProgressBar.setString(String.format("%.2f%%", percent));
        }

        subProgressBar.setValue((int) Math.round(percent * 100.0));
    }

    public void setIndeterminate() {
        if (subProgressBar.isStringPainted()) {
            subProgressBar.setStringPainted(false);
        }
        if (!subProgressBar.isVisible()) {
            subProgressBar.setVisible(true);
        }
        if (!subProgressBar.isIndeterminate()) {
            subProgressBar.setIndeterminate(true);
        }
    }

    @Override
    public void setTotalBytes(long bytes) {
        this.downloadedBytes = 0L;
        this.totalBytes = bytes;

        subProgressBar.setVisible(bytes > 0L);

        if (bytes > 0L) {
            this.updateProgressBar();
        }
    }

    @Override
    public void addDownloadedBytes(long bytes) {
        this.downloadedBytes += bytes;
        this.updateProgressBar();
    }

    public void clearDownloadedBytes() {
        this.downloadedBytes = 0L;
        subProgressBar.setVisible(false);
    }

    @Override
    public void addBytesToDownload(long bytes) {
        this.totalBytes += bytes;
        this.updateProgressBar();
    }
}
