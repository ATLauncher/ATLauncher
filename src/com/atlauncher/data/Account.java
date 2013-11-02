/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.atlauncher.App;
import com.atlauncher.gui.ProgressDialog;
import com.atlauncher.utils.Utils;

public class Account implements Serializable {

    private static final long serialVersionUID = 525763616120118176L;
    private String username; // Username/Email used to login to minecraft
    private transient String password; // Users password to login to minecraft
    private String encryptedPassword; // users encrypted password
    private String minecraftUsername; // Users Minecraft Username
    private boolean remember; // Remember the users password or not
    private transient boolean isReal; // If this is a real user
    private ArrayList<String> collapsedPacks; // Array of packs collapsed in the Packs Tab
    private ArrayList<String> collapsedInstances; // Array of instances collapsed in the Instances
                                                  // Tab
    private boolean skinUpdating = false; // If the skin is being updated

    public Account(String username, String password, String minecraftUsername, boolean remember) {
        this.username = username;
        if (remember) {
            this.password = password;
            this.encryptedPassword = Utils.encrypt(password);
        }
        this.minecraftUsername = minecraftUsername;
        this.remember = remember;
        this.isReal = true;
        this.collapsedPacks = new ArrayList<String>();
        this.collapsedInstances = new ArrayList<String>();
    }

    public Account(String name) {
        this.username = "";
        this.minecraftUsername = name;
        this.remember = false;
        this.isReal = false;
        this.collapsedPacks = new ArrayList<String>();
        this.collapsedInstances = new ArrayList<String>();
    }

    public ImageIcon getMinecraftHead() {
        File file = null;
        if (isReal()) {
            file = new File(App.settings.getSkinsDir(), minecraftUsername + ".png");
            if (!file.exists()) {
                updateSkin();
            }
        }

        if (file == null || !file.exists()) {
            file = new File(App.settings.getSkinsDir(), "default.png");
        }

        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        BufferedImage main = image.getSubimage(8, 8, 8, 8);
        BufferedImage helmet = image.getSubimage(40, 8, 8, 8);
        BufferedImage head = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);

        int count = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (helmet.getRGB(x, y) == -1) {
                    count++;
                }
            }
        }

        Graphics g = head.getGraphics();
        g.drawImage(main, 0, 0, null);
        if (count <= 32) {
            g.drawImage(helmet, 0, 0, null);
        }

        ImageIcon icon = new ImageIcon(head.getScaledInstance(32, 32, Image.SCALE_SMOOTH));

        return icon;
    }

    public ImageIcon getMinecraftSkin() {
        File file = null;
        if (isReal()) {
            file = new File(App.settings.getSkinsDir(), minecraftUsername + ".png");
            if (!file.exists()) {
                updateSkin();
            }
        }

        if (file == null || !file.exists()) {
            file = new File(App.settings.getSkinsDir(), "default.png");
        }

        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }

        BufferedImage head = image.getSubimage(8, 8, 8, 8);
        BufferedImage helmet = image.getSubimage(40, 8, 8, 8);
        BufferedImage arm = image.getSubimage(44, 20, 4, 12);
        BufferedImage body = image.getSubimage(20, 20, 8, 12);
        BufferedImage leg = image.getSubimage(4, 20, 4, 12);
        BufferedImage skin = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);

        int count = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (helmet.getRGB(x, y) == -1) {
                    count++;
                }
            }
        }

        Graphics g = skin.getGraphics();
        g.drawImage(head, 4, 0, null);
        if (count <= 32) {
            g.drawImage(helmet, 4, 0, null);
        }
        g.drawImage(arm, 0, 8, null);
        g.drawImage(arm, 12, 8, null);
        g.drawImage(body, 4, 8, null);
        g.drawImage(leg, 4, 20, null);
        g.drawImage(leg, 8, 20, null);

        ImageIcon icon = new ImageIcon(skin.getScaledInstance(128, 256, Image.SCALE_SMOOTH));

        return icon;
    }

    public boolean isReal() {
        return this.isReal;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMinecraftUsername(String username) {
        this.minecraftUsername = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.encryptedPassword = Utils.encrypt(password);
    }

    public boolean isRemembered() {
        return this.remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
        if (!remember) {
            this.password = "";
            this.encryptedPassword = "";
        }
    }

    public String getMinecraftUsername() {
        return this.minecraftUsername;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject(); // Read the object in
        if (encryptedPassword == null) {
            password = ""; // No password saved so don't set it
            remember = false; // And make sure remember is set to false
        } else {
            password = Utils.decrypt(encryptedPassword); // Encrypted password found so decrypt it
        }
        isReal = true;
    }

    public ArrayList<String> getCollapsedPacks() {
        if (this.collapsedPacks == null) {
            this.collapsedPacks = new ArrayList<String>();
        }
        return this.collapsedPacks;
    }

    public ArrayList<String> getCollapsedInstances() {
        if (this.collapsedInstances == null) {
            this.collapsedInstances = new ArrayList<String>();
        }
        return this.collapsedInstances;
    }

    public String toString() {
        return this.minecraftUsername;
    }

    public void updateSkin() {
        if (!skinUpdating) {
            skinUpdating = true;
            final File file = new File(App.settings.getSkinsDir(), minecraftUsername + ".png");
            if (file.exists()) {
                Utils.delete(file);
            }
            App.settings.log("Downloading skin for " + getMinecraftUsername());
            final ProgressDialog dialog = new ProgressDialog(
                    App.settings.getLocalizedString("account.downloadingskin"), 0,
                    App.settings.getLocalizedString("account.downloadingminecraftskin",
                            getMinecraftUsername()), "Aborting downloading Minecraft skin for "
                            + getMinecraftUsername());
            dialog.addThread(new Thread() {
                public void run() {
                    try {
                        HttpURLConnection conn = (HttpURLConnection) new URL(
                                "http://s3.amazonaws.com/MinecraftSkins/" + minecraftUsername
                                        + ".png").openConnection();
                        if (conn.getResponseCode() == 200) {
                            Downloadable skin = new Downloadable(
                                    "http://s3.amazonaws.com/MinecraftSkins/" + minecraftUsername
                                            + ".png", file, null, null, false);
                            skin.download(false);
                        } else {
                            Utils.copyFile(new File(App.settings.getSkinsDir(), "default.png"),
                                    file, true);
                        }
                    } catch (MalformedURLException e) {
                        App.settings.logStackTrace(e);
                    } catch (IOException e) {
                        App.settings.logStackTrace(e);
                    }
                    App.settings.reloadAccounts();
                    dialog.close();
                };
            });
            dialog.start();
            skinUpdating = false;
        }
    }

}
