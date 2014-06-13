/**
 * Copyright 2013-2014 by ATLauncher and Contributors
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
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.atlauncher.App;
import com.atlauncher.gui.ProgressDialog;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.utils.Utils;

/**
 * This class deals with the Accounts in the launcher.
 */
public class Account implements Serializable {

    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 525763616120118176L;

    /**
     * The username/email used to login to Mojang servers.
     */
    private String username;

    /**
     * The account's password to login to Mojang servers.
     */
    private transient String password;

    /**
     * The encrypted password.
     */
    private String encryptedPassword;

    /**
     * The account's Minecraft username.
     */
    private String minecraftUsername;

    /**
     * If this account should remember the password or not.
     */
    private boolean remember;

    /**
     * If this account is a real user or not.
     */
    private transient boolean isReal;

    /**
     * The pack names this account has collapsed in the {@link PacksTab}, if any.
     */
    private List<String> collapsedPacks;

    /**
     * The instance names this account has collapsed in the {@link InstancesTab}, if any.
     */
    private List<String> collapsedInstances;

    /**
     * If the skin is currently being updated.
     */
    private boolean skinUpdating = false;

    /**
     * Constructor for a real user Account.
     * 
     * @param username
     *            The name of the Account
     * @param password
     *            The password of the Account
     * @param minecraftUsername
     *            The Minecraft username of the Account
     * @param remember
     *            If this Account's password should be remembered or not
     */
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

    /**
     * Constructor for a fake user account, used for displaying non selectable accounts.
     * 
     * @param name
     *            The name of the Account
     */
    public Account(String name) {
        this.username = "";
        this.minecraftUsername = name;
        this.remember = false;
        this.isReal = false;
        this.collapsedPacks = new ArrayList<String>();
        this.collapsedInstances = new ArrayList<String>();
    }

    /**
     * Creates an {@link ImageIcon} of the Account's Minecraft skin, getting just the head of it.
     * 
     * @return The Account's Minecraft usernames head
     */
    public ImageIcon getMinecraftHead() {
        File file = null;
        if (this.isReal()) {
            file = new File(App.settings.getSkinsDir(), this.minecraftUsername + ".png");
            if (!file.exists()) {
                this.updateSkin(); // Download/update the users skin
            }
        }

        // If the file doesn't exist then use the default Minecraft skin.
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

        Graphics g = head.getGraphics();
        g.drawImage(main, 0, 0, null);
        if (Utils.nonTransparentPixels(helmet) <= 32) {
            g.drawImage(helmet, 0, 0, null);
        }

        return new ImageIcon(head.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
    }

    /**
     * Creates an {@link ImageIcon} of the Account's Minecraft skin.
     * 
     * @return The Account's Minecraft usernames skin
     */
    public ImageIcon getMinecraftSkin() {
        File file = null;
        if (this.isReal()) {
            file = new File(App.settings.getSkinsDir(), this.minecraftUsername + ".png");
            if (!file.exists()) {
                this.updateSkin(); // Download/update the users skin
            }
        }

        // If the file doesn't exist then use the default Minecraft skin.
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

        Graphics g = skin.getGraphics();
        g.drawImage(head, 4, 0, null);

        // Draw the helmet on the skin if more than half of the pixels are not transparent.
        if (Utils.nonTransparentPixels(helmet) <= 32) {
            g.drawImage(helmet, 4, 0, null);
        }

        g.drawImage(arm, 0, 8, null);
        g.drawImage(Utils.flipImage(arm), 12, 8, null);
        g.drawImage(body, 4, 8, null);
        g.drawImage(leg, 4, 20, null);
        g.drawImage(Utils.flipImage(leg), 8, 20, null);

        return new ImageIcon(skin.getScaledInstance(128, 256, Image.SCALE_SMOOTH));
    }

    /**
     * If this Account is real or not.
     * 
     * @return true if the Account is real and was added by the user, false otherwise
     */
    public boolean isReal() {
        return this.isReal;
    }

    /**
     * Gets the username used for logging into Mojang servers. Can be an email address or a username
     * if the user has not migrated their Minecraft account to a Mojang account.
     * 
     * @return The username used for logging into Mojang servers
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username used for this Account to login to Mojang servers.
     * 
     * @param username
     *            The new username for this Account
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the Minecraft username used for this Account.
     * 
     * @return The Minecraft username for this Account
     */
    public String getMinecraftUsername() {
        return this.minecraftUsername;
    }

    /**
     * Sets the Minecraft username used for this Account.
     * 
     * @param username
     *            The new Minecraft username for this Account
     */
    public void setMinecraftUsername(String username) {
        this.minecraftUsername = username;
    }

    /**
     * Gets the password for logging into Mojang servers for this Account.
     * 
     * @return The password for logging into Mojang servers
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password for this Account.
     * 
     * @param password
     *            The password for the Account
     */
    public void setPassword(String password) {
        this.password = password;
        this.encryptedPassword = Utils.encrypt(this.password);
    }

    /**
     * If this account should save the password or not for convenience.
     * 
     * @return True if the Account has been set to remember, false otherwise
     */
    public boolean isRemembered() {
        return this.remember;
    }

    /**
     * Sets this Account to remember or not remember the password.
     * 
     * @param remember
     *            True if the password should be remembered, False if it shouldn't be remembered
     */
    public void setRemember(boolean remember) {
        this.remember = remember;
        if (!this.remember) {
            this.password = "";
            this.encryptedPassword = "";
        }
    }

    /**
     * Reads in the object from file into an Object.
     * 
     * @param ois
     *            The InputStream for the object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject(); // Read the object in
        if (this.encryptedPassword == null) {
            this.password = "";
            this.remember = false;
        } else {
            this.password = Utils.decrypt(this.encryptedPassword);
        }
        this.isReal = true;
    }

    /**
     * Gets a List of packs this Account has collapsed in the {@link PacksTab}.
     * 
     * @return List of collapsed packs
     */
    public List<String> getCollapsedPacks() {
        if (this.collapsedPacks == null) {
            this.collapsedPacks = new ArrayList<String>();
        }
        return this.collapsedPacks;
    }

    /**
     * Gets a List of instances this Account has collapsed in the {@link InstancesTab}.
     * 
     * @return List of collapsed instances
     */
    public List<String> getCollapsedInstances() {
        if (this.collapsedInstances == null) {
            this.collapsedInstances = new ArrayList<String>();
        }
        return this.collapsedInstances;
    }

    /**
     * Updates this Account's skin by redownloading the Minecraft skin from Mojang's skin server.
     */
    public void updateSkin() {
        if (!this.skinUpdating) {
            this.skinUpdating = true;
            final File file = new File(App.settings.getSkinsDir(), this.minecraftUsername + ".png");
            if (file.exists()) {
                Utils.delete(file);
            }
            App.settings.log("Downloading skin for " + this.minecraftUsername);
            final ProgressDialog dialog = new ProgressDialog(
                    App.settings.getLocalizedString("account.downloadingskin"), 0,
                    App.settings.getLocalizedString("account.downloadingminecraftskin",
                            this.minecraftUsername), "Aborting downloading Minecraft skin for "
                            + this.minecraftUsername);
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
                }

                ;
            });
            dialog.start();
            this.skinUpdating = false;
        }
    }

    @Override
    public String toString() {
        return this.minecraftUsername;
    }

}
