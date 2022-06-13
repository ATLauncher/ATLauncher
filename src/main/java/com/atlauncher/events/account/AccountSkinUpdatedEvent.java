package com.atlauncher.events.account;

import com.atlauncher.events.AbstractAnalyticsEvent;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;

public final class AccountSkinUpdatedEvent extends AbstractAnalyticsEvent{
    AccountSkinUpdatedEvent(){
        super(AnalyticsActions.UPDATE_SKIN, AnalyticsCategories.ACCOUNT);
    }

    public static AccountSkinUpdatedEvent newInstance(){
        return new AccountSkinUpdatedEvent();
    }
}