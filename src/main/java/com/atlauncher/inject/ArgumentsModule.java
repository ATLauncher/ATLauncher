package com.atlauncher.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.inject.Named;

public final class ArgumentsModule extends AbstractModule{
    public static final String ARGUMENTS = "Arguments";
    private final String[] arguments;

    public ArgumentsModule(final String[] arguments){
        this.arguments = arguments;
    }

    @Override
    protected void configure(){
        this.bind(OptionParser.class)
            .toProvider(ArgumentsParserProvider.class);
        this.bind(OptionSet.class)
            .toProvider(ArgumentsProvider.class);
    }

    @Provides
    @Named(ARGUMENTS)
    public String[] getArguments(){
        return this.arguments;
    }
}