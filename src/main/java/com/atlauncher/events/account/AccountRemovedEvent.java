package com.atlauncher.events.account;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.events.AbstractAnalyticsEvent;
import com.atlauncher.events.AnalyticsAction;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;
import com.atlauncher.events.AnalyticsEvent;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

//TODO: refactor
public final class AccountRemovedEvent extends AccountEvent implements AnalyticsEvent {
    AccountRemovedEvent(final AbstractAccount account){
        super(account);
    }

    public static AccountRemovedEvent of(@Nonnull  final AbstractAccount account){
        Preconditions.checkNotNull(account);
        return new AccountRemovedEvent(account);
    }

    @Override
    public String getCategory() {
        return AnalyticsCategories.ACCOUNT.getAnalyticsCategory();
    }

    @Override
    public String getAction() {
        return AnalyticsActions.DELETE.getAnalyticsValue();
    }
}
