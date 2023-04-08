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
package com.atlauncher.viewmodel.base;

import javax.annotation.Nonnull;

/**
 * 13 / 06 / 2022
 * <p>
 * View model for the about tab.
 */
public interface IAboutTabViewModel {

    /**
     * @return Array of authors
     */
    @Nonnull
    String[] getAuthors();

    /**
     * @return Info about the launcher and its environment
     */
    @Nonnull
    String getInfo();

    /**
     * @return Info to be copied to users clipboard
     */
    @Nonnull
    String getCopyInfo();
}
