package com.atlauncher.events;

public enum AnalyticsCategories implements AnalyticsCategory{
    CURSE_FORGE_MOD("CurseForgeMod"),
    MODRINTH("Modrinth"),
    IMPORT_INSTANCE("ImportInstance"),
    ACCOUNT("Account"),
    NAVIGATION("Navigation"),
    TOOL("Tool"),
    SERVER("Server"),
    LAUNCHER("Launcher"),
    FEATURED_PACK("FeaturedPack")
    ;

    private final String value;

    AnalyticsCategories(final String value){
        this.value = value;
    }

    @Override
    public String getAnalyticsCategory() {
        return this.value;
    }
}