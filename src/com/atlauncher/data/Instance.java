/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.atlauncher.gui.ProgressDialog;
import com.atlauncher.mclauncher.LegacyMCLauncher;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.utils.Authentication;
import com.atlauncher.utils.Utils;

import javax.swing.*;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Instance implements Serializable {

    private static final long serialVersionUID = 1925450686877381452L;
    private String name;
    private String pack;
    private String installedBy;
    private String version;
    private String minecraftVersion;
    private int memory = 0;
    private int permgen = 0;
    private String jarOrder;
    private String librariesNeeded = null;
    private String extraArguments = null;
    private String minecraftArguments = null;
    private String mainClass = null;
    private String assets = null;
    private boolean isConverted = false;
    private transient Pack realPack;
    private boolean isDev;
    private boolean isPlayable;
    private boolean newLaunchMethod;
    private String[] modsInstalled;
    private ArrayList<DisableableMod> mods;
    private ArrayList<String> ignoredUpdates;

    public Instance(String name, String pack, Pack realPack, boolean installJustForMe,
            String version, String minecraftVersion, int memory, int permgen,
            ArrayList<DisableableMod> mods, String jarOrder, String librariesNeeded,
            String extraArguments, String minecraftArguments, String mainClass, String assets,
            boolean isDev, boolean isPlayable, boolean newLaunchMethod) {
        this.name = name;
        this.pack = pack;
        this.realPack = realPack;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
        this.memory = memory;
        this.permgen = permgen;
        this.mods = mods;
        this.jarOrder = jarOrder;
        this.librariesNeeded = librariesNeeded;
        this.mainClass = mainClass;
        this.assets = assets;
        this.jarOrder = jarOrder;
        this.extraArguments = extraArguments;
        this.minecraftArguments = minecraftArguments;
        this.isDev = isDev;
        this.isPlayable = isPlayable;
        this.newLaunchMethod = newLaunchMethod;
        if (installJustForMe) {
            this.installedBy = App.settings.getAccount().getMinecraftUsername();
        } else {
            this.installedBy = null;
        }
        this.isConverted = true;
    }

    public Instance(String name, String pack, Pack realPack, boolean installJustForMe,
            String version, String minecraftVersion, int memory, int permgen,
            ArrayList<DisableableMod> mods, String jarOrder, String librariesNeeded,
            String extraArguments, String minecraftArguments, String mainClass, String assets,
            boolean isDev, boolean newLaunchMethod) {
        this(name, pack, realPack, installJustForMe, version, minecraftVersion, memory, permgen,
                mods, jarOrder, librariesNeeded, extraArguments, minecraftArguments, mainClass,
                assets, isDev, true, newLaunchMethod);
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

    public boolean hasInstalledMods() {
        return (this.mods == null ? false : true);
    }

    public String getJarOrder() {
        return this.jarOrder;
    }

    public int getMemory() {
        return this.memory;
    }

    public ArrayList<DisableableMod> getInstalledMods() {
        return this.mods;
    }

    public int getPermGen() {
        return this.permgen;
    }

    public void rename(String newName) {
        File oldDir = getRootDirectory();
        this.name = newName;
        File newDir = getRootDirectory();
        Utils.moveDirectory(oldDir, newDir);
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

    public boolean hasBeenConverted() {
        return isConverted;
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

    public void ignoreUpdate() {
        if (this.ignoredUpdates == null) {
            this.ignoredUpdates = new ArrayList<String>();
        }
        String version = getLatestVersion();
        if (!hasUpdateBeenIgnored(version)) {
            this.ignoredUpdates.add(version);
            App.settings.saveInstances();
        }
    }

    public boolean hasUpdateBeenIgnored(String version) {
        if (version == null) {
            return true;
        }
        if (ignoredUpdates == null) {
            return false;
        }
        if (ignoredUpdates.size() == 0) {
            return false;
        }
        for (String vers : ignoredUpdates) {
            if (vers.equalsIgnoreCase(version)) {
                return true;
            }
        }
        return false;
    }

    public void convert() {
        if (this.minecraftArguments != null) {
            this.extraArguments = this.minecraftArguments;
            this.minecraftArguments = null;
        }
        this.isConverted = true;
    }

    public void removeInstalledMod(DisableableMod mod) {
        if (mod.isDisabled()) {
            Utils.delete(mod.getDisabledFile(this));
        } else {
            Utils.delete(mod.getFile(this));
        }
        this.mods.remove(mod);
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

    public File getAssetsDir() {
        return new File(App.settings.getVirtualAssetsDir(), getAssets());
    }

    public File getSavesDirectory() {
        return new File(getRootDirectory(), "saves");
    }

    public File getModsDirectory() {
        return new File(getRootDirectory(), "mods");
    }

    public File getIC2LibDirectory() {
        return new File(getModsDirectory(), "ic2");
    }

    public File getDenLibDirectory() {
        return new File(getModsDirectory(), "denlib");
    }

    public File getPluginsDirectory() {
        return new File(getRootDirectory(), "plugins");
    }

    public File getShaderPacksDirectory() {
        return new File(getRootDirectory(), "shaderpacks");
    }

    public File getDisabledModsDirectory() {
        return new File(getRootDirectory(), "disabledmods");
    }

    public File getCoreModsDirectory() {
        return new File(getRootDirectory(), "coremods");
    }

    public File getJarModsDirectory() {
        return new File(getRootDirectory(), "jarmods");
    }

    public File getTexturePacksDirectory() {
        return new File(getRootDirectory(), "texturepacks");
    }

    public File getResourcePacksDirectory() {
        return new File(getRootDirectory(), "resourcepacks");
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

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public void setPermgen(int permgen) {
        this.permgen = permgen;
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

    public String getExtraArguments() {
        return this.extraArguments;
    }

    public void setExtraArguments(String extraArguments) {
        this.extraArguments = extraArguments;
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

    public String getAssets() {
        if (this.assets == null) {
            return "legacy";
        }
        return this.assets;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public void setAssets(String assets) {
        this.assets = assets;
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
                if (!realPack.getLatestVersion().getVersion().equalsIgnoreCase(this.version)
                        && !realPack.isLatestVersionNoUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getLatestVersion() {
        if (realPack != null) {
            return realPack.getLatestVersion().getVersion();
        }
        return null;
    }

    public boolean wasModInstalled(String name) {
        if (mods != null) {
            for (DisableableMod mod : mods) {
                if (mod.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setModsInstalled(ArrayList<DisableableMod> mods) {
        this.mods = mods;
    }

    public boolean hasExtraArguments() {
        if (this.extraArguments != null) {
            return true;
        }
        return false;
    }

    public boolean hasMinecraftArguments() {
        if (this.minecraftArguments != null) {
            return true;
        }
        return false;
    }

    public boolean launch() {
        final Account account = App.settings.getAccount();
        if (account == null) {
            String[] options = { App.settings.getLocalizedString("common.ok") };
            JOptionPane.showOptionDialog(App.settings.getParent(),
                    App.settings.getLocalizedString("instance.noaccount"),
                    App.settings.getLocalizedString("instance.noaccountselected"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                    options[0]);
            App.settings.setMinecraftLaunched(false);
            return false;
        } else {
            if (App.settings.getMemory() < this.memory) {
                String[] options = { App.settings.getLocalizedString("common.yes"),
                        App.settings.getLocalizedString("common.no") };
                int ret = JOptionPane.showOptionDialog(
                        App.settings.getParent(),
                        "<html><center>"
                                + App.settings.getLocalizedString("instance.insufficientram", "<b>"
                                        + this.memory + "</b> MB<br/><br/>") + "</center></html>",
                        App.settings.getLocalizedString("instance.insufficientramtitle"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                        options[0]);
                if (ret != 0) {
                    App.settings
                            .log("Launching of instance cancelled due to user cancelling memory warning!",
                                    LogMessageType.warning, false);
                    App.settings.setMinecraftLaunched(false);
                    return false;
                }
            }
            if (App.settings.getPermGen() < this.permgen) {
                String[] options = { App.settings.getLocalizedString("common.yes"),
                        App.settings.getLocalizedString("common.no") };
                int ret = JOptionPane
                        .showOptionDialog(
                                App.settings.getParent(),
                                "<html><center>"
                                        + App.settings.getLocalizedString(
                                                "instance.insufficientpermgen", "<b>"
                                                        + this.permgen + "</b> MB<br/><br/>")
                                        + "</center></html>", App.settings
                                        .getLocalizedString("instance.insufficientpermgentitle"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
                if (ret != 0) {
                    App.settings
                            .log("Launching of instance cancelled due to user cancelling memory warning!",
                                    LogMessageType.warning, false);
                    App.settings.setMinecraftLaunched(false);
                    return false;
                }
            }
            AuthenticationResponse sess = null;
            if (!App.settings.isInOfflineMode()) {
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
                        App.settings.log("Aborting login for " + account.getMinecraftUsername(),
                                LogMessageType.error, false);
                        App.settings.setMinecraftLaunched(false);
                        return false;
                    }
                }
                App.settings.log("Logging into Minecraft!");
                final String pass = password;
                final ProgressDialog dialog = new ProgressDialog(
                        App.settings.getLocalizedString("account.loggingin"), 0,
                        App.settings.getLocalizedString("account.loggingin"), "Aborting login for "
                                + account.getMinecraftUsername());
                dialog.addThread(new Thread() {
                    public void run() {
                        dialog.setReturnValue(Authentication.login(account.getUsername(), pass));
                        dialog.close();
                    };
                });
                dialog.start();
                if (dialog.getReturnValue() == null) {
                    return false;
                }
                sess = (AuthenticationResponse) dialog.getReturnValue();
                if (sess.hasError()) {
                    App.settings.log(sess.getErrorMessage(), LogMessageType.error, false);
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(
                            App.settings.getParent(),
                            "<html><center>"
                                    + App.settings.getLocalizedString("instance.errorloggingin",
                                            "<br/><br/>" + sess.getErrorMessage())
                                    + "</center></html>", App.settings
                                    .getLocalizedString("instance.errorloggingintitle"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                    App.settings.setMinecraftLaunched(false);
                    return false;
                }
            } else {
                sess = new AuthenticationResponse("token:0:0", false);
            }

            final AuthenticationResponse session = sess;
            Thread launcher = new Thread() {
                public void run() {
                    try {
                        long start = System.currentTimeMillis();
                        if (App.settings.getParent() != null) {
                            App.settings.getParent().setVisible(false);
                        }
                        // Create a note of worlds for auto backup if enabled
                        HashMap<String, Long> preWorldList = new HashMap<String, Long>();
                        if (App.settings.isAdvancedBackupsEnabled() && App.settings.getAutoBackup()) {
                            if (getSavesDirectory().exists()) {
                                File[] files = getSavesDirectory().listFiles();
                                if (files != null) {
                                    for (File file : files) {
                                        if (file.isDirectory())
                                            preWorldList.put(file.getName(), file.lastModified());
                                    }
                                }
                            }
                        }
                        Process process = null;
                        if (isNewLaunchMethod()) {
                            process = MCLauncher.launch(account, Instance.this, session);
                        } else {
                            process = LegacyMCLauncher.launch(account, Instance.this, session);
                        }
                        App.settings.showKillMinecraft(process);
                        InputStream is = process.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String line;
                        while ((line = br.readLine()) != null) {
                            App.settings.logMinecraft(line);
                        }
                        App.settings.hideKillMinecraft();
                        if (App.settings.getParent() != null) {
                            App.settings.getParent().setVisible(true);
                        }
                        long end = System.currentTimeMillis();
                        if (App.settings.isInOfflineMode()) {
                            App.settings.checkOnlineStatus();
                        }
                        int exitValue = 0; // Assume we exited fine
                        try {
                            exitValue = process.exitValue(); // Try to get the real exit value
                        } catch (IllegalThreadStateException e) {
                            App.settings.logStackTrace(e);
                            process.destroy(); // Kill the process
                        }
                        if (exitValue != 0) {
                            if (getRealPack().isLoggingEnabled() && App.settings.enableLogs()
                                    && getRealPack().crashReportsEnabled()) {
                                Thread crashThread = new Thread() {
                                    @Override
                                    public void run() {
                                        uploadCrashLog(); // Auto upload crash log if enabled
                                    }
                                };
                                crashThread.start();
                            }
                        } else if (App.settings.isAdvancedBackupsEnabled()
                                && App.settings.getAutoBackup()) {
                            // Begin backup
                            if (getSavesDirectory().exists()) {
                                File[] files = getSavesDirectory().listFiles();
                                if (files != null) {
                                    for (File file : files) {
                                        if ((file.isDirectory()) && (!file.getName().equals("NEI"))) {
                                            if (preWorldList.containsKey(file.getName())) {
                                                // Only backup if file changed
                                                if (!(preWorldList.get(file.getName()) == file
                                                        .lastModified())) {
                                                    SyncAbstract sync = SyncAbstract.syncList
                                                            .get(App.settings.getLastSelectedSync());
                                                    sync.backupWorld(
                                                            file.getName()
                                                                    + String.valueOf(file
                                                                            .lastModified()), file,
                                                            Instance.this);
                                                }
                                            }
                                            // Or backup if a new file is found
                                            else {
                                                SyncAbstract sync = SyncAbstract.syncList
                                                        .get(App.settings.getLastSelectedSync());
                                                sync.backupWorld(
                                                        file.getName()
                                                                + String.valueOf(
                                                                        file.lastModified())
                                                                        .replace(":", ""), file,
                                                        Instance.this);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        App.settings.setMinecraftLaunched(false);
                        if (!App.settings.isInOfflineMode() && isLeaderboardsEnabled()) {
                            App.settings.apiCall(account.getMinecraftUsername(),
                                    "addleaderboardtime"
                                            + (App.settings.enableLeaderboards() ? "" : "generic"),
                                    (getRealPack() == null ? "0" : getRealPack().getID() + ""),
                                    ((end - start) / 1000) + "", (isDev ? "dev" : getVersion()));
                            if (App.settings.hasUpdatedFiles()) {
                                App.settings.reloadLauncherData();
                            }
                        }
                    } catch (IOException e1) {
                        App.settings.logStackTrace(e1);
                    }
                }
            };
            launcher.start();
            return true;
        }
    }

    public void uploadCrashLog() {
        Thread thread = new Thread() {
            public void run() {
                String result = Utils.uploadPaste("ATLauncher Log", App.settings.getLog());
                if (result.contains("%PASTECHECKURL%")) {
                    App.settings.apiCall(App.settings.getAccount().getMinecraftUsername(),
                            "reportcrash", realPack.getID() + "", getVersion(),
                            result.replace("http://paste.atlauncher.com/view/", ""));
                    App.settings.log("Log uploaded and reported to ModPack creator: " + result);
                } else {
                    App.settings
                            .log("Log failed to upload: " + result, LogMessageType.error, false);
                }
            };
        };
        thread.run();
    }
}
