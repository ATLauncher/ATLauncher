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
package com.atlauncher.utils;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.atlauncher.managers.LogManager;

public class SkinUtils {
    public static ImageIcon getDefaultHead() {
        return getHead(Utils.getImage("/assets/image/skins/default.png"));
    }

    public static ImageIcon getHead(BufferedImage image) {
        BufferedImage main = image.getSubimage(8, 8, 8, 8);
        BufferedImage helmet = image.getSubimage(40, 8, 8, 8);
        BufferedImage head = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);

        Graphics g = head.getGraphics();
        g.drawImage(main, 0, 0, null);
        if (Utils.nonTransparentPixels(helmet) <= 32) {
            g.drawImage(helmet, 0, 0, null);
        }

        return new ImageIcon(head.getScaledInstance(20, 20, Image.SCALE_SMOOTH));
    }

    public static ImageIcon getHead(File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        if (image == null) {
            return getHead(Utils.getImage("/assets/image/skins/default.png"));
        }

        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        return getHead(image);
    }

    public static ImageIcon getDefaultSkin() {
        return getSkin(Utils.getImage("/assets/image/skins/default.png"));
    }

    public static ImageIcon getSkin(BufferedImage image) {
        // new skins are 64x64 and old ones are 64x32
        boolean isNewImage = image.getWidth() == 64 && image.getHeight() == 64;

        BufferedImage head = image.getSubimage(8, 8, 8, 8);
        BufferedImage helmet = image.getSubimage(40, 8, 8, 8);

        BufferedImage leftArm = image.getSubimage(44, 20, 4, 12);
        BufferedImage rightArm;

        if (!isNewImage || Utils.nonTransparentPixels(image.getSubimage(36, 52, 4, 12)) == 48) {
            rightArm = Utils.flipImage(leftArm);
        } else {
            rightArm = image.getSubimage(36, 52, 4, 12);
        }

        BufferedImage body = image.getSubimage(20, 20, 8, 12);

        BufferedImage leftLeg = image.getSubimage(4, 20, 4, 12);
        BufferedImage rightLeg;

        if (!isNewImage || Utils.nonTransparentPixels(image.getSubimage(20, 52, 4, 12)) == 48) {
            rightLeg = Utils.flipImage(leftLeg);
        } else {
            rightLeg = image.getSubimage(20, 52, 4, 12);
        }

        BufferedImage skin = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);

        Graphics g = skin.getGraphics();
        g.drawImage(head, 4, 0, null);

        // Draw the helmet on the skin if more than half of the pixels are not
        // transparent.
        if (Utils.nonTransparentPixels(helmet) <= 32) {
            g.drawImage(helmet, 4, 0, null);
        }

        g.drawImage(leftArm, 0, 8, null);
        g.drawImage(rightArm, 12, 8, null);

        g.drawImage(body, 4, 8, null);

        g.drawImage(leftLeg, 4, 20, null);
        g.drawImage(rightLeg, 8, 20, null);

        return new ImageIcon(skin.getScaledInstance(128, 256, Image.SCALE_SMOOTH));
    }

    public static ImageIcon getSkin(File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        if (image == null) {
            return getSkin(Utils.getImage("/assets/image/skins/default.png"));
        }

        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        return getSkin(image);
    }
}
