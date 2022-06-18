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
package com.atlauncher.inject;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/**
 * A {@link Provider} that constructs the app {@link OptionSet} using the provided {@link OptionParser}
 * and command line arguments.
 */
public final class ArgumentsProvider implements Provider<OptionSet>{
    private final String[] arguments;
    private final OptionParser parser;

    @Inject
    private ArgumentsProvider(@Named(ArgumentsModule.ARGUMENTS) final String[] arguments,
                              final OptionParser parser){
        this.arguments = arguments;
        this.parser = parser;
    }

    @Override
    public OptionSet get(){
        return this.parser.parse(this.arguments);
    }
}