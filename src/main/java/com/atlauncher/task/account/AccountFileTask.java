package com.atlauncher.task.account;

import com.atlauncher.task.ResourceTask;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public abstract class AccountFileTask extends ResourceTask{
    protected AccountFileTask(@NotNull final Path file,
                              @NotNull final Gson gson,
                              @Nullable final CountDownLatch latch) {
        super(file, gson, latch);
    }
}
