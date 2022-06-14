package com.atlauncher.events.account;

import com.atlauncher.events.AbstractAnalyticsEvent;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;

public final class AccountUpdateUsernameEvent extends AbstractAnalyticsEvent {
    AccountUpdateUsernameEvent(){
        super(AnalyticsActions.UPDATE_USERNAME, AnalyticsCategories.ACCOUNT);
    }

    public static AccountUpdateUsernameEvent newInstance(){
        return new AccountUpdateUsernameEvent();
    }
}
