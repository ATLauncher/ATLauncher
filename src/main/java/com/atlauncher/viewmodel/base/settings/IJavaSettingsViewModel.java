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
package com.atlauncher.viewmodel.base.settings;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlauncher.data.CheckState;
import com.atlauncher.data.MaxRamWarning;
import com.atlauncher.data.ScreenResolution;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.gui.tabs.settings.JavaSettingsTab;

import io.reactivex.rxjava3.core.Observable;

/**
 * @since 2022 / 06 / 15
 * <p>
 * View model for {@link JavaSettingsTab}
 */
public interface IJavaSettingsViewModel extends SettingsListener {
    /**
     * @return Is the current java 32 bit
     */
    boolean isJava32Bit();

    /**
     * @return total system ram
     */
    Integer getSystemRam();

    boolean isInitialMemoryWarningShown();

    void setInitialMemoryWarningShown();


    boolean isMaximumMemoryHalfWarningShown();

    void setMaximumMemoryHalfWarningShown();


    boolean isMaximumMemoryEightGBWarningShown();

    void setMaximumMemoryEightGBWarningShown();


    boolean isPermgenWarningShown();

    void setPermgenWarningShown();


    /**
     * Set the initial ram
     *
     * @param initialRam initial ram value
     * @return true to show warning for above 512, false otherwise
     */
    boolean setInitialRam(int initialRam);

    Observable<Integer> getInitialRam();


    /**
     * Set the maximum ram
     *
     * @param maxRam max ram value
     * @return null if no warning, else handle the warning
     */
    @Nullable
    MaxRamWarning setMaxRam(int maxRam);

    Observable<Integer> getMaxRam();

    int getPermGenMaxRecommendSize();

    /**
     * Set the perm gen size
     *
     * @param permGen perm gen size
     * @return true to show warning, false otherwise
     */
    boolean setPermGen(int permGen);

    Observable<Integer> getPermGen();

    void setWidth(int width);

    Observable<Integer> getWidth();

    void setHeight(int height);

    Observable<Integer> getHeight();

    List<ScreenResolution> getScreenResolutions();

    void setScreenResolution(ScreenResolution resolution);

    List<String> getJavaPaths();

    void resetJavaPath();

    void setJavaPathPending();

    String getJavaPath();

    /**
     * Set the java path
     */
    void setJavaPath(String path);

    Observable<String> getJavaPathObservable();

    Observable<CheckState> getJavaPathChecker();

    void resetJavaParams();

    /**
     * Set the java params
     */
    void setJavaParams(String params);

    void setJavaParamsPending();

    Observable<String> getJavaParams();

    Observable<CheckState> getJavaParamsChecker();

    void setStartMinecraftMax(Boolean b);

    Observable<Boolean> get5StartMinecraftMax();

    void setIgnoreJavaChecks(Boolean b);

    Observable<Boolean> getIgnoreJavaChecks();

    boolean getUseJavaFromMinecraftEnabled();

    void setJavaFromMinecraft(Boolean b);

    Observable<Boolean> getJavaFromMinecraft();

    void setDisableLegacyLaunching(Boolean b);

    Observable<Boolean> getDisableLegacyLaunching();

    void setSystemGLFW(Boolean b);

    Observable<Boolean> getSystemGLFW();

    void setSystemOpenAL(Boolean b);

    Observable<Boolean> getSystemOpenAL();

    Boolean useInitialMemoryOption();

    void resetBaseInstallFolder();

    Observable<String> getBaseInstallFolder();

    void setBaseInstallFolder(@Nonnull String path);
}