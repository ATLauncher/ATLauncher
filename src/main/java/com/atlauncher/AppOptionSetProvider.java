package com.atlauncher;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public final class AppOptionSetProvider implements Provider<OptionSet>{
    private final OptionParser parser;
    private final String[] arguments;

    @Inject
    private AppOptionSetProvider(final OptionParser parser,
                                 @Named("arguments") final String[] arguments){
        this.parser = parser;
        this.arguments = arguments;
    }

    @Override
    public OptionSet get(){
        return this.parser.parse(this.arguments);
    }
}