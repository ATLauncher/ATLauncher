package com.atlauncher.themes;

import com.atlauncher.constants.Constants;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

public final class CurrentThemeProvider implements Provider<ATLauncherLaf> {
    private static final Logger LOG = LogManager.getLogger(CurrentThemeProvider.class);
    private final String theme;
    private final Injector injector;

    @Inject
    private CurrentThemeProvider(@Named("CurrentTheme") final String theme, final Injector injector){
        this.theme = theme;
        this.injector = injector;
    }

    @SuppressWarnings({ "unchecked" })
    private static Class<? extends ATLauncherLaf> getThemeClassOrDefault(final String className) throws Exception{
        try {
            return (Class<? extends ATLauncherLaf>) Class.forName(className);
        } catch(ClassNotFoundException exc){
            LOG.error("failed to get theme class {}", className, exc);
            return (Class<? extends ATLauncherLaf>) Class.forName(Constants.DEFAULT_THEME_CLASS);
        }
    }

    @Override
    public ATLauncherLaf get(){
        LOG.info("Creating theme");
        try{
            final Class<? extends ATLauncherLaf> cls = getThemeClassOrDefault(this.theme);
            return this.injector.getInstance(cls);
        } catch(Exception exc){ // should never happen
            LOG.error("this should never happen", exc);
            return null;
        }
    }
}