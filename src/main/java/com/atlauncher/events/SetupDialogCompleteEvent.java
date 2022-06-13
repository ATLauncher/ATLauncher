package com.atlauncher.events;

public final class SetupDialogCompleteEvent implements AnalyticsEvent{
    SetupDialogCompleteEvent(){}

    @Override
    public String getAction() {
        return AnalyticsActions.SETUP_DIALOG_COMPLETE.getAnalyticsValue();
    }

    @Override
    public String getCategory() {
        return AnalyticsCategories.LAUNCHER.getAnalyticsCategory();
    }

    public static SetupDialogCompleteEvent of(){
        return new SetupDialogCompleteEvent();
    }
}