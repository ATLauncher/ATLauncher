package com.atlauncher.events;

public final class AddFabricApiEvent extends AnalyticsEvent.AppEvent{
    public static final String ACTION = "AddFabricApi";

    private AddFabricApiEvent(final String category){
        super(ACTION, category);
    }

    public static AddFabricApiEvent forCurseForge(){
        return new AddFabricApiEvent("CurseForgeMod");
    }

    public static AddFabricApiEvent forModrinth(){
        return new AddFabricApiEvent("Modrinth");
    }
}