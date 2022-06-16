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

import com.atlauncher.constants.Constants.ScreenResolution;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 */
public interface IJavaSettingsViewModel extends IAbstractSettingsViewModel {
    boolean isJava32Bit();

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

    void addOnInitialRamChanged(Consumer<Integer> onChanged);


    /**
     * Set the maximum ram
     *
     * @param maxRam max ram value
     * @return null if no warning, else handle the warning
     */
    @Nullable
    MaxRamWarning setMaxRam(int maxRam);

    void addOnMaxRamChanged(Consumer<Integer> onChanged);

    enum MaxRamWarning {
        ABOVE_8GB,
        ABOVE_HALF;
    }

    int getPermGenMaxRecommendSize();

    /**
     * Set the perm gen size
     *
     * @param permGen perm gen size
     * @return true to show warning, false otherwise
     */
    boolean setPermGen(int permGen);

    void addOnPermGenChanged(Consumer<Integer> onChanged);


    void setWidth(int width);

    void addOnWidthChanged(Consumer<Integer> onChanged);


    void setHeight(int height);

    void addOnHeightChanged(Consumer<Integer> onChanged);


    List<ScreenResolution> getScreenResolutions();

    void setScreenResolution(ScreenResolution resolution);


    List<String> getJavaPaths();

    void resetJavaPath();

    /**
     * Set the java path
     *
     * @param path the path
     * @return true if valid
     */
    boolean setJavaPath(String path);

    String getJavaPath();

    void addOnJavaPathChanged(Consumer<String> onChanged);


    void resetJavaParams();

    /**
     * Set the java params
     *
     * @param params parameters
     * @return true if valid
     */
    boolean setJavaParams(String params);

    void addOnJavaParamsChanged(Consumer<String> onChanged);


    void setStartMinecraftMax(Boolean b);

    void addOnStartMinecraftMaxChanged(Consumer<Boolean> onChanged);


    void setIgnoreJavaChecks(Boolean b);

    void addOnIgnoreJavaChecksChanged(Consumer<Boolean> onChanged);


    boolean getUseJavaFromMinecraftEnabled();

    void setJavaFromMinecraft(Boolean b);

    void addOnJavaFromMinecraftChanged(Consumer<Boolean> onChanged);


    void setDisableLegacyLaunching(Boolean b);

    void addOnDisableLegacyLaunchingChanged(Consumer<Boolean> onChanged);


    void setSystemGLFW(Boolean b);

    void addOnSystemGLFWChanged(Consumer<Boolean> onChanged);


    void setSystemOpenAL(Boolean b);

    void addOnSystemOpenALChanged(Consumer<Boolean> onChanged);


}