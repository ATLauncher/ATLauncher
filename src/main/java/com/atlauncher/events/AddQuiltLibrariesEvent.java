package com.atlauncher.events;

public final class AddQuiltLibrariesEvent extends AbstractAnalyticsEvent {
    private AddQuiltLibrariesEvent(final AnalyticsCategories category){
        super(AnalyticsActions.ADD_QUILT_STANDARD_LIBRARIES, category);
    }

    public static AddQuiltLibrariesEvent forMod(final AnalyticsCategories category){
        return new AddQuiltLibrariesEvent(category);
    }

    public static AddQuiltLibrariesEvent forModrinthMod(){
        return forMod(AnalyticsCategories.MODRINTH);
    }
}