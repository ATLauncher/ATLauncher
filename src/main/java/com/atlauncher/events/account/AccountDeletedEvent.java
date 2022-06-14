package com.atlauncher.events.account;

import com.atlauncher.events.AbstractAnalyticsEvent;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;

public final class AccountDeletedEvent extends AbstractAnalyticsEvent{
    AccountDeletedEvent(){
        super(AnalyticsActions.DELETE, AnalyticsCategories.ACCOUNT);
    }

    public static AccountDeletedEvent newInstance(){
        return new AccountDeletedEvent();
    }
}
