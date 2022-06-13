package com.atlauncher.events.account;


import com.atlauncher.events.AbstractAnalyticsEvent;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;

public final class AccountEditEvent extends AbstractAnalyticsEvent{
    AccountEditEvent(){
        super(AnalyticsActions.EDIT, AnalyticsCategories.ACCOUNT);
    }

    public static AccountEditEvent newInstance(){
        return new AccountEditEvent();
    }
}