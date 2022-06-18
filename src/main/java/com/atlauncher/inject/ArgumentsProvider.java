package com.atlauncher.inject;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

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