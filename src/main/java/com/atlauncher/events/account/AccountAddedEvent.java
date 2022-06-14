/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.events.account;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;
import com.atlauncher.events.AnalyticsEvent;

public final class AccountAddedEvent extends AccountEvent implements AnalyticsEvent {
    AccountAddedEvent(final AbstractAccount account){
        super(account);
    }

    @Override
    public String getLabel() {
        return getAccount() instanceof MicrosoftAccount ? "Microsoft" : "Mojang";
    }

    @Override
    public String getCategory() {
        return AnalyticsCategories.ACCOUNT.getAnalyticsCategory();
    }

    @Override
    public String getAction(){
        return AnalyticsActions.ADD.getAnalyticsValue();
    }

    public static AccountAddedEvent of(final AbstractAccount account){
        return new AccountAddedEvent(account);
    }
}
