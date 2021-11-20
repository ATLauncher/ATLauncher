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
package com.atlauncher.workers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import com.atlauncher.FileSystem;
import com.atlauncher.network.Download;

public class BackgroundImageWorker extends SwingWorker<ImageIcon, Object> {
    private final JLabel label;
    private final String url;

    public BackgroundImageWorker(JLabel label, String url) {
        this.label = label;
        this.url = url;
    }

    @Override
    protected ImageIcon doInBackground() throws Exception {
        Path path = FileSystem.REMOTE_IMAGE_CACHE.resolve(this.url.replaceAll("[^A-Za-z0-9]", ""));

        Download download = Download.build().setUrl(this.url).downloadTo(path);

        if (!Files.exists(path)) {
            download.downloadFile();
        }

        if (Files.exists(path)) {
            BufferedImage image = ImageIO.read(path.toFile());
            label.setIcon(new ImageIcon(image.getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
            label.setVisible(true);
        }

        return null;
    }

}
