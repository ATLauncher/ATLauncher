package com.atlauncher.task.account;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.task.Task;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public abstract class AccountTask implements Task {
    private final AbstractAccount account;
    private final CountDownLatch latch;

    protected AccountTask(@Nonnull final AbstractAccount account,
                          @Nullable final CountDownLatch latch){
        Preconditions.checkNotNull(account);
        this.account = account;
        this.latch = latch;
    }

    protected AccountTask(@Nonnull final AbstractAccount account){
        this(account, null);
    }

    public final AbstractAccount getAccount(){
        return this.account;
    }

    public final Optional<CountDownLatch> getLatch(){
        return Optional.ofNullable(this.latch);
    }
}