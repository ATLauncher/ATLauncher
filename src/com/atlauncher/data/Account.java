/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.Utils;

public class Account implements Serializable {

    private static final long serialVersionUID = 525763616120118176L;
    private String username; // Username/Email used to login to minecraft
    private transient String password; // Users password to login to minecraft
    private String encryptedPassword; // users encrypted password
    private String minecraftUsername; // Users Minecraft Username
    private boolean remember; // Remember the users password or not

    public Account(String username, String password, String minecraftUsername, boolean remember) {
        this.username = username;
        this.password = password;
        this.encryptedPassword = Utils.encrypt(password);
        this.minecraftUsername = minecraftUsername;
        this.remember = remember;
    }

    public ImageIcon getMinecraftHead() {
        if(this.username.isEmpty()){
            return null;
        }
        File file = new File(LauncherFrame.settings.getSkinsDir(), minecraftUsername + ".png");
        if (!file.exists()) {
            new Downloader("http://s3.amazonaws.com/MinecraftSkins/" + minecraftUsername + ".png",
                    file.getAbsolutePath()).runNoReturn();
        }

        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage main = image.getSubimage(8, 8, 8, 8);
        BufferedImage helmet = image.getSubimage(40, 8, 8, 8);
        BufferedImage head = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);

        Graphics g = head.getGraphics();
        g.drawImage(main, 0, 0, null);
        g.drawImage(helmet, 0, 0, null);

        ImageIcon icon = new ImageIcon(head.getScaledInstance(32, 32, Image.SCALE_SMOOTH));

        return icon;
    }

    public String getMinecraftUsername() {
        return this.minecraftUsername;
    }
    
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject(); // Read the object in
        if (encryptedPassword.isEmpty()) {
            password = ""; // No password saved so don't set it
            remember = false; // And make sure remember is set to false
        } else {
            password = Utils.decrypt(encryptedPassword); // Encrypted password found so decrypt it
        }
    }
    
    public String toString() {
        return this.minecraftUsername;
    }

}
