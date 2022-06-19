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
package com.atlauncher.gui.tabs.settings;

import com.atlauncher.App;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.CheckState;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.evnt.manager.SettingsValidityManager;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 16 / 06 / 2022
 */
public class JavaSettingsViewModel implements IJavaSettingsViewModel {
    private Consumer<Integer>
        _addOnInitialRamChanged,
        _addOnMaxRamChanged,
        _addOnPermGenChanged,
        _addOnWidthChanged,
        _addOnHeightChanged;

    private Consumer<String>
        _addOnJavaPathChanged,
        _addOnJavaParamsChanged;

    private Consumer<Boolean>
        _addOnStartMinecraftMaxChanged,
        _addOnIgnoreJavaChecksChanged,
        _addOnJavaFromMinecraftChanged,
        _addOnDisableLegacyLaunchingChanged,
        _addOnSystemGLFWChanged,
        _addOnSystemOpenALChanged;

    @Override
    public void onSettingsSaved() {
        _addOnInitialRamChanged.accept(App.settings.initialMemory);
        _addOnMaxRamChanged.accept(App.settings.maximumMemory);
        _addOnPermGenChanged.accept(App.settings.metaspace);
        _addOnWidthChanged.accept(App.settings.windowWidth);
        _addOnHeightChanged.accept(App.settings.windowHeight);

        _addOnJavaPathChanged.accept(App.settings.javaPath);
        _addOnJavaParamsChanged.accept(App.settings.javaParameters);

        _addOnStartMinecraftMaxChanged.accept(App.settings.maximiseMinecraft);
        _addOnIgnoreJavaChecksChanged.accept(App.settings.ignoreJavaOnInstanceLaunch);
        _addOnJavaFromMinecraftChanged.accept(App.settings.useJavaProvidedByMinecraft);
        _addOnDisableLegacyLaunchingChanged.accept(App.settings.disableLegacyLaunching);
        _addOnSystemGLFWChanged.accept(App.settings.useSystemGlfw);
        _addOnSystemOpenALChanged.accept(App.settings.useSystemOpenAl);
    }

    public JavaSettingsViewModel() {
        SettingsManager.addListener(this);
    }

    @Override
    public boolean isJava32Bit() {
        return !Java.is64Bit();
    }

    private Integer systemRam = -1;

    @Override
    public Integer getSystemRam() {
        if (systemRam == -1) {
            int ram = OS.getSystemRam();
            systemRam = ram == 0 ? null : ram;
        }
        return systemRam;
    }

    private boolean initialMemoryWarningShown = false;
    private boolean maximumMemoryHalfWarningShown = false;
    private boolean maximumMemoryEightGBWarningShown = false;
    private boolean permgenWarningShown = false;

    @Override
    public boolean isInitialMemoryWarningShown() {
        return initialMemoryWarningShown;
    }

    @Override
    public void setInitialMemoryWarningShown() {
        initialMemoryWarningShown = true;
    }

    @Override
    public boolean isMaximumMemoryHalfWarningShown() {
        return maximumMemoryHalfWarningShown;
    }

    @Override
    public void setMaximumMemoryHalfWarningShown() {
        maximumMemoryHalfWarningShown = true;
    }

    @Override
    public boolean isMaximumMemoryEightGBWarningShown() {
        return maximumMemoryEightGBWarningShown;
    }

    @Override
    public void setMaximumMemoryEightGBWarningShown() {
        maximumMemoryEightGBWarningShown = true;
    }

    @Override
    public boolean isPermgenWarningShown() {
        return permgenWarningShown;
    }

    @Override
    public void setPermgenWarningShown() {
        permgenWarningShown = true;
    }

    @Override
    public boolean setInitialRam(int initialRam) {
        App.settings.initialMemory = initialRam;

        // if initial memory is larger than maximum memory, make maximum memory match
        if (initialRam > App.settings.maximumMemory) {
            App.settings.maximumMemory = initialRam;
        }

        SettingsManager.post();

        return initialRam > 512 && !isInitialMemoryWarningShown();
    }

    @Override
    public void addOnInitialRamChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.initialMemory);
        _addOnInitialRamChanged = onChanged;
    }

    @Override
    public MaxRamWarning setMaxRam(int maxRam) {
        App.settings.maximumMemory = maxRam;
        // if initial memory is larger than maximum memory, make initial memory match
        if (App.settings.initialMemory > maxRam) {
            App.settings.initialMemory = maxRam;
        }

        SettingsManager.post();

        if (maxRam > 8192 && !isMaximumMemoryEightGBWarningShown())
            return MaxRamWarning.ABOVE_8GB;

        if ((OS.getMaximumRam() != 0 && OS.getMaximumRam() < 16384)
            && maxRam > (OS.getMaximumRam() / 2)
            && !isMaximumMemoryHalfWarningShown())
            return MaxRamWarning.ABOVE_HALF;

        return null;
    }

    @Override
    public void addOnMaxRamChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.maximumMemory);
        _addOnMaxRamChanged = onChanged;
    }


    private Integer recommendSize;

    @Override
    public int getPermGenMaxRecommendSize() {
        if (recommendSize == null)
            recommendSize = OS.is64Bit() ? 256 : 128;

        return recommendSize;
    }

    @Override
    public boolean setPermGen(int permGen) {
        App.settings.metaspace = permGen;

        SettingsManager.post();

        return permGen > getPermGenMaxRecommendSize() && !isPermgenWarningShown();
    }

    @Override
    public void addOnPermGenChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.metaspace);
        _addOnPermGenChanged = onChanged;
    }

    @Override
    public void setWidth(int width) {
        App.settings.windowWidth = width;
        SettingsManager.post();
    }

    @Override
    public void addOnWidthChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.windowWidth);
        _addOnWidthChanged = onChanged;
    }

    @Override
    public void setHeight(int height) {
        App.settings.windowHeight = height;
        SettingsManager.post();
    }

    @Override
    public void addOnHeightChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.windowHeight);
        _addOnHeightChanged = onChanged;
    }

    @Override
    public List<Constants.ScreenResolution> getScreenResolutions() {
        return Arrays.stream(Constants.SCREEN_RESOLUTIONS)
            .filter(screenResolution -> OS.getMaximumWindowWidth() >= screenResolution.width
                && OS.getMaximumWindowHeight() >= screenResolution.height)
            .collect(Collectors.toList());
    }

    @Override
    public void setScreenResolution(Constants.ScreenResolution resolution) {
        App.settings.windowWidth = resolution.width;
        App.settings.windowHeight = resolution.height;
        SettingsManager.post();
    }

    private List<String> javaPaths;

    @Override
    public List<String> getJavaPaths() {
        if (javaPaths == null)
            javaPaths = Java.getInstalledJavas().stream()
                .map(javaInfo -> javaInfo.rootPath)
                .collect(Collectors.toList());

        return javaPaths;
    }

    @Override
    public void resetJavaPath() {
        App.settings.javaPath = OS.getDefaultJavaPath();
        javaPathLastChange = System.currentTimeMillis();
        javaPathChanged = true;
        SettingsManager.post();
    }

    private long javaPathLastChange = 0;
    private boolean javaPathChanged = false;
    private static final long javaPathCheckDelay = 2000;
    private Consumer<CheckState> javaPathCheckStateConsumer;
    private final Thread javaPathCheckThread = new Thread(() -> {
        LOG.debug("Running javaParamCheckThread");
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                LOG.error("javaParamCheckThread : Failed to delay check thread", e);
            } finally {
                if (javaPathChanged) {
                    javaPathCheckStateConsumer.accept(new CheckState.CheckPending());
                    if (javaPathLastChange + javaPathCheckDelay < System.currentTimeMillis()) {
                        // Prevent user from saving while checking
                        SettingsValidityManager.post("javaPath", false);
                        javaPathCheckStateConsumer.accept(new CheckState.Checking());

                        File jPath = new File(App.settings.javaPath, "bin");
                        boolean valid = jPath.exists();
                        javaPathCheckStateConsumer.accept(new CheckState.Checked(valid));
                        javaPathChanged = false;
                        SettingsValidityManager.post("javaPath", valid);

                        if (!valid) {
                            LOG.debug("javaParamCheckThread: Check thread reporting check fail");
                        }
                    }
                }
            }
        }
    });

    @Override
    public void setJavaPath(String path) {
        App.settings.javaPath = path;
        App.settings.usingCustomJavaPath =
            !path.equalsIgnoreCase(OS.getDefaultJavaPath());
        javaPathLastChange = System.currentTimeMillis();
        javaPathChanged = true;
        if (!javaPathCheckThread.isAlive())
            javaPathCheckThread.start();
        SettingsManager.post();
    }

    @Override
    public String getJavaPath() {
        return App.settings.javaPath;
    }

    @Override
    public void addOnJavaPathCheckerListener(Consumer<CheckState> consumer) {
        consumer.accept(new CheckState.NotChecking());
        javaPathCheckStateConsumer = consumer;
    }

    @Override
    public void addOnJavaPathChanged(Consumer<String> onChanged) {
        onChanged.accept(getJavaPath());
        _addOnJavaPathChanged = onChanged;
    }

    private static final Logger LOG = LogManager.getLogger();

    private long javaParamLastChange = 0;
    private boolean javaParamChanged = false;
    private static final long javaParamCheckDelay = 2000;
    private Consumer<CheckState> javaParamCheckStateConsumer;
    private final Thread javaParamCheckThread = new Thread(() -> {
        LOG.debug("Running javaParamCheckThread");
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                LOG.error("javaParamCheckThread : Failed to delay check thread", e);
            } finally {
                if (javaParamChanged) {
                    javaParamCheckStateConsumer.accept(new CheckState.CheckPending());
                    if (javaParamLastChange + javaParamCheckDelay < System.currentTimeMillis()) {
                        // Prevent user from saving while checking
                        SettingsValidityManager.post("javaParam", false);
                        javaParamCheckStateConsumer.accept(new CheckState.Checking());

                        String params = App.settings.javaParameters;
                        boolean valid = !(params.contains("-Xms") || params.contains("-Xmx")
                            || params.contains("-XX:PermSize")
                            || params.contains("-XX:MetaspaceSize"));
                        javaParamCheckStateConsumer.accept(new CheckState.Checked(valid));
                        javaParamChanged = false;
                        SettingsValidityManager.post("javaParam", valid);

                        if (!valid) {
                            LOG.debug("javaParamCheckThread: Check thread reporting check fail");
                        }
                    }
                }
            }
        }
    });

    @Override
    public void addOnJavaParamsCheckerListener(Consumer<CheckState> consumer) {
        consumer.accept(new CheckState.NotChecking());
        javaParamCheckStateConsumer = consumer;
    }

    @Override
    public void resetJavaParams() {
        App.settings.javaParameters = Constants.DEFAULT_JAVA_PARAMETERS;
        javaParamLastChange = System.currentTimeMillis();
        javaParamChanged = true;
        SettingsManager.post();
    }

    @Override
    public void setJavaParams(String params) {
        App.settings.javaParameters = params;
        javaParamLastChange = System.currentTimeMillis();
        javaParamChanged = true;
        if (!javaParamCheckThread.isAlive())
            javaParamCheckThread.start();
        SettingsManager.post();
    }

    @Override
    public void addOnJavaParamsChanged(Consumer<String> onChanged) {
        onChanged.accept(App.settings.javaParameters);
        _addOnJavaParamsChanged = onChanged;
    }

    @Override
    public void setStartMinecraftMax(Boolean b) {
        App.settings.maximiseMinecraft = b;
        SettingsManager.post();
    }

    @Override
    public void addOnStartMinecraftMaxChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.maximiseMinecraft);
        _addOnStartMinecraftMaxChanged = onChanged;
    }


    @Override
    public void setIgnoreJavaChecks(Boolean b) {
        App.settings.ignoreJavaOnInstanceLaunch = b;
        SettingsManager.post();
    }

    @Override
    public void addOnIgnoreJavaChecksChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.ignoreJavaOnInstanceLaunch);
        _addOnIgnoreJavaChecksChanged = onChanged;
    }

    @Override
    public boolean getUseJavaFromMinecraftEnabled() {
        return !OS.isArm() || OS.isMacArm();
    }

    @Override
    public void setJavaFromMinecraft(Boolean b) {
        App.settings.useJavaProvidedByMinecraft = b;
        SettingsManager.post();
    }

    @Override
    public void addOnJavaFromMinecraftChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.useJavaProvidedByMinecraft);
        _addOnJavaFromMinecraftChanged = onChanged;
    }

    @Override
    public void setDisableLegacyLaunching(Boolean b) {
        App.settings.disableLegacyLaunching = b;
        SettingsManager.post();
    }

    @Override
    public void addOnDisableLegacyLaunchingChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.disableLegacyLaunching);
        _addOnDisableLegacyLaunchingChanged = onChanged;
    }

    @Override
    public void setSystemGLFW(Boolean b) {
        App.settings.useSystemGlfw = b;
        SettingsManager.post();
    }

    @Override
    public void addOnSystemGLFWChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.useSystemGlfw);
        _addOnSystemGLFWChanged = onChanged;
    }

    @Override
    public void setSystemOpenAL(Boolean b) {
        App.settings.useSystemOpenAl = b;
        SettingsManager.post();
    }

    @Override
    public void addOnSystemOpenALChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.useSystemOpenAl);
        _addOnSystemOpenALChanged = onChanged;
    }
}
