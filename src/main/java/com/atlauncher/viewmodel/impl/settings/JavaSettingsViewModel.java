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
import com.atlauncher.data.ScreenResolution;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.tabs.settings.JavaSettingsTab;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.SettingsValidityManager;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2022 / 06 / 16
 *        <p>
 *        View model for {@link JavaSettingsTab}
 */
public class JavaSettingsViewModel implements SettingsListener {
    private static final Logger LOG = LogManager.getLogger();
    private static final long javaPathCheckDelay = 2000;
    private static final long javaParamCheckDelay = 2000;
    private static final long javaInstallLocationCheckDelay = 2000;

    private final BehaviorSubject<Integer> _initialRam = BehaviorSubject.create(),
            _maxRam = BehaviorSubject.create(),
            _metaspace = BehaviorSubject.create(),
            _width = BehaviorSubject.create(),
            _height = BehaviorSubject.create();

    private final BehaviorSubject<String> _javaPath = BehaviorSubject.create(),
            _javaParams = BehaviorSubject.create(),
            _javaInstallLocation = BehaviorSubject.create();

    private final BehaviorSubject<Boolean> _maximizeMinecraft = BehaviorSubject.create(),
            _ignoreJavaOnInstanceLaunch = BehaviorSubject.create(),
            _useJavaProvidedByMinecraft = BehaviorSubject.create(),
            _disableLegacyLaunching = BehaviorSubject.create(),
            _useSystemGlfw = BehaviorSubject.create(),
            _useSystemOpenAl = BehaviorSubject.create(),
            _useDedicatedGpu = BehaviorSubject.create();

    private final BehaviorSubject<CheckState> javaPathCheckState = BehaviorSubject.create(),
            javaInstallLocationCheckState = BehaviorSubject.create(),
            javaParamCheckState = BehaviorSubject.create();

    private Integer systemRam = -1,
            recommendSize;

    private boolean initialMemoryWarningShown = false,
            maximumMemoryHalfWarningShown = false,
            maximumMemoryEightGBWarningShown = false,
            permgenWarningShown = false;
    private List<String> javaPaths;

    private long javaPathLastChange = 0;
    private boolean javaPathChanged = false;
    private final Thread javaPathCheckThread = new Thread(this::verifyJavaPath);

    private long javaInstallLocationLastChange = 0;
    private boolean javaInstallLocationChanged = false;
    private final Thread javaInstallLocationCheckThread = new Thread(this::verifyJavaInstallLocation);

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

    private void verifyJavaInstallLocation() {
        LOG.debug("Running javaInstallLocationCheckThread");
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                LOG.error("javaInstallLocationCheckThread : Failed to delay check thread", e);
            } finally {
                if (javaInstallLocationChanged) {
                    javaInstallLocationCheckState.onNext(CheckState.CheckPending);
                    if (javaInstallLocationLastChange + javaInstallLocationCheckDelay < System.currentTimeMillis()) {
                        // Prevent user from saving while checking
                        setJavaInstallLocationPending();
                        javaInstallLocationCheckState.onNext(CheckState.Checking);

                        File jPath = new File(App.settings.javaInstallLocation);
                        boolean valid = jPath.exists();
                        javaInstallLocationCheckState.onNext(new CheckState.Checked(valid));
                        javaInstallLocationChanged = false;
                        SettingsValidityManager.setValidity("javaInstallLocation", valid);

                        if (!valid) {
                            LOG.debug("javaInstallLocationCheckThread: Check thread reporting check fail");
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
                        boolean valid = (!useInitialMemoryOption() || !params.contains("-Xms")) &&
                                !params.contains("-Xmx") &&
                                !params.contains("-XX:PermSize") &&
                                !params.contains("-XX:MetaspaceSize");
                        javaParamCheckState.onNext(new CheckState.Checked(valid));
                        javaParamChanged = false;
                        SettingsValidityManager.setValidity("javaParam", valid);

                        if (!valid) {
                            LOG.debug("javaParamCheckThread: Check thread reporting check fail");
                        } else
                            SettingsManager.post();
                    }
                }
            }
        }
    }

    @Override
    public void onSettingsSaved() {
        _initialRam.onNext(App.settings.initialMemory);
        _maxRam.onNext(App.settings.maximumMemory);
        _metaspace.onNext(App.settings.metaspace);
        _width.onNext(App.settings.windowWidth);
        _height.onNext(App.settings.windowHeight);

        _javaPath.onNext(App.settings.javaPath);
        _javaParams.onNext(App.settings.javaParameters);
        _javaInstallLocation.onNext(Optional.ofNullable(App.settings.javaInstallLocation).orElse(""));

        _maximizeMinecraft.onNext(App.settings.maximiseMinecraft);
        _ignoreJavaOnInstanceLaunch.onNext(App.settings.ignoreJavaOnInstanceLaunch);
        _useJavaProvidedByMinecraft.onNext(App.settings.useJavaProvidedByMinecraft);
        _disableLegacyLaunching.onNext(App.settings.disableLegacyLaunching);
        _useSystemGlfw.onNext(App.settings.useSystemGlfw);
        _useSystemOpenAl.onNext(App.settings.useSystemOpenAl);

        if (OS.isLinux()) {
            _useDedicatedGpu.onNext(App.settings.useDedicatedGpu);
        }
    }

    /**
     * @return Is the current java 32 bit
     */
    public boolean isJava32Bit() {
        return !Java.is64Bit();
    }

    /**
     * @return total system ram
     */
    public Integer getSystemRam() {
        if (systemRam == -1) {
            int ram = OS.getSystemRam();
            systemRam = ram == 0 ? null : ram;
        }
        return systemRam;
    }

    public boolean isInitialMemoryWarningShown() {
        return initialMemoryWarningShown;
    }

    public void setInitialMemoryWarningShown() {
        initialMemoryWarningShown = true;
    }

    public boolean isMaximumMemoryHalfWarningShown() {
        return maximumMemoryHalfWarningShown;
    }

    public void setMaximumMemoryHalfWarningShown() {
        maximumMemoryHalfWarningShown = true;
    }

    public boolean isMaximumMemoryEightGBWarningShown() {
        return maximumMemoryEightGBWarningShown;
    }

    public void setMaximumMemoryEightGBWarningShown() {
        maximumMemoryEightGBWarningShown = true;
    }

    public boolean isPermgenWarningShown() {
        return permgenWarningShown;
    }

    public void setPermgenWarningShown() {
        permgenWarningShown = true;
    }

    /**
     * Set the initial ram
     *
     * @param initialRam initial ram value
     * @return true to show warning for above 512, false otherwise
     */
    public boolean setInitialRam(int initialRam) {
        App.settings.initialMemory = initialRam;

        // if initial memory is larger than maximum memory, make maximum memory match
        if (initialRam > App.settings.maximumMemory) {
            App.settings.maximumMemory = initialRam;
        }

        SettingsManager.post();

        return initialRam > 512 && !isInitialMemoryWarningShown();
    }

    public Observable<Integer> getInitialRam() {
        return _initialRam.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set the maximum ram
     *
     * @param maxRam max ram value
     */
    public void setMaxRam(int maxRam) {
        App.settings.maximumMemory = maxRam;
        // if initial memory is larger than maximum memory, make initial memory match
        if (useInitialMemoryOption() && App.settings.initialMemory > maxRam) {
            App.settings.initialMemory = maxRam;
        }

        SettingsManager.post();
    }

    public Observable<Integer> getMaxRam() {
        return _maxRam.observeOn(SwingSchedulers.edt());
    }

    public int getPermGenMaxRecommendSize() {
        if (recommendSize == null)
            recommendSize = OS.is64Bit() ? 256 : 128;

        return recommendSize;
    }

    /**
     * Set the perm gen size
     *
     * @param permGen perm gen size
     * @return true to show warning, false otherwise
     */
    public boolean setPermGen(int permGen) {
        App.settings.metaspace = permGen;

        SettingsManager.post();

        return permGen > getPermGenMaxRecommendSize() && !isPermgenWarningShown();
    }

    public Observable<Integer> getMetaspace() {
        return _metaspace.observeOn(SwingSchedulers.edt());
    }

    public Observable<Integer> getWidth() {
        return _width.observeOn(SwingSchedulers.edt());
    }

    public void setWidth(int width) {
        App.settings.windowWidth = width;
        SettingsManager.post();
    }

    public Observable<Integer> getHeight() {
        return _height.observeOn(SwingSchedulers.edt());
    }

    public void setHeight(int height) {
        App.settings.windowHeight = height;
        SettingsManager.post();
    }

    public List<ScreenResolution> getScreenResolutions() {
        return Arrays.stream(Constants.SCREEN_RESOLUTIONS)
                .filter((it) -> it.width <= OS.getMaximumWindowWidth() && it.height <= OS.getMaximumWindowHeight())
                .collect(Collectors.toList());
    }

    public void setScreenResolution(ScreenResolution resolution) {
        App.settings.windowWidth = resolution.width;
        App.settings.windowHeight = resolution.height;
        SettingsManager.post();
    }

    public List<String> getJavaPaths() {
        if (javaPaths == null)
            javaPaths = Java.getInstalledJavas().stream()
                    .map(javaInfo -> javaInfo.rootPath)
                    .collect(Collectors.toList());

        return javaPaths;
    }

    public void resetJavaPath() {
        App.settings.javaPath = OS.getDefaultJavaPath();
        javaPathLastChange = System.currentTimeMillis();
        javaPathChanged = true;
        SettingsManager.post();
    }

    public void setJavaPathPending() {
        SettingsValidityManager.setValidity("javaPath", false);
    }

    public String getJavaPath() {
        return App.settings.javaPath;
    }

    /**
     * Set the java path
     */
    public void setJavaPath(String path) {
        setJavaPathPending();
        App.settings.javaPath = path;
        App.settings.usingCustomJavaPath = !path.equalsIgnoreCase(OS.getDefaultJavaPath());
        javaPathLastChange = System.currentTimeMillis();
        javaPathChanged = true;
        if (!javaPathCheckThread.isAlive()) {
            javaPathCheckThread.start();
        }
    }

    public Observable<String> getJavaPathObservable() {
        return _javaPath.observeOn(SwingSchedulers.edt());
    }

    public Observable<CheckState> getJavaPathChecker() {
        return javaPathCheckState.observeOn(SwingSchedulers.edt());
    }

    public void resetJavaParams() {
        App.settings.javaParameters = Constants.DEFAULT_JAVA_PARAMETERS;
        javaParamLastChange = System.currentTimeMillis();
        javaParamChanged = true;
        SettingsManager.post();
    }

    public void setJavaParamsPending() {
        SettingsValidityManager.setValidity("javaParam", false);
    }

    public Observable<String> getJavaParams() {
        return _javaParams.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set the java params
     */
    public void setJavaParams(String params) {
        App.settings.javaParameters = params;
        javaParamLastChange = System.currentTimeMillis();
        javaParamChanged = true;
        if (!javaParamCheckThread.isAlive()) {
            javaParamCheckThread.start();
        }
    }

    public Observable<CheckState> getJavaParamsChecker() {
        return javaParamCheckState.observeOn(SwingSchedulers.edt());
    }

    public void setStartMinecraftMax(Boolean b) {
        App.settings.maximiseMinecraft = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getMaximizeMinecraft() {
        return _maximizeMinecraft.observeOn(SwingSchedulers.edt());
    }

    public void setIgnoreJavaChecks(Boolean b) {
        App.settings.ignoreJavaOnInstanceLaunch = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getIgnoreJavaOnInstanceLaunch() {
        return _ignoreJavaOnInstanceLaunch.observeOn(SwingSchedulers.edt());
    }

    public boolean getUseJavaFromMinecraftEnabled() {
        return !OS.isArm() || OS.isMacArm();
    }

    public void setJavaFromMinecraft(Boolean b) {
        App.settings.useJavaProvidedByMinecraft = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getUseJavaProvidedByMinecraft() {
        return _useJavaProvidedByMinecraft.observeOn(SwingSchedulers.edt());
    }

    public Observable<Boolean> getDisableLegacyLaunching() {
        return _disableLegacyLaunching.observeOn(SwingSchedulers.edt());
    }

    public void setDisableLegacyLaunching(Boolean b) {
        App.settings.disableLegacyLaunching = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getSystemGLFW() {
        return _useSystemGlfw.observeOn(SwingSchedulers.edt());
    }

    public void setSystemGLFW(Boolean b) {
        App.settings.useSystemGlfw = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getSystemOpenAL() {
        return _useSystemOpenAl.observeOn(SwingSchedulers.edt());
    }

    public void setSystemOpenAL(Boolean b) {
        App.settings.useSystemOpenAl = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getDedicatedGpu() {
        return _useDedicatedGpu.observeOn(SwingSchedulers.edt());
    }

    public void setDedicatedGpu(Boolean b) {
        App.settings.useDedicatedGpu = b;
        SettingsManager.post();
    }

    public Boolean useInitialMemoryOption() {
        return !ConfigManager.getConfigItem("removeInitialMemoryOption", false);
    }

    public void setJavaInstallLocationPending() {
        SettingsValidityManager.setValidity("javaInstallLocation", false);
    }

    public Observable<CheckState> getJavaInstallLocationChecker() {
        return javaInstallLocationCheckState.observeOn(SwingSchedulers.edt());
    }

    public Observable<String> getJavaInstallLocation() {
        return _javaInstallLocation.observeOn(SwingSchedulers.edt());
    }

    public Observable<String> getJavaInstallLocationObservable() {
        return _javaInstallLocation.observeOn(SwingSchedulers.edt());
    }

    public void setJavaInstallLocation(@NotNull String path) {
        if (!path.isEmpty()) {
            setJavaInstallLocationPending();
            App.settings.javaInstallLocation = path;
            javaInstallLocationLastChange = System.currentTimeMillis();
            javaInstallLocationChanged = true;
            if (!javaInstallLocationCheckThread.isAlive()) {
                javaInstallLocationCheckThread.start();
            }
        } else {
            App.settings.javaInstallLocation = null;
            javaInstallLocationCheckState.onNext(new CheckState.Checked(true));
            SettingsValidityManager.setValidity("javaInstallLocation", true);
            SettingsManager.post();
        }
    }
}