/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import com.atlauncher.data.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.OS;
import com.atlauncher.data.Pack;
import com.atlauncher.gui.LauncherConsole;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.gui.TrayMenu;
import com.atlauncher.gui.dialogs.SetupDialog;
import com.atlauncher.gui.theme.Theme;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.walker.ClearDirVisitor;

public class Loader {
    private SplashScreen splashScreen;

    public Loader() {
        File config = FileSystem.CONFIGS.toFile();
        if (!config.exists()) {
            File[] fileList = config.getParentFile().listFiles(new FileFilter());
            int files = fileList.length;
            if (files > 1) {
                String[] opt = {"Yes It's Fine", "Whoops. I'll Change That Now"};
                int ret = JOptionPane.showOptionDialog(null, HTMLUtils.centerParagraph("I've detected that you may " +
                        "not have installed this in the right location.<br/><br/>The exe or JAR file should " +
                        "be placed in it's own folder with nothing else in it.<br/><br/>Are you 100% sure " +
                        "that's what you've done?"), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane
                        .ERROR_MESSAGE, null, opt, opt[0]);
                if (ret != 0) {
                    System.exit(0);
                }
            }
        }

        // Load in the accounts, settings and languages
        AccountManager.loadAccounts();
        SettingsManager.loadSettings();
        LanguageManager.loadLanguages();

        splashScreen = new SplashScreen();
        // Load and show the splash screen while we load other things.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                splashScreen.setVisible(true);
            }
        });

        checkFolders(); // Checks the setup of the folders and makes sure they're there
        clearTempDir(); // Cleans all files in the Temp Dir
    }

    public void finish() {
        splashScreen.close();
    }

    public void checkIfUsingOldOSXApp() {
        if (this.isUsingMacApp() && !this.isUsingNewMacApp()) {
            String[] opt = {"Download"};

            JOptionPane.showOptionDialog(null, HTMLUtils.centerParagraph("You're using an old version of the" +
                    " ATLauncher Mac OSX app.<br/><br/>Please download the new Mac OSX app from below to " +
                    "keep playing!<br/><br/>Your instances and data will be transferred once the new app " +
                    "is launched.<br/><br/>Sorry for any inconvenience caused!"), "Error", JOptionPane
                    .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, opt, opt[0]);

            Utils.openBrowser("https://atl.pw/oldosxapp");
            System.exit(0);
        }
    }

    public void loadTheme() {
        Path themeFile = SettingsManager.getThemeFile();
        if (themeFile != null) {
            try {
                InputStream stream = null;

                ZipFile zipFile = new ZipFile(themeFile.toFile());
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.getName().equals("theme.json")) {
                        stream = zipFile.getInputStream(entry);
                        break;
                    }
                }

                if (stream != null) {
                    App.THEME = Gsons.THEMES.fromJson(new InputStreamReader(stream), Theme.class);
                    stream.close();
                }

                zipFile.close();
            } catch (Exception ex) {
                App.THEME = Theme.DEFAULT_THEME;
            }
        }

        try {
            setLAF();
            modifyLAF();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets the look and feel to be that of nimbus which is the base.
     *
     * @throws Exception
     */
    private void setLAF() throws Exception {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().equalsIgnoreCase("nimbus")) {
                UIManager.setLookAndFeel(info.getClassName());
            }
        }
    }

    /**
     * This modifies the look and feel based upon the theme loaded.
     *
     * @throws Exception
     */
    private void modifyLAF() throws Exception {
        App.THEME.apply();
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        ToolTipManager.sharedInstance().setInitialDelay(50);
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);

        if (OS.isMac()) {
            InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
        }
    }

    public void loadConsole() {
        App.console = new LauncherConsole();
        App.console.setVisible(SettingsManager.enableConsole());
        LogManager.start();
    }
    
    public boolean isUsingMacApp() {
        return OS.isMac() && Files.exists(FileSystem.BASE_DIR.getParent().resolve("MacOS"));
    }

    public boolean isUsingNewMacApp() {
        return Files.exists(FileSystem.BASE_DIR.getParent().getParent().resolve("MacOS").resolve
                ("universalJavaApplicationStub"));
    }

    /**
     * Checks the directory to make sure all the necessary folders are there
     */
    private void checkFolders() {
        try {
            for (Field field : FileSystem.class.getDeclaredFields()) {
                Path p = (Path) field.get(null);
                if (!Files.exists(p)) {
                    FileUtils.createDirectory(p);
                }

                if (!Files.isDirectory(p)) {
                    Files.delete(p);
                    FileUtils.createDirectory(p);
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
    }

    /**
     * Deletes all files in the Temp directory
     */
    public void clearTempDir() {
        try {
            Files.walkFileTree(FileSystem.TMP, new ClearDirVisitor());
        } catch (IOException e) {
            LogManager.logStackTrace("Error clearing temp directory at " + FileSystem.TMP, e);
        }
    }

    public void loadSystemTray() {
        if (SettingsManager.enableTrayIcon() && !App.skipTrayIntegration) {
            try {
                // Try to enable the tray icon.
                trySystemTrayIntegration();
            } catch (Exception e) {
                LogManager.logStackTrace(e);
            }
        }
    }

    /**
     * This tries to create the system tray menu.
     *
     * @throws Exception
     */
    private void trySystemTrayIntegration() throws Exception {
        App.trayMenu = new TrayMenu();

        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            TrayIcon trayIcon = new TrayIcon(Utils.getImage("/assets/image/Icon.png"));

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        App.trayMenu.setInvoker(App.trayMenu);
                        App.trayMenu.setLocation(e.getX(), e.getY());
                        App.trayMenu.setVisible(true);
                    }
                }
            });
            trayIcon.setToolTip(Constants.LAUNCHER_NAME);
            trayIcon.setImageAutoSize(true);

            tray.add(trayIcon);
        }
    }

    /**
     * This creates some integration files so the launcher can work with other applications by storing some properties
     * about itself and it's location in a set location.
     */
    public void integrate() {
        try {
            if (Files.notExists(OS.storagePath())) {
                Files.createDirectories(OS.storagePath());
            }

            Path config = OS.storagePath().resolve("atlauncher.conf");

            if (Files.notExists(config)) {
                Files.createFile(config);
            }

            Properties props = new Properties();
            props.load(new FileInputStream(config.toFile()));

            props.setProperty("java_version", Utils.getJavaVersion());
            props.setProperty("location", FileSystem.BASE_DIR.toString());
            props.setProperty("executable", new File(Update.class.getProtectionDomain().getCodeSource().getLocation()
                    .getPath()).getAbsolutePath());

            App.packCodeToAdd = props.getProperty("pack_code_to_add", null);
            props.remove("pack_code_to_add");

            App.packToInstall = props.getProperty("pack_to_install", null);
            props.remove("pack_to_install");

            App.packShareCodeToInstall = props.getProperty("pack_share_code_to_install", null);
            props.remove("pack_share_code_to_install");

            props.store(new FileOutputStream(config.toFile()), "");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (App.packCodeToAdd != null) {
            if (PackManager.addSemiPublicPack(App.packCodeToAdd)) {
                Pack packAdded = PackManager.getSemiPublicPackByCode(App.packCodeToAdd);
                if (packAdded != null) {
                    LogManager.info("The pack " + packAdded.getName() + " was automatically added to the launcher!");
                } else {
                    LogManager.error("Error automatically adding semi public pack with code of " + App.packCodeToAdd
                            + "!");
                }
            } else {
                LogManager.error("Error automatically adding semi public pack with code of " + App.packCodeToAdd + "!");
            }
        }
    }

    public void logInformation() {
        LogManager.info(Constants.LAUNCHER_NAME + " Version: " + Constants.VERSION);
        LogManager.info("Operating System: " + OS.getName());
        LogManager.info("RAM Available: " + Utils.getMaximumRam() + "MB");
        LogManager.info("Java Version: " + Utils.getActualJavaVersion());
        LogManager.info("Java Path: " + SettingsManager.getJavaPath());
        LogManager.info("64 Bit Java: " + Utils.is64Bit());
        LogManager.info("Launcher Directory: " + FileSystem.BASE_DIR.toString());
        LogManager.info("Using Theme: " + App.THEME);
    }

    public void setupOSXSpecificStuff() {
        if (OS.isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", Constants.LAUNCHER_NAME + " " +
                    Constants.VERSION);
            try {
                Class util = Class.forName("com.apple.eawt.Application");
                Method getApplication = util.getDeclaredMethod("getApplication");
                Object application = getApplication.invoke(util);
                Class params[] = new Class[]{Image.class};
                Method setDockIconImage = util.getDeclaredMethod("setDockIconImage", params);
                setDockIconImage.invoke(application, Utils.getImage("/assets/image/Icon.png"));
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public void autoLaunchInstance() {
        if (App.autoLaunch != null && InstanceManager.isInstanceBySafeName(App.autoLaunch)) {
            Instance instance = InstanceManager.getInstanceBySafeName(App.autoLaunch);
            LogManager.info("Opening Instance " + instance.getName());
            if (!instance.launch()) {
                App.autoLaunch = null;
                LogManager.error("Error Opening Instance " + instance.getName());
            }
        }
    }

    public void checkIfSetupIsComplete() {
        if (SettingsManager.isFirstTimeRun()) {
            LogManager.warn("Launcher not setup. Loading Setup Dialog");
            new SetupDialog();
        }
    }
   
    /**
     * FileFilter is a local implementation of java.io.FileFilter
     * in order to get rid of any possible memory leaks
     * that may occur as a result of using an anonymous class.
     * 
     * @author flaw600
     *
     */
    private static final class FileFilter implements java.io.FileFilter {
    	@Override
    	public boolean accept(File pathname) {
			// TODO Auto-generated method stub
			return !pathname.isHidden();
		}
    }
}