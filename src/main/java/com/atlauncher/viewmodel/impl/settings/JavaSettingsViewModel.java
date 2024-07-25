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
package com.atlauncher.viewmodel.impl.settings;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.atlauncher.App;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.CheckState;
import com.atlauncher.data.MaxRamWarning;
import com.atlauncher.data.ScreenResolution;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.SettingsValidityManager;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.viewmodel.base.settings.IJavaSettingsViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2022 / 06 / 16
 */
public class JavaSettingsViewModel implements IJavaSettingsViewModel {
    private static final long javaPathCheckDelay = 2000;
    private static final Logger LOG = LogManager.getLogger();
    private static final long javaParamCheckDelay = 2000;

    private final BehaviorSubject<Integer>
        _addOnInitialRamChanged = BehaviorSubject.create(),
        _addOnMaxRamChanged = BehaviorSubject.create(),
        _addOnPermGenChanged = BehaviorSubject.create(),
        _addOnWidthChanged = BehaviorSubject.create(),
        _addOnHeightChanged = BehaviorSubject.create();

    private final BehaviorSubject<String>
        _addOnJavaPathChanged = BehaviorSubject.create(),
        _addOnJavaParamsChanged = BehaviorSubject.create(),
        baseJavaInstallFolder = BehaviorSubject.create();

    private final BehaviorSubject<Boolean>
        _addOnStartMinecraftMaxChanged = BehaviorSubject.create(),
        _addOnIgnoreJavaChecksChanged = BehaviorSubject.create(),
        _addOnJavaFromMinecraftChanged = BehaviorSubject.create(),
        _addOnDisableLegacyLaunchingChanged = BehaviorSubject.create(),
        _addOnSystemGLFWChanged = BehaviorSubject.create(),
        _addOnSystemOpenALChanged = BehaviorSubject.create();

    private final BehaviorSubject<CheckState>
        javaPathCheckStateConsumer = BehaviorSubject.create(),
        javaParamCheckStateConsumer = BehaviorSubject.create();

    private Integer
        systemRam = -1,
        recommendSize;

    private boolean
        initialMemoryWarningShown = false,
        maximumMemoryHalfWarningShown = false,
        maximumMemoryEightGBWarningShown = false,
        permgenWarningShown = false;
    private List<String> javaPaths;

    private long javaPathLastChange = 0;
    private boolean javaPathChanged = false;
    private final Thread javaPathCheckThread = new Thread(this::verifyJavaPath);

    private long javaParamLastChange = 0;
    private boolean javaParamChanged = false;
    private final Thread javaParamCheckThread = new Thread(this::verifyJavaParam);

    public JavaSettingsViewModel() {
        onSettingsSaved();
        SettingsManager.addListener(this);
    }

    private void verifyJavaPath() {
        LOG.debug("Running javaPathCheckThread");
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                LOG.error("javaParamCheckThread : Failed to delay check thread", e);
            } finally {
                if (javaPathChanged) {
                    javaPathCheckStateConsumer.onNext(CheckState.CheckPending);
                    if (javaPathLastChange + javaPathCheckDelay < System.currentTimeMillis()) {
                        // Prevent user from saving while checking
                        setJavaPathPending();
                        javaPathCheckStateConsumer.onNext(CheckState.Checking);

                        File jPath = new File(App.settings.javaPath, "bin");
                        boolean valid = jPath.exists();
                        javaPathCheckStateConsumer.onNext(new CheckState.Checked(valid));
                        javaPathChanged = false;
                        SettingsValidityManager.setValidity("javaPath", valid);

                        if (!valid) {
                            LOG.debug("javaPathCheckThread: Check thread reporting check fail");
                        } else {
                            SettingsManager.post();
                        }
                    }
                }
            }
        }
    }

    private void verifyJavaParam() {
        LOG.debug("Running javaParamCheckThread");
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                LOG.error("javaParamCheckThread : Failed to delay check thread", e);
            } finally {
                if (javaParamChanged) {
                    javaParamCheckStateConsumer.onNext(CheckState.CheckPending);
                    if (javaParamLastChange + javaParamCheckDelay < System.currentTimeMillis()) {
                        // Prevent user from saving while checking
                        setJavaParamsPending();
                        javaParamCheckStateConsumer.onNext(CheckState.Checking);

                        String params = App.settings.javaParameters;
                        boolean valid =
                            (useInitialMemoryOption() || !params.contains("-Xms")) &&
                            !params.contains("-Xmx") &&
                            !params.contains("-XX:PermSize") &&
                            !params.contains("-XX:MetaspaceSize");
                        javaParamCheckStateConsumer.onNext(new CheckState.Checked(valid));
                        javaParamChanged = false;
                        SettingsValidityManager.setValidity("javaParam", valid);

                        if (!valid) {
                            LOG.debug("javaParamCheckThread: Check thread reporting check fail");
                        } else SettingsManager.post();
                    }
                }
            }
        }
    }

    @Override
    public void onSettingsSaved() {
        _addOnInitialRamChanged.onNext(App.settings.initialMemory);
        _addOnMaxRamChanged.onNext(App.settings.maximumMemory);
        _addOnPermGenChanged.onNext(App.settings.metaspace);
        _addOnWidthChanged.onNext(App.settings.windowWidth);
        _addOnHeightChanged.onNext(App.settings.windowHeight);

        _addOnJavaPathChanged.onNext(App.settings.javaPath);
        _addOnJavaParamsChanged.onNext(App.settings.javaParameters);
        baseJavaInstallFolder.onNext(Optional.ofNullable(App.settings.baseJavaInstallFolder).orElse(""));

        _addOnStartMinecraftMaxChanged.onNext(App.settings.maximiseMinecraft);
        _addOnIgnoreJavaChecksChanged.onNext(App.settings.ignoreJavaOnInstanceLaunch);
        _addOnJavaFromMinecraftChanged.onNext(App.settings.useJavaProvidedByMinecraft);
        _addOnDisableLegacyLaunchingChanged.onNext(App.settings.disableLegacyLaunching);
        _addOnSystemGLFWChanged.onNext(App.settings.useSystemGlfw);
        _addOnSystemOpenALChanged.onNext(App.settings.useSystemOpenAl);
    }

    @Override
    public boolean isJava32Bit() {
        return !Java.is64Bit();
    }

    @Override
    public Integer getSystemRam() {
        if (systemRam == -1) {
            int ram = OS.getSystemRam();
            systemRam = ram == 0 ? null : ram;
        }
        return systemRam;
    }

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
    public Observable<Integer> getInitialRam() {
        return _addOnInitialRamChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public MaxRamWarning setMaxRam(int maxRam) {
        App.settings.maximumMemory = maxRam;
        // if initial memory is larger than maximum memory, make initial memory match
        if (useInitialMemoryOption() && App.settings.initialMemory > maxRam) {
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
    public Observable<Integer> getMaxRam() {
        return _addOnMaxRamChanged.observeOn(SwingSchedulers.edt());
    }

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
    public Observable<Integer> getPermGen() {
        return _addOnPermGenChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setWidth(int width) {
        App.settings.windowWidth = width;
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> getWidth() {
        return _addOnWidthChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setHeight(int height) {
        App.settings.windowHeight = height;
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> getHeight() {
        return _addOnHeightChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public List<ScreenResolution> getScreenResolutions() {
        return Arrays.stream(Constants.SCREEN_RESOLUTIONS)
            .filter((it) -> it.width <= OS.getMaximumWindowWidth() && it.height <= OS.getMaximumWindowHeight())
            .collect(Collectors.toList());
    }

    @Override
    public void setScreenResolution(ScreenResolution resolution) {
        App.settings.windowWidth = resolution.width;
        App.settings.windowHeight = resolution.height;
        SettingsManager.post();
    }

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

    @Override
    public void setJavaPathPending() {
        SettingsValidityManager.setValidity("javaPath", false);
    }

    @Override
    public String getJavaPath() {
        return App.settings.javaPath;
    }

    @Override
    public void setJavaPath(String path) {
        setJavaPathPending();
        App.settings.javaPath = path;
        App.settings.usingCustomJavaPath =
            !path.equalsIgnoreCase(OS.getDefaultJavaPath());
        javaPathLastChange = System.currentTimeMillis();
        javaPathChanged = true;
        if (!javaPathCheckThread.isAlive())
            javaPathCheckThread.start();
    }

    @Override
    public Observable<String> getJavaPathObservable() {
        return _addOnJavaPathChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<CheckState> getJavaPathChecker() {
        return javaPathCheckStateConsumer.observeOn(SwingSchedulers.edt());
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
    }

    @Override
    public void setJavaParamsPending() {
        SettingsValidityManager.setValidity("javaParam", false);
    }

    @Override
    public Observable<String> getJavaParams() {
        return _addOnJavaParamsChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<CheckState> getJavaParamsChecker() {
        return javaParamCheckStateConsumer.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setStartMinecraftMax(Boolean b) {
        App.settings.maximiseMinecraft = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> get5StartMinecraftMax() {
        return _addOnStartMinecraftMaxChanged.observeOn(SwingSchedulers.edt());
    }


    @Override
    public void setIgnoreJavaChecks(Boolean b) {
        App.settings.ignoreJavaOnInstanceLaunch = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getIgnoreJavaChecks() {
        return _addOnIgnoreJavaChecksChanged.observeOn(SwingSchedulers.edt());
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
    public Observable<Boolean> getJavaFromMinecraft() {
        return _addOnJavaFromMinecraftChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setDisableLegacyLaunching(Boolean b) {
        App.settings.disableLegacyLaunching = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getDisableLegacyLaunching() {
        return _addOnDisableLegacyLaunchingChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setSystemGLFW(Boolean b) {
        App.settings.useSystemGlfw = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getSystemGLFW() {
        return _addOnSystemGLFWChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setSystemOpenAL(Boolean b) {
        App.settings.useSystemOpenAl = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getSystemOpenAL() {
        return _addOnSystemOpenALChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Boolean useInitialMemoryOption() {
        return !ConfigManager.getConfigItem("removeInitialMemoryOption", false);
    }

    @Override
    public void resetBaseInstallFolder() {
        App.settings.baseJavaInstallFolder = null;
        SettingsManager.post();
    }

    @Override
    public Observable<String> getBaseInstallFolder() {
        return baseJavaInstallFolder.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setBaseInstallFolder(@NotNull String path) {
        if (!path.isEmpty()) {
            App.settings.baseJavaInstallFolder = path;
        } else {
            App.settings.baseJavaInstallFolder = null;
        }
        SettingsManager.post();
    }
}