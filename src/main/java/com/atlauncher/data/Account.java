/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
package com.atlauncher.data;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;


import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.mojang.api.MinecraftProfileResponse;
import com.atlauncher.data.mojang.api.ProfileTexture;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.Authentication;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.MojangAPIUtils;
import com.atlauncher.utils.Utils;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;

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
     * The client token.
     */
    private String clientToken;

    /**
     * The access token.
     */
    private String accessToken;

    /**
     * The account's Minecraft username.
     */
    private String minecraftUsername;

    /**
     * The UUID of the account.
     */
    private String uuid;

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
     * The instance names this account has collapsed in the {@link InstancesTab}, if
     * any.
     */
    private List<String> collapsedInstances;

    /**
     * If the skin is currently being updated.
     */
    private boolean skinUpdating = false;

    /**
     * This is the store for this username as returned by Mojang.
     */
    private Map<String, Object> store;

    /**
     * Constructor for a real user Account.
     *
     * @param username          The name of the Account
     * @param password          The password of the Account
     * @param minecraftUsername The Minecraft username of the Account
     * @param uuid              The UUID of the Account
     * @param remember          If this Account's password should be remembered or
     *                          not
     */
    public Account(String username, String password, String minecraftUsername, String uuid, boolean remember) {
        this.username = username;
        if (remember) {
            this.password = password;
            this.encryptedPassword = Utils.encrypt(password);
        }
        this.minecraftUsername = minecraftUsername;
        this.uuid = uuid;
        this.remember = remember;
        this.isReal = true;
        this.collapsedPacks = new ArrayList<>();
        this.collapsedInstances = new ArrayList<>();
    }

    /**
     * Constructor for a fake user account, used for displaying non selectable
     * accounts.
     *
     * @param name The name of the Account
     */
    public Account(String name) {
        this.username = "";
        this.minecraftUsername = name;
        this.uuid = UUID.randomUUID() + "";
        this.remember = false;
        this.isReal = false;
        this.collapsedPacks = new ArrayList<>();
        this.collapsedInstances = new ArrayList<>();
    }

    /**
     * Creates an {@link ImageIcon} of the Account's Minecraft skin, getting just
     * the head of it.
     *
     * @return The Account's Minecraft usernames head
     */
    public ImageIcon getMinecraftHead() {
        File file = null;
        if (this.isReal()) {
            file = new File(App.settings.getSkinsDir(),
                    (this.isUUIDNull() ? "default" : this.getUUIDNoDashes()) + ".png");
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
            LogManager.logStackTrace(e);
        }

        if (image == null) {
            file = new File(App.settings.getSkinsDir(), "default.png");
        }

        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
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
            file = new File(App.settings.getSkinsDir(), this.getUUIDNoDashes() + ".png");
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
            LogManager.logStackTrace(e);
        }

        if (image == null) {
            file = new File(App.settings.getSkinsDir(), "default.png");
        }

        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        BufferedImage head = image.getSubimage(8, 8, 8, 8);
        BufferedImage helmet = image.getSubimage(40, 8, 8, 8);
        BufferedImage arm = image.getSubimage(44, 20, 4, 12);
        BufferedImage body = image.getSubimage(20, 20, 8, 12);
        BufferedImage leg = image.getSubimage(4, 20, 4, 12);
        BufferedImage skin = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);

        Graphics g = skin.getGraphics();
        g.drawImage(head, 4, 0, null);

        // Draw the helmet on the skin if more than half of the pixels are not
        // transparent.
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
     * @return true if the Account is real and was added by the user, false
     *         otherwise
     */
    public boolean isReal() {
        return this.isReal;
    }

    /**
     * Gets the username used for logging into Mojang servers. Can be an email
     * address or a username if the user has not migrated their Minecraft account to
     * a Mojang account.
     *
     * @return The username used for logging into Mojang servers
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username used for this Account to login to Mojang servers.
     *
     * @param username The new username for this Account
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
     * @param username The new Minecraft username for this Account
     */
    public void setMinecraftUsername(String username) {
        this.minecraftUsername = username;
    }

    /**
     * Gets the UUID of this account.
     *
     * @return The UUID for this Account
     */
    public String getUUID() {
        return (this.uuid == null ? "0" : this.uuid);
    }

    public boolean isUUIDNull() {
        return this.uuid == null;
    }

    /**
     * Gets the UUID of this account with no dashes.
     *
     * @return The UUID for this Account with no dashes
     */
    public String getUUIDNoDashes() {
        return (this.uuid == null ? "0" : this.uuid.replace("-", ""));
    }

    /**
     * Gets the real UUID of this account.
     *
     * @return The real UUID for this Account
     */
    public UUID getRealUUID() {
        return (this.uuid == null ? UUID.randomUUID() : UUIDTypeAdapter.fromString(this.uuid));
    }

    /**
     * Sets the uuid for this Account.
     *
     * @param uuid The new UUID for this Account
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public boolean hasUUID() {
        return this.uuid != null;
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
     * @param password The password for the Account
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
     * @param remember True if the password should be remembered, False if it
     *                 shouldn't be remembered
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
     * @param ois The InputStream for the object
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
            if (this.password == null) {
                LogManager.error("Error reading in saved password from file!");
                this.password = "";
                this.remember = false;
            }
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
            this.collapsedPacks = new ArrayList<>();
        }
        return this.collapsedPacks;
    }

    /**
     * Gets a List of instances this Account has collapsed in the
     * {@link InstancesTab}.
     *
     * @return List of collapsed instances
     */
    public List<String> getCollapsedInstances() {
        if (this.collapsedInstances == null) {
            this.collapsedInstances = new ArrayList<>();
        }
        return this.collapsedInstances;
    }

    /**
     * Updates this Account's skin by redownloading the Minecraft skin from Mojang's
     * skin server.
     */
    public void updateSkin() {
        if (!this.skinUpdating) {
            this.skinUpdating = true;
            final File file = new File(App.settings.getSkinsDir(), this.getUUIDNoDashes() + ".png");
            LogManager.info("Downloading skin for " + this.minecraftUsername);
            final ProgressDialog dialog = new ProgressDialog(
                    Language.INSTANCE.localize("account" + "" + ".downloadingskin"), 0,
                    Language.INSTANCE.localizeWithReplace("account.downloadingminecraftskin", this.minecraftUsername),
                    "Aborting downloading Minecraft skin for " + this.minecraftUsername);
            final UUID uid = this.getRealUUID();
            dialog.addThread(new Thread() {
                public void run() {
                    dialog.setReturnValue(false);
                    String skinURL = getSkinURL();
                    if (skinURL == null) {
                        LogManager.warn("Couldn't download skin because the url found was NULL. Using default skin");
                        if (!file.exists()) {
                            String skinFilename = "default.png";

                            // even UUID's use the alex skin
                            if ((uid.hashCode() & 1) != 0) {
                                skinFilename = "default_alex.png";
                            }

                            // Only copy over the default skin if there is no skin for the user
                            Utils.copyFile(new File(App.settings.getSkinsDir(), skinFilename), file, true);
                            dialog.setReturnValue(true);
                        }
                    } else {
                        try {
                            HttpURLConnection conn = (HttpURLConnection) new URL(skinURL).openConnection();
                            if (conn.getResponseCode() == 200) {
                                if (file.exists()) {
                                    Utils.delete(file);
                                }
                                Downloadable skin = new Downloadable(skinURL, file, null, null, false);
                                skin.download(false);
                                dialog.setReturnValue(true);
                            } else {
                                if (!file.exists()) {
                                    String skinFilename = "default.png";

                                    // even UUID's use the alex skin
                                    if ((uid.hashCode() & 1) != 0) {
                                        skinFilename = "default_alex.png";
                                    }

                                    // Only copy over the default skin if there is no skin for the user
                                    Utils.copyFile(new File(App.settings.getSkinsDir(), skinFilename), file, true);
                                    dialog.setReturnValue(true);
                                }
                            }
                        } catch (IOException e) {
                            LogManager.logStackTrace(e);
                        }
                        App.settings.reloadAccounts();
                    }
                    dialog.close();
                }
            });
            dialog.start();
            if (!(Boolean) dialog.getReturnValue()) {
                DialogManager.okDialog().setTitle(Language.INSTANCE.localize("common.error"))
                        .setContent(Language.INSTANCE.localize("account.skinerror")).setType(DialogManager.ERROR)
                        .show();
            }
            this.skinUpdating = false;
        }
    }

    public String getSkinURL() {
        StringBuilder response = null;
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + this.getUUIDNoDashes());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("GET");

            connection.setUseCaches(false);

            // Read the result

            if (connection.getResponseCode() != 200) {
                return null;
            }

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException e) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            response = null;
        }

        if (response == null) {
            return null;
        }

        MinecraftProfileResponse profile = Gsons.DEFAULT.fromJson(response.toString(), MinecraftProfileResponse.class);

        if (!profile.hasProperties()) {
            return null;
        }

        ProfileTexture texture = profile.getUserProperty("textures").getTexture("SKIN");

        if (texture == null) {
            return null;
        }

        return texture.getUrl();
    }

    public String getAccessToken() {
        return (this.accessToken == null ? "0" : this.accessToken);
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean hasAccessToken() {
        return this.accessToken != null;
    }

    public String getClientToken() {
        return (this.clientToken == null ? "0" : this.clientToken);
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public boolean hasClientToken() {
        return this.clientToken != null;
    }

    @Override
    public String toString() {
        return this.minecraftUsername;
    }

    public String getSession(LoginResponse response) {
        if (!response.isOffline() && response != null && response.getAuth().isLoggedIn()
                && response.getAuth().canPlayOnline()) {
            if (response.getAuth() instanceof YggdrasilUserAuthentication) {
                return String.format("token:%s:%s", response.getAuth().getAuthenticatedToken(),
                        UUIDTypeAdapter.fromUUID(response.getAuth().getSelectedProfile().getId()));
            } else {
                return response.getAuth().getAuthenticatedToken();
            }
        }
        return "token:0:0";
    }

    public boolean hasStore() {
        return this.store != null;
    }

    public Map<String, Object> getStore() {
        return this.store;
    }

    public void saveStore(Map<String, Object> store) {
        this.store = store;
        App.settings.saveAccounts();
    }

    public LoginResponse login() {
        LoginResponse response = null;

        if (this.hasAccessToken() && this.hasStore()) {
            LogManager.info("Trying to login with access token!");
            response = Authentication.login(this);
        }

        if (response == null || response.hasError()) {
            if (this.hasAccessToken()) {
                LogManager.error("Access token is NOT valid! Will attempt to get another one!");
                this.setAccessToken(null);
                App.settings.saveAccounts();
            }

            if (!this.isRemembered()) {
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                JLabel passwordLabel = new JLabel(
                        Language.INSTANCE.localizeWithReplace("instance.enterpassword", this.getMinecraftUsername()));

                JPasswordField passwordField = new JPasswordField();
                panel.add(passwordLabel, BorderLayout.NORTH);
                panel.add(passwordField, BorderLayout.CENTER);

                int ret = DialogManager.confirmDialog()
                        .setTitle(Language.INSTANCE.localize("instance.enterpasswordtitle")).setContent(panel).show();

                if (ret == DialogManager.OK_OPTION) {
                    if (passwordField.getPassword().length == 0) {
                        LogManager.error("Aborting login for " + this.getMinecraftUsername() + ", no password entered");
                        App.settings.setMinecraftLaunched(false);
                        return null;
                    }

                    this.setPassword(new String(passwordField.getPassword()));
                } else {
                    LogManager.error("Aborting login for " + this.getMinecraftUsername());
                    App.settings.setMinecraftLaunched(false);
                    return null;
                }
            }

            response = Authentication.login(Account.this, true);
        }

        if (response.hasError() && !response.isOffline()) {
            LogManager.error(response.getErrorMessage());

            DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.errorloggingintitle"))
                    .setContent(HTMLUtils.centerParagraph(Language.INSTANCE
                            .localizeWithReplace("instance.errorloggingin", "<br/><br/>" + response.getErrorMessage())))
                    .setType(DialogManager.ERROR).show();

            App.settings.setMinecraftLaunched(false);
            return null;
        }

        if (!response.isOffline() && !response.getAuth().canPlayOnline()) {
            return null;
        }

        if (!response.isOffline()) {
            this.setAccessToken(response.getAuth().getAuthenticatedToken());
            this.setUUID(response.getAuth().getSelectedProfile().getId().toString());
            response.save();
            App.settings.saveAccounts();
        }

        return response;
    }

    public boolean checkForUsernameChange() {
        if (this.uuid == null) {
            LogManager.error("The account " + this.minecraftUsername + " has no UUID associated with it !");
            return false;
        }

        String currentUsername = MojangAPIUtils.getCurrentUsername(this.getUUIDNoDashes());

        if (currentUsername == null) {
            return false;
        }

        if (!currentUsername.equals(this.minecraftUsername)) {
            LogManager.info("The username for account with UUID of " + this.getUUIDNoDashes() + " changed from "
                    + this.minecraftUsername + " to " + currentUsername);
            this.minecraftUsername = currentUsername;
            return true;
        }

        return false;
    }
}
