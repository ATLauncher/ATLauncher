/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.atlauncher.App;
import com.atlauncher.gui.Utils;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.mclauncher.NewMCLauncher;

public class Instance implements Serializable {

    private static final long serialVersionUID = 1925450686877381452L;
    private String name;
    private String pack;
    private String installedBy;
    private String version;
    private String minecraftVersion;
    private int permgen = 0;
    private String jarOrder;
    private String librariesNeeded = null;
    private String minecraftArguments = null;
    private String mainClass = null;
    private transient Pack realPack;
    private boolean isDev;
    private boolean isPlayable;
    private boolean newLaunchMethod;
    private String[] modsInstalled;

    public Instance(String name, String pack, Pack realPack, boolean installJustForMe,
            String version, String minecraftVersion, int permgen, String[] modsInstalled,
            String jarOrder, String librariesNeeded, String minecraftArguments, String mainClass,
            boolean isDev, boolean isPlayable, boolean newLaunchMethod) {
        this.name = name;
        this.pack = pack;
        this.realPack = realPack;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
        this.permgen = permgen;
        this.modsInstalled = modsInstalled;
        this.jarOrder = jarOrder;
        if (newLaunchMethod) {
            this.librariesNeeded = librariesNeeded;
            this.mainClass = mainClass;
            this.jarOrder = jarOrder;
            this.minecraftArguments = minecraftArguments;
        }
        this.isDev = isDev;
        this.isPlayable = isPlayable;
        this.newLaunchMethod = newLaunchMethod;
        if (installJustForMe) {
            this.installedBy = App.settings.getAccount().getMinecraftUsername();
        } else {
            this.installedBy = null;
        }
    }

    public Instance(String name, String pack, Pack realPack, boolean installJustForMe,
            String version, String minecraftVersion, int permgen, String[] modsInstalled,
            String jarOrder, String librariesNeeded, String minecraftArguments, String mainClass,
            boolean isDev, boolean newLaunchMethod) {
        this(name, pack, realPack, installJustForMe, version, minecraftVersion, permgen,
                modsInstalled, jarOrder, librariesNeeded, minecraftArguments, mainClass, isDev,
                true, newLaunchMethod);
    }

    public String getName() {
        return this.name;
    }

    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public String getPackName() {
        return pack;
    }

    public String getJarOrder() {
        return this.jarOrder;
    }

    public int getPermGen() {
        return this.permgen;
    }

    /**
     * Gets a file safe and URL safe name which simply means replacing all non alpha numerical
     * characters with nothing
     * 
     * @return File safe and URL safe name of the pack
     */
    public String getSafePackName() {
        return this.pack.replaceAll("[^A-Za-z0-9]", "");
    }

    public ImageIcon getImage() {
        File imageFile = new File(App.settings.getImagesDir(), getSafePackName().toLowerCase()
                + ".png");
        if (!imageFile.exists()) {
            imageFile = new File(App.settings.getImagesDir(), "defaultimage.png");
        }
        return Utils.getIconImage(imageFile);
    }

    public String getPackDescription() {
        if (this.realPack != null) {
            return this.realPack.getDescription();
        } else {
            return "No Description!";
        }
    }

    public boolean isLeaderboardsEnabled() {
        if (this.realPack != null) {
            return this.realPack.isLeaderboardsEnabled();
        } else {
            return false;
        }
    }

    public boolean isLoggingEnabled() {
        if (this.realPack != null) {
            return this.realPack.isLoggingEnabled();
        } else {
            return false;
        }
    }

    public String getVersion() {
        return version;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public File getRootDirectory() {
        return new File(App.settings.getInstancesDir(), getSafeName());
    }

    public File getSavesDirectory() {
        return new File(getRootDirectory(), "saves");
    }

    public File getModsDirectory() {
        return new File(getRootDirectory(), "mods");
    }

    public File getCoreModsDirectory() {
        return new File(getRootDirectory(), "coremods");
    }

    public File getJarModsDirectory() {
        return new File(getRootDirectory(), "jarmods");
    }

    public File getBinDirectory() {
        return new File(getRootDirectory(), "bin");
    }

    public File getNativesDirectory() {
        return new File(getBinDirectory(), "natives");
    }

    public File getMinecraftJar() {
        return new File(getBinDirectory(), "minecraft.jar");
    }

    public boolean canInstall() {
        if (realPack == null) {
            return false;
        }
        return realPack.canInstall();
    }

    public Pack getRealPack() {
        return this.realPack;
    }

    public void setRealPack(Pack realPack) {
        this.realPack = realPack;
    }

    public boolean hasJarMods() {
        if (this.jarOrder == null) {
            return false;
        } else {
            return true;
        }
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public void setJarOrder(String jarOrder) {
        this.jarOrder = jarOrder;
    }

    public void setPlayable() {
        this.isPlayable = true;
    }

    public void setUnplayable() {
        this.isPlayable = false;
    }
    
    public void setDevVersion() {
        this.isDev = true;
    }
    
    public void setNotDevVersion() {
        this.isDev = false;
    }

    public boolean isDev() {
        return this.isDev;
    }

    public boolean isPlayable() {
        return this.isPlayable;
    }

    public void setIsNewLaunchMethod(boolean newLaunchMethod) {
        this.newLaunchMethod = newLaunchMethod;
    }

    public boolean isNewLaunchMethod() {
        return this.newLaunchMethod;
    }

    public String getLibrariesNeeded() {
        return librariesNeeded;
    }

    public void setLibrariesNeeded(String librariesNeeded) {
        this.librariesNeeded = librariesNeeded;
    }

    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    public void setMinecraftArguments(String minecraftArguments) {
        this.minecraftArguments = minecraftArguments;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public boolean canPlay() {
        if (App.settings.getAccount() == null) {
            return false;
        } else if (!App.settings.getAccount().isReal()) {
            return false;
        }
        if (installedBy != null) {
            if (!App.settings.getAccount().getMinecraftUsername().equalsIgnoreCase(installedBy)) {
                return false;
            }
        }
        return true;
    }

    public boolean hasUpdate() {
        if (realPack != null) {
            if (realPack.hasVersions() && !this.version.equalsIgnoreCase("Dev")) {
                if (!realPack.getLatestVersion().equalsIgnoreCase(this.version)
                        && !realPack.isLatestVersionNoUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean wasModInstalled(String name) {
        if (modsInstalled != null) {
            for (String modName : modsInstalled) {
                if (modName.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setModsInstalled(String[] modsInstalled) {
        this.modsInstalled = modsInstalled;
    }

    public boolean hasMinecraftArguments() {
        if (this.minecraftArguments != null) {
            return true;
        }
        return false;
    }

    public void launch() {
        final Account account = App.settings.getAccount();
        if (account == null) {
            String[] options = { App.settings.getLocalizedString("common.ok") };
            JOptionPane.showOptionDialog(App.settings.getParent(),
                    App.settings.getLocalizedString("instance.noaccount"),
                    App.settings.getLocalizedString("instance.noaccountselected"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                    options[0]);
        } else {
            String username = account.getUsername();
            String password = account.getPassword();
            if (!account.isRemembered()) {
                JPanel panel = new JPanel();
                panel.setLayout(new BorderLayout());
                JLabel passwordLabel = new JLabel(App.settings.getLocalizedString(
                        "instance.enterpassword", account.getMinecraftUsername()));
                JPasswordField passwordField = new JPasswordField();
                panel.add(passwordLabel, BorderLayout.NORTH);
                panel.add(passwordField, BorderLayout.CENTER);
                int ret = JOptionPane.showConfirmDialog(App.settings.getParent(), panel,
                        App.settings.getLocalizedString("instance.enterpasswordtitle"),
                        JOptionPane.OK_CANCEL_OPTION);
                if (ret == JOptionPane.OK_OPTION) {
                    password = new String(passwordField.getPassword());
                } else {
                    return;
                }
            }
            boolean loggedIn = false;
            String url = null;
            String sess = null;
            String auth = null;
            if (!App.settings.isInOfflineMode()) {
                if (isNewLaunchMethod()) {
                    String result = Utils.newLogin(username, password);
                    JSONParser parser = new JSONParser();
                    try {
                        Object obj = parser.parse(result);
                        JSONObject jsonObject = (JSONObject) obj;
                        if (jsonObject.containsKey("accessToken")) {
                            String accessToken = (String) jsonObject.get("accessToken");
                            JSONObject profile = (JSONObject) jsonObject.get("selectedProfile");
                            String profileID = (String) profile.get("id");
                            sess = "token:" + accessToken + ":" + profileID;
                            loggedIn = true;
                        } else {
                            auth = (String) jsonObject.get("errorMessage");
                        }
                    } catch (ParseException e1) {
                        App.settings.getConsole().logStackTrace(e1);
                    }
                } else {
                    try {
                        url = "https://login.minecraft.net/?user="
                                + URLEncoder.encode(username, "UTF-8") + "&password="
                                + URLEncoder.encode(password, "UTF-8") + "&version=999";
                    } catch (UnsupportedEncodingException e1) {
                        App.settings.getConsole().logStackTrace(e1);
                    }
                    auth = Utils.urlToString(url);
                    if (auth.contains(":")) {
                        String[] parts = auth.split(":");
                        if (parts.length == 5) {
                            loggedIn = true;
                            sess = parts[3];
                        }
                    }
                }
            } else {
                loggedIn = true;
                sess = "0";
            }
            if (!loggedIn) {
                String[] options = { App.settings.getLocalizedString("common.ok") };
                JOptionPane.showOptionDialog(
                        App.settings.getParent(),
                        "<html><center>"
                                + App.settings.getLocalizedString("instance.errorloggingin",
                                        "<br/><br/>" + auth) + "</center></html>",
                        App.settings.getLocalizedString("instance.errorloggingintitle"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                        options[0]);
            } else {
                final String session = sess;
                Thread launcher = new Thread() {
                    public void run() {
                        try {
                            long start = System.currentTimeMillis();
                            if (App.settings.getParent() != null) {
                                App.settings.getParent().setVisible(false);
                            }
                            Process process = null;
                            if (isNewLaunchMethod()) {
                                process = NewMCLauncher.launch(account, Instance.this, session);
                            } else {
                                process = MCLauncher.launch(account, Instance.this, session);
                            }
                            App.settings.showKillMinecraft(process);
                            InputStream is = process.getInputStream();
                            InputStreamReader isr = new InputStreamReader(is);
                            BufferedReader br = new BufferedReader(isr);
                            String line;
                            while ((line = br.readLine()) != null) {
                                App.settings.getConsole().logMinecraft(line);
                            }
                            App.settings.hideKillMinecraft();
                            if (App.settings.getParent() != null) {
                                App.settings.getParent().setVisible(true);
                            }
                            long end = System.currentTimeMillis();
                            if (!App.settings.isInOfflineMode() && isLeaderboardsEnabled()) {
                                if (App.settings.enableLeaderboards()) {
                                    App.settings.apiCall(account.getMinecraftUsername(),
                                            "addleaderboardtime", (getRealPack() == null ? "0"
                                                    : getRealPack().getID() + ""),
                                            ((end - start) / 1000) + "");
                                } else {
                                    App.settings.apiCall("NULL", "addleaderboardtime",
                                            (getRealPack() == null ? "0" : getRealPack().getID()
                                                    + ""), ((end - start) / 1000) + "");
                                }
                            }
                            if (App.settings.isUpdatedFiles()) {
                                App.settings.reloadLauncherData();
                            }
                        } catch (IOException e1) {
                            App.settings.getConsole().logStackTrace(e1);
                        }
                    }
                };
                launcher.start();
            }
        }
    }
}
