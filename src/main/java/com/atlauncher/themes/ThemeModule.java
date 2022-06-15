package com.atlauncher.themes;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public final class ThemeModule extends AbstractModule{
    @Override
    protected void configure(){
        this.bindTheme(ArcOrange.class, ArcOrange.NAME);
        this.bindTheme(CyanLight.class, CyanLight.NAME);
        this.bindTheme(Dark.class, Dark.NAME);
        this.bindTheme(DraculaContrast.class, DraculaContrast.NAME);
        this.bindTheme(HiberbeeDark.class, HiberbeeDark.NAME);
        this.bindTheme(HighTechDarkness.class, HighTechDarkness.NAME);
        this.bindTheme(Light.class, Light.NAME);
        this.bindTheme(MaterialPalenightContrast.class, MaterialPalenightContrast.NAME);
        this.bindTheme(MonokaiPro.class, MonokaiPro.NAME);
        this.bindTheme(OneDark.class, OneDark.NAME);
        this.bindTheme(Vuesion.class, Vuesion.NAME);

        this.bind(ATLauncherLaf.class)
            .toProvider(CurrentThemeProvider.class);
    }

    private <T extends ATLauncherLaf> void bindTheme(final Class<T> cls, final String name){
        this.bind(cls)
            .annotatedWith(Names.named(name))
            .to(cls);
    }
}
