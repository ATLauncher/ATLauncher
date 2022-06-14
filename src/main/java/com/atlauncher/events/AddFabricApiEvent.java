package com.atlauncher.events;

public final class AddFabricApiEvent extends AbstractAnalyticsEvent{
    AddFabricApiEvent(final AnalyticsCategories category){
        super(AnalyticsActions.ADD_FABRIC_API, category);
    }

    public static AddFabricApiEvent forMod(final AnalyticsCategories category){
        return new AddFabricApiEvent(category);
    }

    public static AddFabricApiEvent forCurseForgeMod(){
        return forMod(AnalyticsCategories.CURSE_FORGE_MOD);
    }

    public static AddFabricApiEvent forModrinthMod(){
        return forMod(AnalyticsCategories.MODRINTH);
    }
}