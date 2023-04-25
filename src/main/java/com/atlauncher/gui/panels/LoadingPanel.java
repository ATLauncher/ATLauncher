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
package com.atlauncher.gui.panels;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.interfaces.NetworkProgressable;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class LoadingPanel extends JPanel implements NetworkProgressable {
    private JProgressBar progressBar = new JProgressBar(0, 10000);
    private double totalBytes = 0; // Total number of bytes to download
    private double downloadedBytes = 0; // Total number of bytes downloaded
    private JLabel label = new JLabel();

    public LoadingPanel() {
        this(GetText.tr("Loading..."));
    }

    public LoadingPanel(String text) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        ImageIcon imageIcon = Utils.getIconImage("/assets/image/loading-bars.gif");

        JLabel iconLabel = new JLabel();
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setIcon(imageIcon);
        imageIcon.setImageObserver(iconLabel);

        label.setText(text);
        label.setFont(App.THEME.getBoldFont().deriveFont(18f));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressBar.setMaximumSize(new Dimension(200, 22));
        progressBar.setVisible(false);
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        add(Box.createVerticalGlue());
        add(iconLabel);
        add(label);
        add(progressBar);
        add(Box.createVerticalGlue());
    }

    public void setText(String text) {
        label.setText(text);
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
            setProgress(100.0, String.format("%.2f MB", done));
        } else {
            setProgress(progress, String.format("%.2f MB / %.2f MB", done, toDo));
        }
    }

    public void setProgress(double percent, String label) {
        if (!progressBar.isVisible()) {
            progressBar.setVisible(true);
        }

        if (progressBar.isIndeterminate()) {
            progressBar.setIndeterminate(false);
        }

        if (percent < 0.0) {
            if (progressBar.isStringPainted()) {
                progressBar.setStringPainted(false);
            }
            progressBar.setVisible(false);
        } else {
            if (!progressBar.isStringPainted()) {
                progressBar.setStringPainted(true);
            }
            if (label != null) {
                progressBar.setString(label);
            }
        }

        if (label == null && percent > 0.0) {
            progressBar.setString(String.format("%.2f%%", percent));
        }

        progressBar.setValue((int) Math.round(percent * 100.0));
    }

    @Override
    public void setTotalBytes(long bytes) {
        this.downloadedBytes = 0L;
        this.totalBytes = bytes;

        progressBar.setVisible(bytes > 0L);

        if (bytes > 0L) {
            this.updateProgressBar();
        }
    }

    @Override
    public void addDownloadedBytes(long bytes) {
        this.downloadedBytes += bytes;

        SwingUtilities.invokeLater(() -> {
            this.updateProgressBar();
        });
    }

    @Override
    public void addBytesToDownload(long bytes) {
        this.totalBytes += bytes;

        SwingUtilities.invokeLater(() -> {
            this.updateProgressBar();
        });
    }
}
