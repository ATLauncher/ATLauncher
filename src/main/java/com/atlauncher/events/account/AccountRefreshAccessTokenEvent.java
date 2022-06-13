package com.atlauncher.events.account;

import com.atlauncher.events.AbstractAnalyticsEvent;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;

public final class AccountRefreshAccessTokenEvent extends AbstractAnalyticsEvent{
    AccountRefreshAccessTokenEvent(){
        super(AnalyticsActions.REFRESH_ACCESS_TOKEN, AnalyticsCategories.ACCOUNT);
    }

    public static AccountRefreshAccessTokenEvent newInstance(){
        return new AccountRefreshAccessTokenEvent();
    }
}