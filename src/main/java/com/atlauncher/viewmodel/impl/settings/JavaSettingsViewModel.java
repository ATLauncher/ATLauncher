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
        _initialRam = BehaviorSubject.create(),
        _maxRam = BehaviorSubject.create(),
        _permGen = BehaviorSubject.create(),
        _width = BehaviorSubject.create(),
        _height = BehaviorSubject.create();

    private final BehaviorSubject<String>
        _javaPath = BehaviorSubject.create(),
        _javaParams = BehaviorSubject.create(),
        baseJavaInstallFolder = BehaviorSubject.create();

    private final BehaviorSubject<Boolean>
        _startMinecraftMax = BehaviorSubject.create(),
        _ignoreJavaChecks = BehaviorSubject.create(),
        _javaFromMinecraft = BehaviorSubject.create(),
        _disableLegacyLaunching = BehaviorSubject.create(),
        _systemGLFW = BehaviorSubject.create(),
        _systemOpenAL = BehaviorSubject.create();

    private final BehaviorSubject<CheckState>
        javaPathCheckState = BehaviorSubject.create(),
        javaParamCheckState = BehaviorSubject.create();

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
                    javaPathCheckState.onNext(CheckState.CheckPending);
                    if (javaPathLastChange + javaPathCheckDelay < System.currentTimeMillis()) {
                        // Prevent user from saving while checking
                        setJavaPathPending();
                        javaPathCheckState.onNext(CheckState.Checking);

                        File jPath = new File(App.settings.javaPath, "bin");
                        boolean valid = jPath.exists();
                        javaPathCheckState.onNext(new CheckState.Checked(valid));
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
                    javaParamCheckState.onNext(CheckState.CheckPending);
                    if (javaParamLastChange + javaParamCheckDelay < System.currentTimeMillis()) {
                        // Prevent user from saving while checking
                        setJavaParamsPending();
                        javaParamCheckState.onNext(CheckState.Checking);

                        String params = App.settings.javaParameters;
                        boolean valid =
                            (useInitialMemoryOption() || !params.contains("-Xms")) &&
                            !params.contains("-Xmx") &&
                            !params.contains("-XX:PermSize") &&
                            !params.contains("-XX:MetaspaceSize");
                        javaParamCheckState.onNext(new CheckState.Checked(valid));
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
        _initialRam.onNext(App.settings.initialMemory);
        _maxRam.onNext(App.settings.maximumMemory);
        _permGen.onNext(App.settings.metaspace);
        _width.onNext(App.settings.windowWidth);
        _height.onNext(App.settings.windowHeight);

        _javaPath.onNext(App.settings.javaPath);
        _javaParams.onNext(App.settings.javaParameters);
        baseJavaInstallFolder.onNext(Optional.ofNullable(App.settings.baseJavaInstallFolder).orElse(""));

        _startMinecraftMax.onNext(App.settings.maximiseMinecraft);
        _ignoreJavaChecks.onNext(App.settings.ignoreJavaOnInstanceLaunch);
        _javaFromMinecraft.onNext(App.settings.useJavaProvidedByMinecraft);
        _disableLegacyLaunching.onNext(App.settings.disableLegacyLaunching);
        _systemGLFW.onNext(App.settings.useSystemGlfw);
        _systemOpenAL.onNext(App.settings.useSystemOpenAl);
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
        return _initialRam.observeOn(SwingSchedulers.edt());
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
        return _maxRam.observeOn(SwingSchedulers.edt());
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
        return _permGen.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setWidth(int width) {
        App.settings.windowWidth = width;
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> getWidth() {
        return _width.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setHeight(int height) {
        App.settings.windowHeight = height;
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> getHeight() {
        return _height.observeOn(SwingSchedulers.edt());
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
        return _javaPath.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<CheckState> getJavaPathChecker() {
        return javaPathCheckState.observeOn(SwingSchedulers.edt());
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
        return _javaParams.observeOn(SwingSchedulers.edt());
    }

    @Override
    public Observable<CheckState> getJavaParamsChecker() {
        return javaParamCheckState.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setStartMinecraftMax(Boolean b) {
        App.settings.maximiseMinecraft = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> get5StartMinecraftMax() {
        return _startMinecraftMax.observeOn(SwingSchedulers.edt());
    }


    @Override
    public void setIgnoreJavaChecks(Boolean b) {
        App.settings.ignoreJavaOnInstanceLaunch = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getIgnoreJavaChecks() {
        return _ignoreJavaChecks.observeOn(SwingSchedulers.edt());
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
        return _javaFromMinecraft.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setDisableLegacyLaunching(Boolean b) {
        App.settings.disableLegacyLaunching = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getDisableLegacyLaunching() {
        return _disableLegacyLaunching.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setSystemGLFW(Boolean b) {
        App.settings.useSystemGlfw = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getSystemGLFW() {
        return _systemGLFW.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setSystemOpenAL(Boolean b) {
        App.settings.useSystemOpenAl = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getSystemOpenAL() {
        return _systemOpenAL.observeOn(SwingSchedulers.edt());
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