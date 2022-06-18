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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.inject.Named;

/**
 * A Guice Module for binding the app arguments.
 *
 * Node(s):
 *   - This needs to be constructed at launch so the arguments can get injected where needed.
 */
public final class ArgumentsModule extends AbstractModule {
    public static final String ARGUMENTS = "Arguments";
    private final String[] arguments;

    /**
     * Constructor
     * @param arguments The command line arguments passed in at launch
     */
    public ArgumentsModule(final String[] arguments) {
        this.arguments = arguments;
    }

    @Override
    protected void configure() {
        this.bind(OptionParser.class)
            .toProvider(ArgumentsParserProvider.class);
        this.bind(OptionSet.class)
            .toProvider(ArgumentsProvider.class);
    }

    /**
     * Provider function for supplying the application arguments to, primarily to the
     * {@link OptionParser} so it can provide the {@link OptionSet}.
     *
     * @return The command line arguments supplied during the construction of this module at launch
     */
    @Provides
    @Named(ARGUMENTS)
    public String[] getArguments() {
        return this.arguments;
    }
}