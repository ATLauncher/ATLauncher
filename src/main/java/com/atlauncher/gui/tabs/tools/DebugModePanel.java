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
package com.atlauncher.gui.tabs.tools;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.evnt.listener.RelocalizationListener;

@SuppressWarnings("serial")
public class DebugModePanel extends AbstractToolPanel implements RelocalizationListener {

    public DebugModePanel(IToolsViewModel viewModel) {
        super(viewModel);

        LAUNCH_BUTTON.setEnabled(viewModel.isLaunchInDebugEnabled());
    }

    @Override
    protected void onLaunch() {
        if (viewModel.isLaunchInDebugEnabled()) {
            viewModel.launchInDebug();
        }
    }

    @NotNull
    @Override
    protected String getLabel() {
        return new HTMLBuilder()
            .center()
            .split(70)
            .text(GetText.tr(
                "Use this to relaunch ATLauncher in debug mode. This can be used to get more debug logs in order to help diagnose issues with ATLauncher."
            ))
            .build();
    }

    @Nonnull
    @Override
    protected String getTitle() {
        return GetText.tr("Debug Mode");
    }
}
